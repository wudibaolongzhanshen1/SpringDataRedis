package cn.iocoder.boot.hmdianping.dal.mysql.token;

import cn.iocoder.boot.hmdianping.dal.dataobject.token.AccessTokenDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AccessTokenMapper extends BaseMapper<AccessTokenDO> {
    default AccessTokenDO selectByAccessToken(String token) {
        return selectOne(Wrappers.lambdaQuery(AccessTokenDO.class)
                .eq(AccessTokenDO::getAccessToken, token));
    }

    default List<AccessTokenDO> selectByRefreshToken(String refreshToken) {
        return selectList(Wrappers.lambdaQuery(AccessTokenDO.class)
                .eq(AccessTokenDO::getRefreshToken, refreshToken));
    }
}
