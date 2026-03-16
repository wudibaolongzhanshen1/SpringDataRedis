package cn.iocoder.boot.hmdianping.dal.mysql.voucher;

import cn.iocoder.boot.hmdianping.dal.dataobject.voucher.VoucherDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

/**
* 优惠券表 Mapper 接口
*/
@Mapper
public interface VoucherMapper extends BaseMapper<VoucherDO> {

}