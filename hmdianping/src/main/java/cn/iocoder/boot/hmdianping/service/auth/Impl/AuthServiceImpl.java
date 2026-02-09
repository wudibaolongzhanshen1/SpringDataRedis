package cn.iocoder.boot.hmdianping.service.auth.Impl;

import cn.iocoder.boot.framework.common.api.sms.SmsApi;
import cn.iocoder.boot.hmdianping.controller.auth.vo.AuthLoginReqVO;
import cn.iocoder.boot.hmdianping.controller.auth.vo.AuthLoginRespVO;
import cn.iocoder.boot.hmdianping.convert.auth.AuthConvert;
import cn.iocoder.boot.hmdianping.dal.dataobject.token.AccessTokenDO;
import cn.iocoder.boot.hmdianping.dal.dataobject.user.UserDO;
import cn.iocoder.boot.hmdianping.service.auth.AuthService;
import cn.iocoder.boot.hmdianping.service.token.TokenService;
import cn.iocoder.boot.hmdianping.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserService userService;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private SmsApi smsApi;

    @Override
    public UserDO authenticate(String phone) {
        UserDO user = userService.getUserByPhone(phone);
        return user;
    }

    @Override
    public AuthLoginRespVO login(AuthLoginReqVO authLoginReqVO) {
        // 校验验证码
        smsApi.validSmsCode(authLoginReqVO.getPhone(), authLoginReqVO.getVerificationCode());
        // 认证用户
        UserDO user = this.authenticate(authLoginReqVO.getPhone());
        if (user == null) {
            // 用户不存在，创建用户
            Long userId = userService.createUser(authLoginReqVO.getPhone());
            user = userService.getUserByPhone(authLoginReqVO.getPhone());
        }
        AccessTokenDO accessToken = tokenService.createAccessToken(user.getId());
        return AuthConvert.INSTANCE.convert(accessToken);
    }
}
