package cn.iocoder.boot.hmdianping.service.shop;

import cn.iocoder.boot.hmdianping.dal.dataobject.shop.ShopDO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* <p>
    * 商铺表 服务类
    * </p>
*
* @author czl
* @since 2026-03-07
*/
public interface ShopService extends IService<ShopDO> {

    public ShopDO selectById(Long id);

    public Integer saveShop(ShopDO shopDO);

}