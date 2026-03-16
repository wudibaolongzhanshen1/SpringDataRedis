package cn.iocoder.boot.hmdianping.canal.handler.blog;

import cn.iocoder.boot.framework.common.util.JsonUtils;
import cn.iocoder.boot.framework.redis.core.util.RedisBatchUtils;
import cn.iocoder.boot.hmdianping.canal.handler.EntryHandler;
import cn.iocoder.boot.hmdianping.dal.dataobject.blog.BlogDO;
import cn.iocoder.boot.hmdianping.dal.dataobject.follow.FollowDO;
import cn.iocoder.boot.hmdianping.service.follow.FollowService;
import com.alibaba.otter.canal.protocol.FlatMessage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static cn.iocoder.boot.hmdianping.enums.RedisKeyConstants.BLOG_ID;
import static cn.iocoder.boot.hmdianping.enums.RedisKeyConstants.FEED_KEY;


@Component
@Slf4j
public class BlogEntryHandler implements EntryHandler {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RBloomFilter<Long> blogBloomFilter;
    @Autowired
    private FollowService followService;

    @Override
    public void handle(FlatMessage flatMessage) {
        String type = flatMessage.getType();
        if (type.equals("INSERT")) {
            handleInsert(flatMessage);
        }
    }

    public void handleInsert(FlatMessage flatMessage) {
        // FlatMessage 的 data 是一个 List，代表这一个批次处理的行数据
        List<Map<String, String>> dataList = flatMessage.getData();
        for (Map<String, String> rowData : dataList) {
            BlogDO blogDO = mapToBlogDO(rowData);
            // 直接根据 Key 获取，注意取出来全是 String
            Long followedUserId = Long.parseLong(rowData.get("user_id"));
            String blogIdStr = rowData.get("id");
            String redisKey = String.format(BLOG_ID, blogIdStr);
            stringRedisTemplate.opsForValue().set(redisKey, JsonUtils.toJsonString(blogDO));
            log.info("MQ同步 [新增]: Redis 插入了博客 id = {}", blogIdStr);
            blogBloomFilter.add(Long.parseLong(blogIdStr));
            log.info("blogBloomFilter 插入了博客 id = {}", blogIdStr);
            // 2. 分页查粉丝，并使用工具类批量推送
            long currentTime = System.currentTimeMillis();
            int page = 1;
            int size = 1000;
            while (true) {
                // 分页查询粉丝 ID 列表 (只查 user_id 字段)
                List<Long> followerIds = followService.lambdaQuery()
                        .eq(FollowDO::getFollowUserId, followedUserId)
                        .select(FollowDO::getUserId)
                        .page(new Page<>(page, size))
                        .getRecords()
                        .stream().map(FollowDO::getUserId).toList();
                if (followerIds.isEmpty()) break;
                // 3. 使用工具类一键开启 Pipeline 推送
                RedisBatchUtils.executeBatch(stringRedisTemplate, followerIds, (connection, fId) -> {
                    String key = FEED_KEY + fId;
                    byte[] rawKey = key.getBytes();
                    byte[] rawValue = blogIdStr.getBytes();
                    // 注意：connection.zAdd 接收的是 byte[]
                    connection.zAdd(rawKey, (double) currentTime, rawValue);
                });
                log.info("MQ同步 [新增]: 博客 id = {} 推送给了粉丝： {}", blogIdStr, followerIds);
                page++;
            }
        }
    }

    private BlogDO mapToBlogDO(Map<String, String> rowData) {
        return BlogDO.builder()
                .id(rowData.get("id") == null ? null : Long.parseLong(rowData.get("id")))
                .userId(rowData.get("user_id") == null ? null : Long.parseLong(rowData.get("user_id")))
                .content(rowData.get("content"))
                .liked(rowData.get("liked") == null ? null : Integer.parseInt(rowData.get("liked")))
                .build();
    }

    @Override
    public String getTableName() {
        return "tb_blog";
    }
}
