package cn.iocoder.boot.hmdianping.service.voucher;

import cn.iocoder.boot.hmdianping.dal.dataobject.voucher.VoucherOrderDO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* <p>
    * 优惠券订单表 服务类
    * </p>
*
* @author czl
* @since 2026-03-08
*/
public interface VoucherOrderService extends IService<VoucherOrderDO> {

    VoucherOrderDO seckillVoucherLua(Long voucherId) throws Exception;

    public VoucherOrderDO seckillVoucher(Long voucherId) throws Exception;

    public VoucherOrderDO createVoucherOrder(Long voucherId) throws Exception;
}