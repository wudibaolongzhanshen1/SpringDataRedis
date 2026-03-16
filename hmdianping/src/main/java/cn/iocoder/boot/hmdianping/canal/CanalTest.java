package cn.iocoder.boot.hmdianping.canal;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.protocol.Message;
import com.alibaba.otter.canal.protocol.CanalEntry.*;
import java.net.InetSocketAddress;
import java.util.List;

public class CanalTest {
    public static void main(String[] args) {
        // 1. 创建连接 (注意：如果你改了 canal.ip=0.0.0.0，这里填 127.0.0.1 即可)
        CanalConnector connector = CanalConnectors.newSingleConnector(
                new InetSocketAddress("127.0.0.1", 11111), "example", "", "");

        int batchSize = 1000;
        try {
            connector.connect();
            connector.subscribe(".*\\..*"); // 订阅所有库和表
            connector.rollback(); // 回滚到上次消费位点
            System.out.println(">>>>>> Canal 客户端已成功启动，正在监听数据... <<<<<<");

            while (true) {
                Message message = connector.getWithoutAck(batchSize); // 获取数据
                long batchId = message.getId();
                List<Entry> entries = message.getEntries();

                if (batchId == -1 || entries.isEmpty()) {
                    Thread.sleep(1000); // 没数据时歇一会儿
                    continue;
                }

                printEntry(entries);
                connector.ack(batchId); // 确认消费成功
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connector.disconnect();
        }
    }

    private static void printEntry(List<Entry> entries) throws Exception {
        for (Entry entry : entries) {
            if (entry.getEntryType() == EntryType.ROWDATA) {
                RowChange rowChange = RowChange.parseFrom(entry.getStoreValue());
                EventType eventType = rowChange.getEventType();

                System.out.println(String.format("================> 库名:%s, 表名:%s, 动作:%s",
                        entry.getHeader().getSchemaName(),
                        entry.getHeader().getTableName(),
                        eventType));

                for (RowData rowData : rowChange.getRowDatasList()) {
                    if (eventType == EventType.INSERT) {
                        System.out.println("--- 监听到插入数据 ---");
                        rowData.getAfterColumnsList().forEach(c -> System.out.println(c.getName() + " : " + c.getValue()));
                    } else if (eventType == EventType.DELETE) {
                        System.out.println("--- 监听到删除数据 ---");
                        rowData.getBeforeColumnsList().forEach(c -> System.out.println(c.getName() + " : " + c.getValue()));
                    }
                }
            }
        }
    }
}