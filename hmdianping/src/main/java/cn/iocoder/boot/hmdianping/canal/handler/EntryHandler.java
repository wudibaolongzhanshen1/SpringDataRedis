package cn.iocoder.boot.hmdianping.canal.handler;
import com.alibaba.otter.canal.protocol.FlatMessage;

public interface EntryHandler {
    void handle(FlatMessage flatMessage);

    /**
     * 该处理器负责的表名
     */
    String getTableName();
}
