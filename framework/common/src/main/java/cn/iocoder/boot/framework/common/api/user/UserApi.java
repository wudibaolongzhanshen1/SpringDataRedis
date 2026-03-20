package cn.iocoder.boot.framework.common.api.user;

import cn.iocoder.boot.framework.common.dto.user.UserDTO;

import java.util.List;

public interface UserApi {
    public UserDTO selectById(Long id);

    public List<UserDTO> selectAll();
}
