package cn.iocoder.boot.hmdianping.controller.auth;


import cn.iocoder.boot.framework.common.pojo.CommonResult;
import cn.iocoder.boot.hmdianping.controller.auth.vo.AuthLoginReqVO;
import cn.iocoder.boot.hmdianping.controller.auth.vo.AuthLoginRespVO;
import cn.iocoder.boot.hmdianping.service.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.config.AuthDefaultSource;
import me.zhyd.oauth.config.AuthSource;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.AuthGiteeRequest;
import me.zhyd.oauth.request.AuthRequest;
import me.zhyd.oauth.utils.AuthStateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

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

    // 建议封装成一个 Factory，根据 type 获取不同的实例
    private AuthRequest getAuthRequest(String source) {
        AuthSource authSource = AuthDefaultSource.valueOf(source.toUpperCase());
        return new AuthGiteeRequest(AuthConfig.builder()
                .clientId("你的ClientId")
                .clientSecret("你的ClientSecret")
                .redirectUri("http://localhost:8080/auth/callback/gitee")
                .build());
    }

    /**
     * 1. 渲染授权地址
     * 用户点击“Gitee登录”后跳转到这里
     */
    @GetMapping("/render/{source}")
    public void renderAuth(@PathVariable("source") String source, HttpServletResponse response) throws IOException {
        AuthRequest authRequest = getAuthRequest(source);
        // 生成授权地址并重定向
        String authorizeUrl = authRequest.authorize(AuthStateUtils.createState());
        response.sendRedirect(authorizeUrl);
    }

    /**
     * 2. 回调接口
     * 系统 B 携带 CODE 重定向回来的地方
     */
    @GetMapping("/callback/{source}")
    public CommonResult<AuthUser> login(@PathVariable("source") String source, AuthCallback callback) {
        AuthRequest authRequest = getAuthRequest(source);
        // 这一步 JustAuth 内部自动完成了：CODE 换 Token -> Token 换用户信息
        AuthResponse<AuthUser> response = authRequest.login(callback);
        if (response.ok()) {
            AuthUser authUser = response.getData();
            // 拿到三方用户信息后的业务逻辑
            return CommonResult.success(authUser);
        } else {
            return CommonResult.error(500, "登录失败，无法获取用户信息");
        }
    }
}
