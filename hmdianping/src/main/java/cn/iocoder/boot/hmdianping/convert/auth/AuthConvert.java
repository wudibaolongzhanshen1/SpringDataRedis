package cn.iocoder.boot.hmdianping.convert.auth;

import cn.iocoder.boot.hmdianping.controller.auth.vo.AuthLoginRespVO;
import cn.iocoder.boot.hmdianping.dal.dataobject.token.AccessTokenDO;
import org.mapstruct.factory.Mappers;


public interface AuthConvert {

    AuthConvert INSTANCE = Mappers.getMapper(AuthConvert.class);

    AuthLoginRespVO convert(AccessTokenDO bean);

}
