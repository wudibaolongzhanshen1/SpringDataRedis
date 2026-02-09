package cn.iocoder.boot.hmdianping.api.token;

import cn.iocoder.boot.framework.common.dto.token.AccessTokenCheckRespDTO;
import cn.iocoder.boot.framework.common.dto.token.RefreshTokenRespDTO;
import cn.iocoder.boot.framework.common.util.BeanUtils;
import cn.iocoder.boot.framework.common.api.token.TokenApi;
import cn.iocoder.boot.hmdianping.dal.dataobject.token.AccessTokenDO;
import cn.iocoder.boot.hmdianping.dal.dataobject.token.RefreshTokenDO;
import cn.iocoder.boot.hmdianping.service.token.TokenService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;


@Service
public class TokenApiImpl implements TokenApi {

    @Resource
    private TokenService tokenService;

    @Override
    public AccessTokenCheckRespDTO createAccessToken(Long userId) {
        return BeanUtils.toBean(tokenService.createAccessToken(userId),AccessTokenCheckRespDTO.class);
    }

    @Override
    public RefreshTokenRespDTO createRefreshToken(Long userId) {
        return BeanUtils.toBean(tokenService.createRefreshToken(userId), RefreshTokenRespDTO.class);
    }

    @Override
    public AccessTokenCheckRespDTO getAccessToken(String accessToken) {
        return BeanUtils.toBean(tokenService.getAccessToken(accessToken), AccessTokenCheckRespDTO.class);
    }

    @Override
    public AccessTokenCheckRespDTO checkAccessToken(String accessToken) {
        return BeanUtils.toBean(tokenService.checkAccessToken(accessToken), AccessTokenCheckRespDTO.class);
    }
}
