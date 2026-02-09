package cn.iocoder.boot.hmdianping.controller.sms;


import cn.iocoder.boot.hmdianping.service.sms.SmsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "短信服务", description = "短信发送相关接口")
@RestController
@RequestMapping("/sms")
public class SmsController {
    @Resource
    private SmsService smsService;

    @Operation(summary = "通过手机号发送短信验证码")
    @Parameters({
            @Parameter(name = "phone", description = "手机号", required = true, example = "13800138000")
    })
    @PermitAll
    @PostMapping("/sendCode")
    public void sendCode(@RequestParam("phone") String phone) {
        smsService.sendSmsCode(phone);
    }
}
