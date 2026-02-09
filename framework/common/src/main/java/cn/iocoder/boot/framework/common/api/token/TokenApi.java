package cn.iocoder.boot.framework.common.api.token;

import cn.iocoder.boot.framework.common.dto.token.AccessTokenCheckRespDTO;
import cn.iocoder.boot.framework.common.dto.token.RefreshTokenRespDTO;


public interface TokenApi {
    AccessTokenCheckRespDTO createAccessToken(Long userId);

    RefreshTokenRespDTO createRefreshToken(Long userId);

    AccessTokenCheckRespDTO getAccessToken(String accessToken);

    AccessTokenCheckRespDTO checkAccessToken(String accessToken);
}
