package cn.iocoder.boot.hmdianping.controller.auth.vo;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "用户登录 Request VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthLoginReqVO extends VerificationReqVO {
    @Schema(description = "手机号", required = true, example = "18888888888")
    @NotNull(message = "手机号不能为空")
    private String phone;
}
