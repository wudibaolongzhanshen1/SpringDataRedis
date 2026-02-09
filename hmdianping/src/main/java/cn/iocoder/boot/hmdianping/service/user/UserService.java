package cn.iocoder.boot.hmdianping.service.user;

import cn.iocoder.boot.hmdianping.dal.dataobject.user.UserDO;

public interface UserService {
    UserDO getUserByPhone(String phone);

    Long createUser(String phone);

    Boolean validatePhoneLegal(String phone);
}
