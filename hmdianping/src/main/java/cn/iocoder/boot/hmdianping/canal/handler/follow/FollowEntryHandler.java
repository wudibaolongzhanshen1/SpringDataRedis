package cn.iocoder.boot.hmdianping.canal.handler.follow;

import cn.iocoder.boot.hmdianping.canal.handler.EntryHandler;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.FlatMessage;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class FollowEntryHandler implements EntryHandler {

    // 注入你处理业务逻辑的 Service，比如 Redis 同步
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private RBloomFilter<Long> followBloomFilter;

    @Override
    public void handle(FlatMessage flatMessage) {
        String type = flatMessage.getType();
        // FlatMessage 的 data 是一个 List，代表这一个批次处理的行数据
        List<Map<String, String>> dataList = flatMessage.getData();
        for (Map<String, String> rowData : dataList) {
            // 直接根据 Key 获取，注意取出来全是 String
            String userIdStr = rowData.get("user_id");
            String followUserIdStr = rowData.get("follow_user_id");
            if (userIdStr == null || followUserIdStr == null) continue;
            Long userId = Long.parseLong(userIdStr);
            Long followUserId = Long.parseLong(followUserIdStr);
            String redisKey = "follows:" + userId;
            if ("INSERT".equalsIgnoreCase(type)) {
                log.info("MQ同步 [新增]: {} 关注了 {}", userId, followUserId);
                stringRedisTemplate.opsForSet().add(redisKey, followUserIdStr);
                followBloomFilter.add(followUserId);
            } else if ("DELETE".equalsIgnoreCase(type)) {
                log.info("MQ同步 [取消]: {} 取消关注 {}", userId, followUserId);
                stringRedisTemplate.opsForSet().remove(redisKey, followUserIdStr);
            }
        }
    }

    @Override
    public String getTableName() {
        return "tb_follow";
    }
}
