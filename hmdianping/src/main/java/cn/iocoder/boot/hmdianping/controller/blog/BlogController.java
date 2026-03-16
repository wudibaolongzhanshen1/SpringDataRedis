package cn.iocoder.boot.hmdianping.controller.blog;

import cn.iocoder.boot.hmdianping.controller.blog.vo.ScrollRespVO;
import cn.iocoder.boot.hmdianping.dal.dataobject.blog.BlogDO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import cn.iocoder.boot.hmdianping.service.blog.BlogService;
import jakarta.annotation.Resource;

/**
 * <p>
 * 探店笔记表 前端控制器
 * </p>
 *
 * @author czl
 * @since 2026-03-11
 */
@Tag(name = "探店笔记表", description = "管理 探店笔记表 相关接口")
@RestController
@RequestMapping("/blog")
public class BlogController {

    @Resource
    private BlogService blogService;

    @PostMapping("/add")
    @Operation(summary = "添加探店blog", description = "添加 探店blog 接口")
    public Long addBlog(@RequestBody BlogDO blog) throws Exception {
        return blogService.saveBlog(blog);
    }

    @GetMapping("/hot")
    @Operation(summary = "查询关注博主的探店blog", description = "查询关注博主的探店blog")
    public ScrollRespVO queryBlogOfFollow(@RequestParam("最新博客时间戳") Long max,
                                          @RequestParam("跳过几条记录的偏移量") Integer offset) {
        return blogService.queryBlogOfFollow(max, offset);
    }


}