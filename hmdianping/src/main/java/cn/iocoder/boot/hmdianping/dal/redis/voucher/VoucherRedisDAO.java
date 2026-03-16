package cn.iocoder.boot.hmdianping.dal.redis.voucher;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
public class VoucherRedisDAO {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;


}
