package cn.iocoder.boot.hmdianping.convert.auth;

import cn.iocoder.boot.hmdianping.controller.auth.vo.AuthLoginRespVO;
import cn.iocoder.boot.hmdianping.dal.dataobject.token.AccessTokenDO;

public class AuthConvertImpl implements AuthConvert{
    @Override
    public AuthLoginRespVO convert(AccessTokenDO bean) {
        if ( bean == null ) {
            return null;
        }
        AuthLoginRespVO.AuthLoginRespVOBuilder authLoginRespVO = AuthLoginRespVO.builder();
        authLoginRespVO.userId( bean.getUserId() );
        authLoginRespVO.accessToken( bean.getAccessToken() );
        authLoginRespVO.refreshToken( bean.getRefreshToken() );
        authLoginRespVO.expiresTime( bean.getExpiresTime() );
        return authLoginRespVO.build();
    }
}
