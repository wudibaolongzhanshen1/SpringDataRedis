package cn.iocoder.boot.hmdianping.service.follow;

import cn.iocoder.boot.hmdianping.dal.dataobject.follow.FollowDO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* <p>
    * 用户关注表 服务类
    * </p>
*
* @author czl
* @since 2026-03-11
*/
public interface FollowService extends IService<FollowDO> {

    void follow(Long followUserId, Boolean isFollow);

    Boolean isFollow(Long followUserId);
}