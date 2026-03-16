package cn.iocoder.boot.hmdianping.controller.voucher;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import cn.iocoder.boot.hmdianping.service.voucher.VoucherService;
import jakarta.annotation.Resource;

/**
* <p>
    * 优惠券表 前端控制器
    * </p>
*
* @author czl
* @since 2026-03-08
*/
@Tag(name = "优惠券表", description = "管理 优惠券表 相关接口")
@RestController
@RequestMapping("/voucherdo")
public class VoucherController {

    @Resource
    private VoucherService voucherService;

}