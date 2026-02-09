package cn.iocoder.boot.framework.security.core.filter;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.boot.framework.common.api.token.TokenApi;
import cn.iocoder.boot.framework.common.exception.ServiceException;
import cn.iocoder.boot.framework.common.pojo.CommonResult;
import cn.iocoder.boot.framework.common.util.ServletUtils;
import cn.iocoder.boot.framework.security.config.SecurityProperties;
import cn.iocoder.boot.framework.security.core.LoginUser;
import cn.iocoder.boot.framework.common.dto.token.AccessTokenCheckRespDTO;
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

    @Resource
    private TokenApi tokenApi;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String headerName = securityProperties.getTokenHeader();
        String token = request.getHeader(headerName);
        if (StrUtil.isEmpty(token)) {
            token = request.getParameter(headerName);
        }
        if (StrUtil.isEmpty(token)) { // 或者使用 token == null
            filterChain.doFilter(request, response); // 直接放行，交给后续的过滤器（如果是放行路径，Security 之后会允许通过）
            return;
        }else if (StringUtils.hasText(token) && !token.startsWith(AUTHORIZATION_BEARER + " ")) {
            // 如果不是以 Bearer 开头（比如是 Basic 开头），直接视为无 Token
            return;
        }
        int index = token.indexOf(AUTHORIZATION_BEARER + " ");
        token = index >= 0 ? token.substring(index + 7).trim() : token;
        if (StrUtil.isNotEmpty(token)) {
            try {
                LoginUser loginUser = buildLoginUserByToken(token);
                if (loginUser != null) {
                    // 创建 UsernamePasswordAuthenticationToken 对象
                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                            loginUser, null, Collections.emptyList());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            } catch (Throwable e) {
                CommonResult<?> result = globalExceptionHandler.allExceptionHandler(request, e);
                ServletUtils.writeJSON(response, result);
                return;
            }
        }
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
        return new LoginUser().setId(userId);
    }


}
