package cn.iocoder.boot.hmdianping.dal.mysql.blog;

import cn.iocoder.boot.hmdianping.dal.dataobject.blog.BlogCommentsDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

/**
* 探店笔记评论表 Mapper 接口
*/
@Mapper
public interface BlogCommentsMapper extends BaseMapper<BlogCommentsDO> {

}