package cn.iocoder.boot.hmdianping.service.voucher.Impl;

import cn.iocoder.boot.framework.common.exception.ServiceException;
import cn.iocoder.boot.framework.redis.core.RedisIdWorker;
import cn.iocoder.boot.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.boot.hmdianping.dal.dataobject.voucher.SeckillVoucherDO;
import cn.iocoder.boot.hmdianping.dal.dataobject.voucher.VoucherOrderDO;
import cn.iocoder.boot.hmdianping.dal.mysql.voucher.SeckillVoucherMapper;
import cn.iocoder.boot.hmdianping.dal.mysql.voucher.VoucherMapper;
import cn.iocoder.boot.hmdianping.dal.mysql.voucher.VoucherOrderMapper;
import cn.iocoder.boot.hmdianping.dal.redis.voucher.SeckillVoucherRedisDAO;
import cn.iocoder.boot.hmdianping.dal.redis.voucher.VoucherOrderRedisDAO;
import cn.iocoder.boot.hmdianping.enums.rocketmq.VoucherOrderMqConstants;
import cn.iocoder.boot.hmdianping.mq.message.VoucherOrderMessage;
import cn.iocoder.boot.hmdianping.mq.producer.VoucherOrderProducer;
import cn.iocoder.boot.hmdianping.service.voucher.SeckillVoucherService;
import cn.iocoder.boot.hmdianping.service.voucher.VoucherOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 优惠券订单表 服务实现类
 * </p>
 *
 * @author czl
 * @since 2026-03-08
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrderDO> implements VoucherOrderService {

    @Autowired
    private VoucherOrderMapper voucherOrderMapper;

    @Autowired
    private VoucherMapper voucherMapper;

    @Autowired
    private SeckillVoucherMapper seckillVoucherMapper;

    @Autowired
    private SeckillVoucherService seckillVoucherService;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RedisIdWorker redisIdWorker;

    @Autowired
    private SeckillVoucherRedisDAO seckillVoucherRedisDAO;

    @Autowired
    private VoucherOrderRedisDAO voucherOrderRedisDAO;

    private final VoucherOrderProducer voucherOrderProducer;

    @Override
    public VoucherOrderDO seckillVoucherLua(Long voucherId) {
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        Long orderId = redisIdWorker.nextId("voucher:order");
        TransactionSendResult transactionSendResult = voucherOrderProducer.sendSeckillOrderTransaction(orderId, voucherId, loginUserId);
        if (transactionSendResult.getLocalTransactionState() != LocalTransactionState.COMMIT_MESSAGE) {
            throw new ServiceException(500, "秒杀失败！");
        }
        // 返回订单id
        return VoucherOrderDO.builder().voucherId(voucherId).id(orderId).userId(loginUserId).build();
    }


    @Override
    public VoucherOrderDO seckillVoucher(Long voucherId) throws Exception {
        SeckillVoucherDO seckillVoucherDO = seckillVoucherRedisDAO.getSeckillVoucher(voucherId);
        if (seckillVoucherDO == null) seckillVoucherDO = seckillVoucherMapper.selectById(voucherId);
        if (seckillVoucherDO == null) {
            throw new ServiceException(500, "秒杀券不存在！");
        }
        seckillVoucherRedisDAO.setSeckillVoucher(seckillVoucherDO);
        LocalDateTime beginTime = seckillVoucherDO.getBeginTime();
        LocalDateTime endTime = seckillVoucherDO.getEndTime();
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(beginTime)) {
            throw new ServiceException(500, "秒杀尚未开始！");
        }
        if (now.isAfter(endTime)) {
            throw new ServiceException(500, "秒杀已经结束！");
        }
        if (seckillVoucherDO.getStock() < 1) {
            throw new ServiceException(500, "库存不足！");
        }
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        // 扣减库存，一人一单
        RLock lock = redissonClient.getLock("lock:seckill:" + voucherId + ":" + loginUserId);
        boolean isLock = lock.tryLock(5, TimeUnit.SECONDS);
        if (!isLock) {
            throw new ServiceException(500, "不允许重复下单！");
        }
        try {
            VoucherOrderService voucherOrderServiceProxy = (VoucherOrderService) AopContext.currentProxy();
            return voucherOrderServiceProxy.createVoucherOrder(voucherId);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public VoucherOrderDO createVoucherOrder(Long voucherId) throws Exception {
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        List<VoucherOrderDO> voucherOrderDOS = voucherOrderRedisDAO.getList(loginUserId, voucherId);
        if (voucherOrderDOS == null || voucherOrderDOS.isEmpty())
            voucherOrderDOS = voucherOrderMapper.selectByUserIdAndVoucherId(loginUserId, voucherId);
        if (!voucherOrderDOS.isEmpty()) {
            throw new ServiceException(500, "不允许重复下单！");
        }
//        voucherOrderRedisDAO.setList(loginUserId, voucherId, voucherOrderDOS);
        boolean success = seckillVoucherService.update().setSql("stock = stock - 1").
                eq("voucher_id", voucherId).
                gt("stock", 0).
                update();
        if (!success) {
            throw new ServiceException(500, "库存不足！");
        }
        // 创建订单
        VoucherOrderDO voucherOrderDO = new VoucherOrderDO();
        voucherOrderDO.setId(redisIdWorker.nextId("voucher:order"));
        voucherOrderDO.setVoucherId(voucherId);
        voucherOrderDO.setUserId(loginUserId);
        voucherOrderDO.setCreateTime(LocalDateTime.now());
        voucherOrderMapper.insert(voucherOrderDO);
        return voucherOrderDO;
    }
}