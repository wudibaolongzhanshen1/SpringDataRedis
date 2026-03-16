package cn.iocoder.boot.hmdianping.service.job;

import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.boot.framework.common.exception.ServiceException;
import cn.iocoder.boot.framework.common.util.BeanUtils;
import cn.iocoder.boot.framework.job.core.handler.JobHandler;
import cn.iocoder.boot.framework.job.core.scheduler.SchedulerManager;
import cn.iocoder.boot.framework.job.core.util.CronUtils;
import cn.iocoder.boot.hmdianping.controller.job.vo.JobSaveReqVO;
import cn.iocoder.boot.hmdianping.dal.dataobject.job.JobDO;
import cn.iocoder.boot.hmdianping.dal.mysql.job.JobMapper;
import cn.iocoder.boot.hmdianping.enums.job.JobStatusEnum;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Objects;

import static cn.iocoder.boot.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.boot.framework.common.util.CollectionUtils.containsAny;

/**
 * 定时任务 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class JobServiceImpl implements JobService {

    @Resource
    private JobMapper jobMapper;

    @Resource
    private SchedulerManager schedulerManager;

    @PostConstruct // <--- 核心！Spring 容器启动完成后会自动执行
    public void init() throws SchedulerException {
        // 这里的逻辑是：为了防止手动修改数据库导致的不一致，
        // 每次启动时，都强制把数据库里的任务同步一遍到 Quartz
        this.syncJob();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createJob(JobSaveReqVO createReqVO) throws SchedulerException {
        validateCronExpression(createReqVO.getCronExpression());
        // 1.1 校验唯一性
        if (jobMapper.selectByHandlerName(createReqVO.getHandlerName()) != null) {
            throw new ServiceException(500, "HandlerName 已存在");
        }
        // 1.2 校验 JobHandler 是否存在
        validateJobHandlerExists(createReqVO.getHandlerName());

        // 2. 插入 JobDO
        JobDO job = BeanUtils.toBean(createReqVO, JobDO.class);
        job.setStatus(JobStatusEnum.INIT.getStatus());
        fillJobMonitorTimeoutEmpty(job);
        jobMapper.insert(job);

        // 3.1 添加 Job 到 Quartz 中
        schedulerManager.addJob(job.getId(), job.getHandlerName(), job.getHandlerParam(), job.getCronExpression(),
                createReqVO.getRetryCount(), createReqVO.getRetryInterval());
        // 3.2 更新 JobDO
        JobDO updateObj = JobDO.builder().id(job.getId()).status(JobStatusEnum.NORMAL.getStatus()).build();
        jobMapper.updateById(updateObj);
        return job.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateJob(JobSaveReqVO updateReqVO) throws SchedulerException {
        validateCronExpression(updateReqVO.getCronExpression());
        // 1.1 校验存在
        JobDO job = validateJobExists(updateReqVO.getId());
        // 1.2 只有开启状态，才可以修改.原因是，如果出暂停状态，修改 Quartz Job 时，会导致任务又开始执行
        if (!job.getStatus().equals(JobStatusEnum.NORMAL.getStatus())) {
            throw new ServiceException(500, "Job 只有开启状态，才可以修改");
        }
        // 1.3 校验 JobHandler 是否存在
        validateJobHandlerExists(updateReqVO.getHandlerName());

        // 2. 更新 JobDO
        JobDO updateObj = BeanUtils.toBean(updateReqVO, JobDO.class);
        fillJobMonitorTimeoutEmpty(updateObj);
        jobMapper.updateById(updateObj);

        // 3. 更新 Job 到 Quartz 中
        schedulerManager.updateJob(job.getHandlerName(), updateReqVO.getHandlerParam(), updateReqVO.getCronExpression(),
                updateReqVO.getRetryCount(), updateReqVO.getRetryInterval());
    }

    private void validateJobHandlerExists(String handlerName) {
        try {
            Object handler = SpringUtil.getBean(handlerName);
            assert handler != null;
            if (!(handler instanceof JobHandler)) {
                throw new ServiceException(500, "HandlerName 对应的 Bean 不是 JobHandler 类型");
            }
        } catch (NoSuchBeanDefinitionException e) {
            throw new ServiceException(500, "HandlerName 对应的 Bean 不存在");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateJobStatus(Long id, Integer status) throws SchedulerException {
        // 校验 status
        if (!containsAny(status, JobStatusEnum.NORMAL.getStatus(), JobStatusEnum.STOP.getStatus())) {
            throw new ServiceException(500, "无效的 Job 状态");
        }
        // 校验存在
        JobDO job = validateJobExists(id);
        // 校验是否已经为当前状态
        if (job.getStatus().equals(status)) {
            throw new ServiceException(500, "Job 已经处于 " + status + " 状态");
        }
        // 更新 Job 状态
        JobDO updateObj = JobDO.builder().id(id).status(status).build();
        jobMapper.updateById(updateObj);
        System.out.println("====== 正在尝试恢复 Quartz 任务，Key 为: [" + job.getHandlerName() + "] ======");
        // 更新状态 Job 到 Quartz 中
        if (JobStatusEnum.NORMAL.getStatus().equals(status)) { // 开启
            schedulerManager.resumeJob(job.getHandlerName());
        } else { // 暂停
            schedulerManager.pauseJob(job.getHandlerName());
        }
    }

    @Override
    public void triggerJob(Long id) throws SchedulerException {
        // 校验存在
        JobDO job = validateJobExists(id);

        // 触发 Quartz 中的 Job
        schedulerManager.triggerJob(job.getId(), job.getHandlerName(), job.getHandlerParam());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncJob() throws SchedulerException {
        // 1. 查询 Job 配置
        List<JobDO> jobList = jobMapper.selectList(new QueryWrapper<>());
        // 2. 遍历处理
        for (JobDO job : jobList) {
            // 2.1 先删除，再创建
            schedulerManager.deleteJob(job.getHandlerName());
            schedulerManager.addJob(job.getId(), job.getHandlerName(), job.getHandlerParam(), job.getCronExpression(),
                    job.getRetryCount(), job.getRetryInterval());
            // 2.2 如果 status 为暂停，则需要暂停
            if (Objects.equals(job.getStatus(), JobStatusEnum.STOP.getStatus())) {
                schedulerManager.pauseJob(job.getHandlerName());
            }
            log.info("[syncJob][id({}) handlerName({}) 同步完成]", job.getId(), job.getHandlerName());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteJob(Long id) throws SchedulerException {
        // 校验存在
        JobDO job = validateJobExists(id);
        // 更新
        jobMapper.deleteById(id);

        // 删除 Job 到 Quartz 中
        schedulerManager.deleteJob(job.getHandlerName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteJobList(List<Long> ids) throws SchedulerException {
        // 批量删除
        List<JobDO> jobs = jobMapper.selectByIds(ids);
        jobMapper.deleteByIds(ids);

        // 删除 Job 到 Quartz 中
        for (JobDO job : jobs) {
            schedulerManager.deleteJob(job.getHandlerName());
        }
    }

    private JobDO validateJobExists(Long id) {
        JobDO job = jobMapper.selectById(id);
        if (job == null) {
            throw new ServiceException(500, "Job 不存在");
        }
        return job;
    }

    private void validateCronExpression(String cronExpression) {
        if (!CronUtils.isValid(cronExpression)) {
            throw new ServiceException(500, "无效的 Cron 表达式");
        }
    }

    @Override
    public JobDO getJob(Long id) {
        return jobMapper.selectById(id);
    }

    private static void fillJobMonitorTimeoutEmpty(JobDO job) {
        if (job.getMonitorTimeout() == null) {
            job.setMonitorTimeout(0);
        }
    }

}
