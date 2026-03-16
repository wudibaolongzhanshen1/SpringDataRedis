package cn.iocoder.boot.hmdianping.service.user;

import cn.iocoder.boot.hmdianping.dal.dataobject.user.UserDO;
import com.baomidou.mybatisplus.extension.service.IService;

public interface UserService extends IService<UserDO> {
    UserDO getUserByPhone(String phone);

    Long createUser(String phone);

    Boolean validatePhoneLegal(String phone);

}
