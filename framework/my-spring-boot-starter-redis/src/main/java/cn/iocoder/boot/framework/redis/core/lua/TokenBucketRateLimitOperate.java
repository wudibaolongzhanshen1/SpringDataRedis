package cn.iocoder.boot.framework.redis.core.lua;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import java.util.List;

@Slf4j
public class TokenBucketRateLimitOperate {

    private final StringRedisTemplate stringRedisTemplate;

    public TokenBucketRateLimitOperate(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    private DefaultRedisScript<Long> redisScript;

    @PostConstruct
    public void init(){
        try {
            redisScript = new DefaultRedisScript<>();
            redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("tokenBucket.lua")));
            redisScript.setResultType(Long.class);
        } catch (Exception e) {
            log.error("TokenBucketRateLimitOperate init lua error", e);
        }
    }

    public Long execute(List<String> keys, String[] args){
        return stringRedisTemplate.execute(redisScript, keys, args);
    }
}