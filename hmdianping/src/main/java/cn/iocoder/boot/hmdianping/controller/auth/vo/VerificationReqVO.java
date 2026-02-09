package cn.iocoder.boot.hmdianping.controller.auth.vo;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "验证码校验 Request VO")
@Data
public class VerificationReqVO {

    @Schema(description = "验证码", required = true, example = "123456")
    @NotNull(message = "验证码不能为空")
    private String verificationCode;
}
