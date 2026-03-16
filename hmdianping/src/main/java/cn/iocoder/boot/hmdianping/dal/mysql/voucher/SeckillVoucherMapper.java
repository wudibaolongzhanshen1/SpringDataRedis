package cn.iocoder.boot.hmdianping.dal.mysql.voucher;

import cn.iocoder.boot.hmdianping.dal.dataobject.voucher.SeckillVoucherDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

/**
* 秒杀优惠券表，与优惠券表是一对一关系 Mapper 接口
*/
@Mapper
public interface SeckillVoucherMapper extends BaseMapper<SeckillVoucherDO> {

}