package cn.iocoder.boot.hmdianping.service.blog;

import cn.iocoder.boot.hmdianping.controller.blog.vo.ScrollRespVO;
import cn.iocoder.boot.hmdianping.dal.dataobject.blog.BlogDO;
import cn.iocoder.boot.hmdianping.dal.dataobject.user.UserDO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* <p>
    * 探店笔记表 服务类
    * </p>
*
* @author czl
* @since 2026-03-11
*/
public interface BlogService extends IService<BlogDO> {
    List<BlogDO> queryHotBlog(Integer current);

    BlogDO queryBlogById(Long id);

    void likeBlog(Long id);

    List<UserDO> queryBlogLikes(Long id);

    Long saveBlog(BlogDO blog) throws Exception;

    ScrollRespVO queryBlogOfFollow(Long max, Integer offset);
}