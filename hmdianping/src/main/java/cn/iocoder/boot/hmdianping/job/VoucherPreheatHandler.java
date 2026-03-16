package cn.iocoder.boot.hmdianping.job;

import cn.iocoder.boot.framework.job.core.handler.JobHandler;
import cn.iocoder.boot.hmdianping.dal.dataobject.voucher.SeckillVoucherDO;
import cn.iocoder.boot.hmdianping.dal.dataobject.voucher.VoucherDO;
import cn.iocoder.boot.hmdianping.dal.dataobject.voucher.VoucherOrderDO;
import cn.iocoder.boot.hmdianping.dal.mysql.voucher.SeckillVoucherMapper;
import cn.iocoder.boot.hmdianping.dal.mysql.voucher.VoucherMapper;
import cn.iocoder.boot.hmdianping.dal.mysql.voucher.VoucherOrderMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component("voucherPreheatHandler")
@Slf4j
public class VoucherPreheatHandler implements JobHandler {

    @Resource
    private VoucherMapper voucherMapper;
    @Resource
    private SeckillVoucherMapper seckillVoucherMapper;
    @Resource
    private VoucherOrderMapper voucherOrderMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public String execute(String param) throws Exception {
        log.info("[voucherPreheatHandler][开始执行预热，参数：{}]", param);
//        // 1. 预热普通优惠券 (Voucher)
//        List<VoucherDO> vouchers = voucherMapper.selectList(null);
//        for (VoucherDO v : vouchers) {
//            stringRedisTemplate.opsForValue().set("seckill:voucher:" + v.getId(), JSON.writeValueAsString(v));
//        }
        // 2. 预热秒杀券库存 (SeckillVoucher) -> 核心业务：通常存入 String 类型的库存计数器
        List<SeckillVoucherDO> seckillVouchers = seckillVoucherMapper.selectList(null);
        for (SeckillVoucherDO sv : seckillVouchers) {
            // 存入库存，方便 Lua 脚本扣减
            stringRedisTemplate.opsForValue().set("seckill:stock:" + sv.getVoucherId(), sv.getStock().toString());
        }
        // 3. 预热订单信息 (VoucherOrder) -> 比如存入已购买的用户名单，防止超卖
        // 注意：全表同步订单到 Redis 压力很大，通常只同步“进行中”或“待支付”的订单
        List<VoucherOrderDO> orders = voucherOrderMapper.selectList(new LambdaQueryWrapper<VoucherOrderDO>()
                .gt(VoucherOrderDO::getCreateTime, LocalDateTime.now().minusDays(1))); // 仅同步近1天的
        for (VoucherOrderDO order : orders) {
            stringRedisTemplate.opsForSet().add("seckill:order:" + order.getVoucherId(), order.getUserId().toString());
        }

        return "预热成功：Seckill优惠券 " + seckillVouchers.size() + " 条 " + "订单 " + orders.size() + " 条";
    }
}