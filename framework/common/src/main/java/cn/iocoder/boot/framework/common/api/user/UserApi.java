package cn.iocoder.boot.framework.common.api.user;

import cn.iocoder.boot.framework.common.dto.user.UserDTO;

public interface UserApi {
    public UserDTO selectById(Long id);
}
