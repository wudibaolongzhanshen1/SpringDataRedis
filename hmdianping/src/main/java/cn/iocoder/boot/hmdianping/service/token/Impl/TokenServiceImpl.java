package cn.iocoder.boot.hmdianping.service.token.Impl;


import cn.hutool.core.util.IdUtil;
import cn.iocoder.boot.framework.common.enums.mysql.TokenExpiry;
import cn.iocoder.boot.framework.common.exception.ServiceException;
import cn.iocoder.boot.hmdianping.dal.dataobject.token.AccessTokenDO;
import cn.iocoder.boot.hmdianping.dal.dataobject.token.RefreshTokenDO;
import cn.iocoder.boot.hmdianping.dal.mysql.token.AccessTokenMapper;
import cn.iocoder.boot.hmdianping.dal.mysql.token.RefreshTokenMapper;
import cn.iocoder.boot.hmdianping.dal.redis.token.AccessTokenRedisDAO;
import cn.iocoder.boot.hmdianping.service.token.TokenService;
import org.apache.el.parser.Token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class TokenServiceImpl implements TokenService {

    @Autowired
    private AccessTokenMapper accessTokenMapper;

    @Autowired
    private AccessTokenRedisDAO accessTokenRedisDAO;

    @Autowired
    private RefreshTokenMapper refreshTokenMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AccessTokenDO createAccessToken(Long userId) {
        RefreshTokenDO refreshTokenDO = createRefreshToken(userId);
        AccessTokenDO accessTokenDO = AccessTokenDO.builder()
                .userId(userId)
                .accessToken(generateAccessToken())
                .refreshToken(refreshTokenDO.getRefreshToken())
                .expiresTime(LocalDateTime.now().plusSeconds(TokenExpiry.ACCESS_TOKEN.getExpiry()))
                .build();
        accessTokenMapper.insert(accessTokenDO);
        accessTokenRedisDAO.set(accessTokenDO);
        return accessTokenDO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RefreshTokenDO createRefreshToken(Long userId) {
        RefreshTokenDO refreshTokenDO = RefreshTokenDO.builder()
                .userId(userId)
                .refreshToken(generateRefreshToken())
                .expiresTime(LocalDateTime.now().plusSeconds(TokenExpiry.REFRESH_TOKEN.getExpiry()))
                .build();
        refreshTokenMapper.insert(refreshTokenDO);
        return refreshTokenDO;
    }

    @Override
    public AccessTokenDO getAccessToken(String accessToken) {
        AccessTokenDO accessTokenDO = accessTokenRedisDAO.get(accessToken);
        if (accessTokenDO != null) {
            return accessTokenDO;
        }
        accessTokenDO = accessTokenMapper.selectByAccessToken(accessToken);
        if (accessTokenDO != null) {
            accessTokenRedisDAO.set(accessTokenDO);
        }
        RefreshTokenDO refreshTokenDO = refreshTokenMapper.selectByRefreshToken(accessToken);
        if (refreshTokenDO != null && !LocalDateTime.now().isAfter(refreshTokenDO.getExpiresTime())) {
            String accessTokenStr = generateAccessToken();
            accessTokenDO = AccessTokenDO.builder()
                    .userId(refreshTokenDO.getUserId())
                    .accessToken(accessTokenStr)
                    .refreshToken(refreshTokenDO.getRefreshToken())
                    .build();
            accessTokenMapper.insert(accessTokenDO);
            accessTokenRedisDAO.set(accessTokenDO);
        }
        return accessTokenDO;
    }

    @Override
    public AccessTokenDO checkAccessToken(String accessToken) {
        AccessTokenDO accessTokenDO = getAccessToken(accessToken);
        if (accessTokenDO == null) {
            throw new ServiceException(500, "Access Token 不存在");
        }
        if (accessTokenDO.getExpiresTime().isBefore(LocalDateTime.now())) {
            throw new ServiceException(500, "Access Token 已过期");
        }
        return accessTokenDO;
    }

    private static String generateRefreshToken() {
        return IdUtil.fastSimpleUUID();
    }

    private static String generateAccessToken() {
        return IdUtil.fastSimpleUUID();
    }
}
