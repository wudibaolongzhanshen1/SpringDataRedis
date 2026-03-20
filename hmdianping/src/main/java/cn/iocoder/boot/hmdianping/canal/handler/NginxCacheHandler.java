package cn.iocoder.boot.hmdianping.canal.handler;

import com.alibaba.otter.canal.protocol.FlatMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class NginxCacheHandler {

    // 建立表名与 Nginx 缓存前缀的映射关系
    private static final Map<String, String> TABLE_TYPE_MAP = Map.of(
            "tb_shop", "shop",
            "tb_voucher", "voucher",
            "tb_blog", "blog"
    );

    @Autowired
    private RestTemplate restTemplate;

    public void handleCanalMessage(FlatMessage flatMessage) {
        // 1. 根据表名获取缓存类型
        String type = TABLE_TYPE_MAP.get(flatMessage.getTable());
        if (type == null) return; // 该表不需要清理 Nginx 缓存
        // 2. 获取变更数据的 ID 列表
        List<Map<String, String>> data = flatMessage.getData();
        for (Map<String, String> rowData : data) {
            String id = rowData.get("id");
            // 3. 异步通知所有 Nginx 节点（生产环境建议用线程池或 MQ 广播）
            sendClearRequest(type, id);
        }
    }

    private void sendClearRequest(String type, String id) {
        log.info("通知 Nginx 清理缓存: type={}, id={}", type, id);
        // 这里的 node 列表可以从 Nacos 配置中心动态获取
        String node = "http://127.0.0.1:80";
        String url = String.format("%s/internal/cache/clear?type=%s&id=%s", node, type, id);
        try {
            String response = restTemplate.getForObject(url, String.class);
            if ("success: deleted".equals(response)) {
                log.info("Nginx 缓存清理成功: {}", id);
            } else if ("success: key_not_found".equals(response)) {
                log.info("Nginx 缓存已是最新状态（Key 不存在）: {}", id);
            }
        } catch (Exception e) {
            // 如果这里失败，可以考虑将失败任务塞入一个重试队列
            log.error("通知 Nginx 失败，准备重试: {}", url);
        }
    }
}