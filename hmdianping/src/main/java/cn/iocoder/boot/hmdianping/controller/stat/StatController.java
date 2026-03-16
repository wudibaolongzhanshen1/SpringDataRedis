package cn.iocoder.boot.hmdianping.controller.stat;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.boot.hmdianping.service.stat.StatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/stat")
@Tag(description = "统计Unique Visitor相关接口", name = "统计相关接口")
public class StatController {

    @Autowired
    private StatService statService;

    /**
     * 查询某商户今天的 UV
     */
    @GetMapping("/uv/{shopId}")
    @Operation(summary = "查询商户 UV", description = "查询商户 UV，date 可选，格式 yyyyMMdd，不传默认为今天")
    public Long getShopUV(@PathVariable("shopId") String shopId,
                            @RequestParam(value = "date", required = false) String date) {
        // 如果不传日期，默认今天
        if (StrUtil.isBlank(date)) {
            date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        }
        return statService.getUVCount("shop", shopId, date);
    }
}