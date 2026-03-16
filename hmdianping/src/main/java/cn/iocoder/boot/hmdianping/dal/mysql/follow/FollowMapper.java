package cn.iocoder.boot.hmdianping.dal.mysql.follow;

import cn.iocoder.boot.hmdianping.dal.dataobject.follow.FollowDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

/**
* 用户关注表 Mapper 接口
*/
@Mapper
public interface FollowMapper extends BaseMapper<FollowDO> {

}