package cn.iocoder.boot.hmdianping.controller.voucher;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import cn.iocoder.boot.hmdianping.service.voucher.SeckillVoucherService;
import jakarta.annotation.Resource;

/**
* <p>
    * 秒杀优惠券表，与优惠券表是一对一关系 前端控制器
    * </p>
*
* @author czl
* @since 2026-03-08
*/
@Tag(name = "秒杀优惠券表，与优惠券表是一对一关系", description = "管理 秒杀优惠券表，与优惠券表是一对一关系 相关接口")
@RestController
@RequestMapping("/seckillvoucherdo")
public class SeckillVoucherController {

    @Resource
    private SeckillVoucherService seckillVoucherService;

}