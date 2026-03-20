package cn.iocoder.boot.framework.security.core.filter;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.boot.framework.common.api.token.TokenApi;
import cn.iocoder.boot.framework.common.api.user.UserApi;
import cn.iocoder.boot.framework.common.dto.user.UserDTO;
import cn.iocoder.boot.framework.common.exception.ServiceException;
import cn.iocoder.boot.framework.common.pojo.CommonResult;
import cn.iocoder.boot.framework.common.util.ServletUtils;
import cn.iocoder.boot.framework.security.config.SecurityProperties;
import cn.iocoder.boot.framework.security.core.LoginUser;
import cn.iocoder.boot.framework.common.dto.token.AccessTokenCheckRespDTO;
import cn.iocoder.boot.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.boot.framework.web.core.handler.GlobalExceptionHandler;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@RequiredArgsConstructor
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private final SecurityProperties securityProperties;
    private final GlobalExceptionHandler globalExceptionHandler;
    public static final String AUTHORIZATION_BEARER = "Bearer";
    public static final String AUTHORIZATION_MOCK = "Mock";
    @Resource
    private TokenApi tokenApi;
    @Resource
    private UserApi userApi;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        System.out.println("TokenAuthenticationFilter 过滤器被调用了");
        String headerName = securityProperties.getTokenHeader();
        String token = request.getHeader(headerName);
        if (StrUtil.isEmpty(token)) {
            token = request.getParameter(headerName);
        }
        // 1. 如果完全没有 Token，直接放行
        if (StrUtil.isEmpty(token)) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            LoginUser loginUser = null;
            // 2. 识别并处理 Mock 逻辑
            if (token.startsWith(AUTHORIZATION_MOCK + " ")) {
                // Mock 开头，剥离 "Mock " (长度为 5)
                String mockToken = token.substring(AUTHORIZATION_MOCK.length() + 1).trim();
                loginUser = mockLoginUser(request, mockToken);
            }
            // 3. 识别并处理标准的 Bearer 逻辑
            else {
                // 兼容你原来的逻辑：如果是 Bearer 开头则剥离，否则视为原样 Token
                int index = token.indexOf(AUTHORIZATION_BEARER + " ");
                String realToken = index >= 0 ? token.substring(index + 7).trim() : token;
                loginUser = buildLoginUserByToken(realToken);
            }
            // 4. 如果成功获取到用户（无论是 Mock 的还是真实的），注入 Security 上下文
            if (loginUser != null) {
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        loginUser, null, Collections.emptyList());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                SecurityFrameworkUtils.setLoginUser(loginUser, request);
            }
        } catch (Throwable e) {
            // 5. 认证过程出错（如 Token 伪造等），返回 JSON 报错
            CommonResult<?> result = globalExceptionHandler.allExceptionHandler(request, e);
            ServletUtils.writeJSON(response, result);
            return;
        }
        // 6. 最后统一放行（无论有没有注入用户信息，Security 后续的 Filter 会根据路径配置决定是否允许匿名访问）
        filterChain.doFilter(request, response);
    }

    private LoginUser buildLoginUserByToken(String token) {
        try {
            AccessTokenCheckRespDTO accessToken = tokenApi.checkAccessToken(token);
            if (accessToken == null) {
                return null;
            }
            // 构建登录用户
            return new LoginUser().setId(accessToken.getUserId()).setInfo(accessToken.getUserInfo()) // 额外的用户信息
                    .setExpiresTime(accessToken.getExpiresTime());
        } catch (ServiceException serviceException) {
            // 校验 Token 不通过时，考虑到一些接口是无需登录的，所以直接返回 null 即可
            return null;
        }
    }

    /**
     * 模拟登录用户，方便日常开发调试
     * <p>
     * 注意，在线上环境下，一定要关闭该功能！！！
     *
     * @param request 请求
     * @param token   模拟的 token，格式为 {@link SecurityProperties#getMockSecret()} + 用户编号
     * @return 模拟的 LoginUser
     */
    private LoginUser mockLoginUser(HttpServletRequest request, String token) {
        if (!securityProperties.getMockEnable()) {
            return null;
        }
        // 必须以 mockSecret 开头
        if (!token.startsWith(securityProperties.getMockSecret())) {
            return null;
        }
        // 构建模拟用户
        Long userId = Long.valueOf(token.substring(securityProperties.getMockSecret().length()));
        UserDTO userDTO = userApi.selectById(userId);
        if (userDTO == null) {
            throw new ServiceException(500, "模拟登录失败，用户不存在");
        }
        return new LoginUser().setId(userId);
    }


}
