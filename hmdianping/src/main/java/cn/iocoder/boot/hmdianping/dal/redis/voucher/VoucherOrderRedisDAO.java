package cn.iocoder.boot.hmdianping.dal.redis.voucher;


import cn.hutool.core.util.StrUtil;
import cn.iocoder.boot.framework.common.exception.ServiceException;
import cn.iocoder.boot.framework.common.util.JsonUtils;
import cn.iocoder.boot.hmdianping.dal.dataobject.voucher.VoucherOrderDO;
import cn.iocoder.boot.hmdianping.enums.RedisKeyConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Repository
public class VoucherOrderRedisDAO {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final DefaultRedisScript<Long> SECKILL_VOUCHER_ORDER_SCRIPT;

    static {
        SECKILL_VOUCHER_ORDER_SCRIPT = new DefaultRedisScript<>();
        SECKILL_VOUCHER_ORDER_SCRIPT.setLocation(new ClassPathResource("seckill.lua"));
        SECKILL_VOUCHER_ORDER_SCRIPT.setResultType(Long.class);
    }

    /**
     * 存入 List 集合
     */
    public void setList(Long userId, Long voucherId, List<VoucherOrderDO> list) {
        String key = formatKey(userId, voucherId);
        // 将整个 List 序列化为 JSON 存入
        stringRedisTemplate.opsForValue().set(key, JsonUtils.toJsonString(list));
    }

    /**
     * 读取 List 集合
     */
    public List<VoucherOrderDO> getList(Long userId, Long voucherId) {
        String key = formatKey(userId, voucherId);
        String json = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isEmpty(json)) {
            return Collections.emptyList();
        }
        // 使用 JsonUtils 解析为 List 集合
        return JsonUtils.parseArray(json, VoucherOrderDO.class);
    }

    /**
     * 向现有 List 中追加一个订单
     */
    public void addToList(Long userId, Long voucherId, VoucherOrderDO order) {
        List<VoucherOrderDO> list = new ArrayList<>(getList(userId, voucherId));
        list.add(order);
        setList(userId, voucherId, list);
    }

    public Long executeSeckillVoucherOrderLua(Long voucherId, Long userId) throws ServiceException {
        List<String> keys = Collections.singletonList(String.valueOf(voucherId));
        Long result = stringRedisTemplate.execute(SECKILL_VOUCHER_ORDER_SCRIPT, keys, String.valueOf(userId));
        if (result == 1L) {
            throw new ServiceException(500, "库存不足！");
        } else if (result == 2L) {
            throw new ServiceException(500, "不允许重复下单！");
        }
        log.info("执行秒杀订单 Lua 脚本成功，voucherId：{}，userId：{}", voucherId, userId);
        return result;
    }

    private String formatKey(Long userId, Long voucherId) {
        return String.format(RedisKeyConstants.VOUCHER_ORDER_BY_USERID_VOUCHERID, userId, voucherId);
    }
}