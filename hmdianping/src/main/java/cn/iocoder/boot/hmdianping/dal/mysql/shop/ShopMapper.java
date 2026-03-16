package cn.iocoder.boot.hmdianping.dal.mysql.shop;

import cn.iocoder.boot.hmdianping.dal.dataobject.shop.ShopDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* 商铺表 Mapper 接口
*/
@Mapper
public interface ShopMapper extends BaseMapper<ShopDO> {

    default List<ShopDO> selectShopList(@Param("shopName") String shopName) {
        return selectList(Wrappers.lambdaQuery(ShopDO.class).eq(ShopDO::getName, shopName));
    }

    default ShopDO selectOne(Integer id){
        return selectOne(Wrappers.lambdaQuery(ShopDO.class).eq(ShopDO::getId, id));
    }

}