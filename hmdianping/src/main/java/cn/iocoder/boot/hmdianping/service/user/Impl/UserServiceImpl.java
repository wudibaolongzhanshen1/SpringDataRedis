package cn.iocoder.boot.hmdianping.service.user.Impl;

import cn.iocoder.boot.hmdianping.dal.dataobject.user.UserDO;
import cn.iocoder.boot.hmdianping.dal.mysql.user.UserMapper;
import cn.iocoder.boot.hmdianping.service.user.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;


@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public UserDO getUserByPhone(String phone) {
        validatePhoneLegal(phone);
        return userMapper.selectByPhone(phone);
    }

    @Override
    public Long createUser(String phone) {
        validatePhoneLegal(phone);
        UserDO user = new UserDO();
        user.setPhone(phone);
        user.setNickName("用户" + System.currentTimeMillis());
        userMapper.insert(user);
        return user.getId();
    }

    @Override
    public Boolean validatePhoneLegal(String phone) {
        if (phone == null || phone.length() != 11) {
            throw new IllegalArgumentException("手机号格式不正确");
        }
        return null;
    }

}
