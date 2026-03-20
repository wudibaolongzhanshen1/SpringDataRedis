package cn.iocoder.boot.hmdianping.api.user;


import cn.hutool.core.bean.BeanUtil;
import cn.iocoder.boot.framework.common.api.user.UserApi;
import cn.iocoder.boot.framework.common.dto.user.UserDTO;
import cn.iocoder.boot.hmdianping.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserApiImpl implements UserApi {

    @Autowired
    private UserService userService;

    @Override
    public UserDTO selectById(Long id) {
        return BeanUtil.toBean(userService.getById(id), UserDTO.class);
    }

    @Override
    public List<UserDTO> selectAll() {
        return BeanUtil.copyToList(userService.list(), UserDTO.class);
    }
}
