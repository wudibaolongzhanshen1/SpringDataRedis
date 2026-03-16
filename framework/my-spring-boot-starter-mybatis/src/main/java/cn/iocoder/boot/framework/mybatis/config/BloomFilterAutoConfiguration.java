package cn.iocoder.boot.framework.mybatis.config;


import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnMissingBean(RBloomFilter.class)
public class BloomFilterAutoConfiguration {
    @Bean
    public RBloomFilter<Long> shopBloomFilter(RedissonClient redissonClient) {
        RBloomFilter<Long> bloomFilter = redissonClient.getBloomFilter("shop:bloom:filter");
        // 初始化：预期插入量为 100w，误差率为 0.03
        bloomFilter.tryInit(1000000L, 0.03);
        return bloomFilter;
    }

    @Bean
    public RBloomFilter<Long> followBloomFilter(RedissonClient redissonClient) {
        RBloomFilter<Long> bloomFilter = redissonClient.getBloomFilter("follow:bloom:filter");
        // 初始化：预期插入量为 100w，误差率为 0.03
        bloomFilter.tryInit(1000000L, 0.03);
        return bloomFilter;
    }

    @Bean
    public RBloomFilter<Long> blogBloomFilter(RedissonClient redissonClient) {
        RBloomFilter<Long> bloomFilter = redissonClient.getBloomFilter("blog:bloom:filter");
        // 初始化：预期插入量为 100w，误差率为 0.03
        bloomFilter.tryInit(1000000L, 0.03);
        return bloomFilter;
    }
}
