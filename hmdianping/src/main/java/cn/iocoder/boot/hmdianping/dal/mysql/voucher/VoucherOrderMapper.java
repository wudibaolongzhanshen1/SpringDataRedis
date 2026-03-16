package cn.iocoder.boot.hmdianping.dal.mysql.voucher;

import cn.iocoder.boot.hmdianping.dal.dataobject.voucher.VoucherOrderDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

/**
* 优惠券订单表 Mapper 接口
*/
@Mapper
public interface VoucherOrderMapper extends BaseMapper<VoucherOrderDO> {

    default List<VoucherOrderDO> selectByUserIdAndVoucherId(Long userId, Long voucherId) {
        return this.selectList(Wrappers.<VoucherOrderDO>lambdaQuery()
                .eq(VoucherOrderDO::getUserId, userId)
                .eq(VoucherOrderDO::getVoucherId, voucherId));
    }

}