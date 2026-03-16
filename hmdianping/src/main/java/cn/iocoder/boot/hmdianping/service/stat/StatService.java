package cn.iocoder.boot.hmdianping.service.stat;

public interface StatService {
    /**
     * 记录用户访问
     *
     * @param bizName  业务名称，如 "shop"
     * @param targetId 目标ID，如 商户ID
     */
    void recordUV(String bizName, String targetId);

    /**
     * 获取指定日期的 UV 总数
     */
    Long getUVCount(String bizName, String targetId, String date);
}