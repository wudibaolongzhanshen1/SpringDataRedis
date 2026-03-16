package cn.iocoder.boot.hmdianping.controller.shop;

import cn.iocoder.boot.framework.common.pojo.CommonResult;
import cn.iocoder.boot.hmdianping.dal.dataobject.shop.ShopDO;
import cn.iocoder.boot.hmdianping.service.stat.Impl.StatServiceImpl;
import cn.iocoder.boot.hmdianping.service.stat.StatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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
        return CommonResult.success(shopService.selectById(shopId));
    }

}