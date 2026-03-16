package cn.iocoder.boot.hmdianping.service.follow.Impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.iocoder.boot.framework.common.exception.ServiceException;
import cn.iocoder.boot.framework.redis.core.CacheClient;
import cn.iocoder.boot.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.boot.hmdianping.dal.dataobject.follow.FollowDO;
import cn.iocoder.boot.hmdianping.dal.mysql.follow.FollowMapper;
import cn.iocoder.boot.hmdianping.service.follow.FollowService;
import cn.iocoder.boot.hmdianping.util.TransactionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.redisson.api.RBloomFilter;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>
 * 用户关注表 服务实现类
 * </p>
 *
 * @author czl
 * @since 2026-03-11
 */
@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, FollowDO> implements FollowService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    private static final String KEY_FOLLOW = "follows:%s";
    private static final DefaultRedisScript<Long> FOLLOW_SET_SCRIPT;
    @Resource
    private CacheClient cacheClient;
    @Resource
    private RBloomFilter<Long> followBloomFilter;

    static {
        FOLLOW_SET_SCRIPT = new DefaultRedisScript<>();
        String script = "redis.call('SADD', KEYS[1], unpack(ARGV, 2)); " +
                "return redis.call('EXPIRE', KEYS[1], ARGV[1]);";
        FOLLOW_SET_SCRIPT.setScriptText(script);
        FOLLOW_SET_SCRIPT.setResultType(Long.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void follow(Long followUserId, Boolean isFollow) {
        Boolean ifAlreadyFollow = isFollow(followUserId);
        if (ifAlreadyFollow) {throw new ServiceException(500,"已经关注了，不可重复关注");}
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        // 2. 判断是关注还是取关
        if (isFollow) {
            // 3. 关注：新增数据
            FollowDO followDO = FollowDO.builder().userId(userId).followUserId(followUserId).build();
            boolean isSuccess = save(followDO);
            if (!isSuccess) throw new RuntimeException("followDO数据库save失败");
        } else {
            // 4. 取关：删除数据
            boolean isSuccess = remove(new QueryWrapper<>(FollowDO.class).eq("user_id", userId)
                    .eq("follow_user_id", followUserId));
            if (!isSuccess) throw new RuntimeException("followDO数据库remove失败");
        }
    }

    @Override
    public Boolean isFollow(Long followUserId) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        Set<String> followUserIds = cacheClient.querySetWithMutexAndBloom("follows:", userId,
                String.class, followBloomFilter::contains, this::loadFollowUserIdFromDB,
                FOLLOW_SET_SCRIPT, 30L, TimeUnit.SECONDS);
        if (followUserIds == null || !followUserIds.contains(followUserId.toString())) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;

    }

    private Set<String> loadFollowUserIdFromDB(Long userId) {
        List<FollowDO> followList = list(new LambdaQueryWrapper<FollowDO>()
                .eq(FollowDO::getUserId, userId)
                .select(FollowDO::getFollowUserId));
        if (CollUtil.isEmpty(followList)) {
            return Collections.emptySet();
        }
        return followList.stream().map(followDO -> followDO.getFollowUserId().toString())
                .collect(Collectors.toSet());
    }

    private String formatKey(Long userId) {
        return String.format(KEY_FOLLOW, userId);
    }
}