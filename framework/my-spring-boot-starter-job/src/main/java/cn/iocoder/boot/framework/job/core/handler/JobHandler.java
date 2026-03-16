package cn.iocoder.boot.framework.job.core.handler;

public interface JobHandler {

    String execute(String param) throws Exception;
}
