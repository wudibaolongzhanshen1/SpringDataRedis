package cn.iocoder.boot.hmdianping.dal.mysql.user;

import cn.iocoder.boot.hmdianping.dal.dataobject.user.UserDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface UserMapper extends BaseMapper<UserDO> {
    default UserDO selectByPhone(String phone) {
        return this.selectOne(Wrappers.<UserDO>lambdaQuery().eq(UserDO::getPhone, phone));
    }
}
