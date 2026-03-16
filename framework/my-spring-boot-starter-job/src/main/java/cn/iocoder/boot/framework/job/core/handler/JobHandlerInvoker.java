package cn.iocoder.boot.framework.job.core.handler;

import cn.hutool.core.thread.ThreadUtil;
import cn.iocoder.boot.framework.job.core.enums.JobDataKeyEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.time.LocalDateTime;

import static cn.hutool.core.lang.Console.log;

@DisallowConcurrentExecution
@PersistJobDataAfterExecution
@Slf4j
public class JobHandlerInvoker extends QuartzJobBean {

    @Resource
    private ApplicationContext applicationContext;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        Long jobId = context.getMergedJobDataMap().getLong(JobDataKeyEnum.JOB_ID.name());
        String jobHandlerName = context.getMergedJobDataMap().getString(JobDataKeyEnum.JOB_HANDLER_NAME.name());
        String jobHandlerParam = context.getMergedJobDataMap().getString(JobDataKeyEnum.JOB_HANDLER_PARAM.name());
        int refireCount = context.getRefireCount();
        int retryCount = (Integer) context.getMergedJobDataMap().getOrDefault(JobDataKeyEnum.JOB_RETRY_COUNT.name(), 0);
        int retryInterval = (Integer) context.getMergedJobDataMap().getOrDefault(JobDataKeyEnum.JOB_RETRY_INTERVAL.name(), 0);
        LocalDateTime now = LocalDateTime.now();
        JobHandler jobHandler = applicationContext.getBean(jobHandlerName, JobHandler.class);
        try {
            String result = jobHandler.execute(jobHandlerParam);
            log("[executeInternal][执行 Job({}) 处理器({}) 参数({}) 成功，结果({})，耗时 {} 秒]",
                    jobId, jobHandlerName, jobHandlerParam, result, LocalDateTime.now().minusSeconds(now.getSecond()).getSecond());
        } catch (Exception e) {
            handleException(e, refireCount, retryCount, retryInterval);
        }
    }

    private void handleException(Throwable exception, int refireCount, int retryCount, int retryInterval) throws JobExecutionException {
        if (exception == null) {
            return;
        }
        if (refireCount >= retryCount) {
            JobHandlerInvoker.log.error("[handleException][重试次数({})超过最大重试次数({})]", refireCount, retryCount, exception);
            return;
        }
        // 情况二：如果未到达重试上限，则 sleep 一定间隔时间，然后重试
        // 这里使用 sleep 来实现，主要还是希望实现比较简单。因为，同一时间，不会存在大量失败的 Job。
        if (retryInterval > 0) {
            ThreadUtil.sleep(retryInterval);
        }
        JobHandlerInvoker.log.warn("[handleException][第{}次重试]", refireCount + 1, exception);
        // 重试
        throw new JobExecutionException(exception, true);
    }
}
