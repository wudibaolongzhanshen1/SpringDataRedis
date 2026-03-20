package cn.iocoder.boot.hmdianping.service.shop.impl;

import cn.iocoder.boot.framework.redis.core.CacheClient;
import cn.iocoder.boot.hmdianping.dal.dataobject.shop.ShopDO;
import cn.iocoder.boot.hmdianping.dal.mysql.shop.ShopMapper;
import cn.iocoder.boot.hmdianping.dal.redis.shop.ShopRedisDAO;
import cn.iocoder.boot.hmdianping.service.shop.ShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * 商铺表 服务实现类
 * </p>
 *
 * @author czl
 * @since 2026-03-07
 */
@Service
@Slf4j
public class ShopServiceImpl extends ServiceImpl<ShopMapper, ShopDO> implements ShopService {

    @Autowired
    private ShopMapper shopMapper;

    @Autowired
    private ShopRedisDAO shopRedisDAO;

    @Autowired
    private RBloomFilter<Long> shopBloomFilter;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private CacheClient cacheClient;

    @Override
    @Cacheable(value = "shopCache", key = "'id:' + #id", unless = "#result == null")
    public ShopDO selectById(Long id) {
        return cacheClient.queryWithMutexAndBloom(
                "shop:", id, ShopDO.class,
                shopBloomFilter::contains, shopMapper::selectById
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer saveShop(ShopDO shopDO) {
        Integer nums = shopMapper.insert(shopDO);
        shopBloomFilter.add(shopDO.getId());
        shopRedisDAO.set(shopDO);
        return nums;
    }

    @Override
    @CacheEvict(value = "shopCache", key = "'id:' + #id")
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteById(Long id) {
        this.removeById(id);
        shopRedisDAO.remove(id);
        return Boolean.TRUE;
    }
}