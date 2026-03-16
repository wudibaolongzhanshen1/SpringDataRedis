package cn.iocoder.boot.hmdianping.controller.voucher;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import cn.iocoder.boot.hmdianping.service.voucher.VoucherOrderService;
import jakarta.annotation.Resource;

/**
 * <p>
 * 优惠券订单表 前端控制器
 * </p>
 *
 * @author czl
 * @since 2026-03-08
 */
@Tag(name = "优惠券订单表", description = "管理 优惠券订单表 相关接口")
@RestController
@RequestMapping("/voucherOrder")
public class VoucherOrderController {

    @Resource
    private VoucherOrderService voucherOrderService;

    @PostMapping("/createSeckillVoucherOrder")
    @Operation(summary = "秒杀下单优惠券，一人一单", description = "秒杀下单优惠券，一人一单")
    public void createSeckillVoucherOrder(Long voucherId) throws Exception {
        voucherOrderService.seckillVoucherLua(voucherId);
    }

}