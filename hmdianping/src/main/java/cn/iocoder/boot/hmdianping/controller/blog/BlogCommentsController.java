package cn.iocoder.boot.hmdianping.controller.blog;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import cn.iocoder.boot.hmdianping.service.blog.BlogCommentsService;
import jakarta.annotation.Resource;

/**
* <p>
    * 探店笔记评论表 前端控制器
    * </p>
*
* @author czl
* @since 2026-03-11
*/
@Tag(name = "探店笔记评论表", description = "管理 探店笔记评论表 相关接口")
@RestController
@RequestMapping("/blogcommentsdo")
public class BlogCommentsController {

    @Resource
    private BlogCommentsService blogCommentsService;

}