package cn.iocoder.boot.hmdianping.service.blog.Impl;

import cn.iocoder.boot.hmdianping.dal.dataobject.blog.BlogCommentsDO;
import cn.iocoder.boot.hmdianping.dal.mysql.blog.BlogCommentsMapper;
import cn.iocoder.boot.hmdianping.service.blog.BlogCommentsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
* <p>
    * 探店笔记评论表 服务实现类
    * </p>
*
* @author czl
* @since 2026-03-11
*/
@Service
public class BlogCommentsServiceImpl extends ServiceImpl<BlogCommentsMapper, BlogCommentsDO> implements BlogCommentsService {

}