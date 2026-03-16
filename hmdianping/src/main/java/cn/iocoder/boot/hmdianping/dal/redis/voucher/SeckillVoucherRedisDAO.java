package cn.iocoder.boot.hmdianping.dal.redis.voucher;


import cn.iocoder.boot.framework.common.util.JsonUtils;
import cn.iocoder.boot.hmdianping.dal.dataobject.voucher.SeckillVoucherDO;
import cn.iocoder.boot.hmdianping.enums.RedisKeyConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
public class SeckillVoucherRedisDAO {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public void setSeckillVoucher(SeckillVoucherDO seckillVoucherDO) {
        String key = formatKey(seckillVoucherDO.getVoucherId());
        stringRedisTemplate.opsForValue().set(key, JsonUtils.toJsonString(seckillVoucherDO));
    }

    public SeckillVoucherDO getSeckillVoucher(Long voucherId) {
        String key = formatKey(voucherId);
        String seckillVoucherS = stringRedisTemplate.opsForValue().get(key);
        if (seckillVoucherS == null) {
            return null;
        }
        return JsonUtils.parseObject(seckillVoucherS, SeckillVoucherDO.class);
    }

    private String formatKey(Long voucherId) {
        return String.format(RedisKeyConstants.SECKIL_VOUCHER, voucherId);
    }
}
