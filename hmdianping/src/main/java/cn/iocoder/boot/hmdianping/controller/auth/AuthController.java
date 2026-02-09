package cn.iocoder.boot.hmdianping.controller.auth;


import cn.iocoder.boot.framework.common.pojo.CommonResult;
import cn.iocoder.boot.hmdianping.controller.auth.vo.AuthLoginReqVO;
import cn.iocoder.boot.hmdianping.controller.auth.vo.AuthLoginRespVO;
import cn.iocoder.boot.hmdianping.service.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "认证服务", description = "用户登录相关接口")
@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @Operation(summary = "根据手机号和验证码进行用户登录")
    @PermitAll
    @PostMapping("/login")
    public CommonResult<AuthLoginRespVO> login(@RequestBody @Valid AuthLoginReqVO authLoginReqVO) {
        return CommonResult.success(authService.login(authLoginReqVO));
    }
}
