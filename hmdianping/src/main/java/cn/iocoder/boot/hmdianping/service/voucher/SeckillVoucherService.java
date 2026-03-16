package cn.iocoder.boot.hmdianping.service.voucher;

import cn.iocoder.boot.hmdianping.controller.voucher.vo.SeckillVoucherVO;
import cn.iocoder.boot.hmdianping.dal.dataobject.voucher.SeckillVoucherDO;
import cn.iocoder.boot.hmdianping.dal.dataobject.voucher.VoucherDO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* <p>
    * 秒杀优惠券表，与优惠券表是一对一关系 服务类
    * </p>
*
* @author czl
* @since 2026-03-08
*/
public interface SeckillVoucherService extends IService<SeckillVoucherDO> {
    public void addSeckillVoucher(SeckillVoucherVO seckillVoucherVO);
}