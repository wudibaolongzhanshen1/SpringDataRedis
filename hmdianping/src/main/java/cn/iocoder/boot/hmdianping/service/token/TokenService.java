package cn.iocoder.boot.hmdianping.service.token;

import cn.iocoder.boot.hmdianping.dal.dataobject.token.AccessTokenDO;
import cn.iocoder.boot.hmdianping.dal.dataobject.token.RefreshTokenDO;

public interface TokenService {
    AccessTokenDO createAccessToken(Long userId);

    RefreshTokenDO createRefreshToken(Long userId);

    AccessTokenDO getAccessToken(String accessToken);

    AccessTokenDO checkAccessToken(String accessToken);
}
