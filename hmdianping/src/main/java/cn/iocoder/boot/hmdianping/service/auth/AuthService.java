package cn.iocoder.boot.hmdianping.service.auth;


import cn.iocoder.boot.hmdianping.controller.auth.vo.AuthLoginReqVO;
import cn.iocoder.boot.hmdianping.controller.auth.vo.AuthLoginRespVO;
import cn.iocoder.boot.hmdianping.dal.dataobject.user.UserDO;

public interface AuthService {

    UserDO authenticate(String phone);

    AuthLoginRespVO login(AuthLoginReqVO authLoginReqVO);


}
