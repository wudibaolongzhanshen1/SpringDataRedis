package cn.iocoder.boot.hmdianping.controller.job;


import cn.iocoder.boot.framework.common.pojo.CommonResult;
import cn.iocoder.boot.framework.common.util.BeanUtils;
import cn.iocoder.boot.hmdianping.controller.job.vo.JobRespVO;
import cn.iocoder.boot.hmdianping.controller.job.vo.JobSaveReqVO;
import cn.iocoder.boot.hmdianping.dal.dataobject.job.JobDO;
import cn.iocoder.boot.hmdianping.service.job.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.quartz.SchedulerException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.iocoder.boot.framework.common.pojo.CommonResult.success;

@RestController
@Tag(name = "管理后台 - 定时任务")
@RequestMapping("/infra/job")
@Validated
public class JobController {
    @Resource
    private JobService jobService;

    @PostMapping("/create")
    @Operation(summary = "创建定时任务")
    public CommonResult<Long> createJob(@Valid @RequestBody JobSaveReqVO createReqVO)
            throws SchedulerException {
        return success(jobService.createJob(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新定时任务")
    public CommonResult<Boolean> updateJob(@Valid @RequestBody JobSaveReqVO updateReqVO)
            throws SchedulerException {
        jobService.updateJob(updateReqVO);
        return success(true);
    }

    @PutMapping("/update-status")
    @Operation(summary = "更新定时任务的状态")
    @Parameters({
            @Parameter(name = "id", description = "编号", required = true, example = "1024"),
            @Parameter(name = "status", description = "状态", required = true, example = "1"),
    })
    public CommonResult<Boolean> updateJobStatus(@RequestParam(value = "id") Long id, @RequestParam("status") Integer status)
            throws SchedulerException {
        jobService.updateJobStatus(id, status);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除定时任务")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    public CommonResult<Boolean> deleteJob(@RequestParam("id") Long id)
            throws SchedulerException {
        jobService.deleteJob(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "批量删除定时任务")
    @Parameter(name = "ids", description = "编号列表", required = true)
    public CommonResult<Boolean> deleteJobList(@RequestParam("ids") List<Long> ids)
            throws SchedulerException {
        jobService.deleteJobList(ids);
        return success(true);
    }

    @PutMapping("/trigger")
    @Operation(summary = "触发定时任务")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    public CommonResult<Boolean> triggerJob(@RequestParam("id") Long id) throws SchedulerException {
        jobService.triggerJob(id);
        return success(true);
    }

    @PostMapping("/sync")
    @Operation(summary = "同步定时任务")
    public CommonResult<Boolean> syncJob() throws SchedulerException {
        jobService.syncJob();
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得定时任务")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    public CommonResult<JobRespVO> getJob(@RequestParam("id") Long id) {
        JobDO job = jobService.getJob(id);
        return success(BeanUtils.toBean(job, JobRespVO.class));
    }
}
