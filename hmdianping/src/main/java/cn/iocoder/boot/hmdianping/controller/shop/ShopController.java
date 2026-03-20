package cn.iocoder.boot.hmdianping.controller.shop;

import cn.iocoder.boot.framework.common.exception.ServiceException;
import cn.iocoder.boot.framework.common.pojo.CommonResult;
import cn.iocoder.boot.hmdianping.dal.dataobject.shop.ShopDO;
import cn.iocoder.boot.hmdianping.service.stat.Impl.StatServiceImpl;
import cn.iocoder.boot.hmdianping.service.stat.StatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;
import cn.iocoder.boot.hmdianping.service.shop.ShopService;
import jakarta.annotation.Resource;

/**
 * <p>
 * 商铺表 前端控制器
 * </p>
 *
 * @author czl
 * @since 2026-03-07
 */
@Tag(name = "商铺表", description = "管理 商铺表 相关接口")
@RestController
@RequestMapping("/shop")
public class ShopController {

    @Resource
    private ShopService shopService;
    @Resource
    private StatService statService;

    @GetMapping("/get")
    @Operation(summary = "根据 id 查询商铺信息", description = "根据 id 查询商铺信息")
    public CommonResult<ShopDO> getShop(@RequestParam(name = "shopId") Long shopId) {
        statService.recordUV("shop", shopId.toString());
        ShopDO shopDO = shopService.selectById(shopId);
        if (shopDO == null) {
            throw new ServiceException(500, "商铺不存在");
        }
        return CommonResult.success(shopDO);
    }


    @DeleteMapping("/delete")
    @Operation(summary = "根据 id 删除商铺信息", description = "根据 id 删除商铺信息")
    public CommonResult<Boolean> deleteShop(@RequestParam(name = "shopId") Long shopId) {
        boolean b = shopService.deleteById(shopId);
        if (!b) {
            throw new ServiceException(500, "商铺不存在");
        }
        return CommonResult.success(b);
    }

    @PostMapping("/create")
    @Operation(summary = "创建商铺信息", description = "创建商铺信息")
    public CommonResult<Integer> createShop(@RequestBody ShopDO shopDO) {
        Integer save = shopService.saveShop(shopDO);
        if (save == 0) {
            throw new ServiceException(500, "商铺创建失败");
        }
        return CommonResult.success(save);
    }
}