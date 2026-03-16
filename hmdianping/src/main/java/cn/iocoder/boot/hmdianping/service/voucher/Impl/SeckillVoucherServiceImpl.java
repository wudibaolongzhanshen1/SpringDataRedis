package cn.iocoder.boot.hmdianping.service.voucher.Impl;

import cn.iocoder.boot.hmdianping.controller.voucher.vo.SeckillVoucherVO;
import cn.iocoder.boot.hmdianping.dal.dataobject.voucher.SeckillVoucherDO;
import cn.iocoder.boot.hmdianping.dal.dataobject.voucher.VoucherDO;
import cn.iocoder.boot.hmdianping.dal.mysql.voucher.SeckillVoucherMapper;
import cn.iocoder.boot.hmdianping.service.voucher.SeckillVoucherService;
import cn.iocoder.boot.hmdianping.service.voucher.VoucherService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 秒杀优惠券表，与优惠券表是一对一关系 服务实现类
 * </p>
 *
 * @author czl
 * @since 2026-03-08
 */
@Service
public class SeckillVoucherServiceImpl extends ServiceImpl<SeckillVoucherMapper, SeckillVoucherDO> implements SeckillVoucherService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private VoucherService voucherService;

    @Override
    public void addSeckillVoucher(SeckillVoucherVO seckillVoucherVO) {
        VoucherDO voucherDO = VoucherDO.builder().id(seckillVoucherVO.getVoucherId()).rules(seckillVoucherVO.getRules())
                .title(seckillVoucherVO.getTitle()).actualValue(seckillVoucherVO.getActualValue())
                .payValue(seckillVoucherVO.getPayValue()).status(seckillVoucherVO.getStatus())
                .shopId(seckillVoucherVO.getShopId()).subTitle(seckillVoucherVO.getSubTitle()).build();
        voucherService.save(voucherDO);
        SeckillVoucherDO seckillVoucherDO = SeckillVoucherDO.builder().voucherId(seckillVoucherVO.getVoucherId())
                .stock(seckillVoucherVO.getStock()).beginTime(seckillVoucherVO.getBeginTime())
                .endTime(seckillVoucherVO.getEndTime()).build();
        this.save(seckillVoucherDO);
        // 将库存信息存入 Redis，使用 voucherId 作为 key，stock 作为 value
        stringRedisTemplate.opsForValue().set("seckill:stock:" + voucherDO.getId(), String.valueOf(seckillVoucherVO.getStock()));

    }
}