package cn.iocoder.boot.hmdianping.dal.mysql.token;

import cn.iocoder.boot.hmdianping.dal.dataobject.token.RefreshTokenDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface RefreshTokenMapper extends BaseMapper<RefreshTokenDO> {
    default RefreshTokenDO selectByRefreshToken(String refreshToken) {
        return selectOne(Wrappers.lambdaQuery(RefreshTokenDO.class)
                .eq(RefreshTokenDO::getRefreshToken, refreshToken));
    }
}
