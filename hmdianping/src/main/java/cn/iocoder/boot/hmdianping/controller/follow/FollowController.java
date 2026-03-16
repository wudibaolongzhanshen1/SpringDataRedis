package cn.iocoder.boot.hmdianping.controller.follow;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import cn.iocoder.boot.hmdianping.service.follow.FollowService;
import jakarta.annotation.Resource;

/**
* <p>
    * 用户关注表 前端控制器
    * </p>
*
* @author czl
* @since 2026-03-11
*/
@Tag(name = "用户关注表", description = "管理 用户关注表 相关接口")
@RestController
@RequestMapping("/follow")
public class FollowController {

    @Resource
    private FollowService followService;

    /**
     * 关注或取关
     * @param id 被关注的用户id
     * @param isFollow true: 关注, false: 取关
     */
    @PutMapping("/{id}/{isFollow}")
    @Operation(summary = "关注或取关", description = "关注或取关")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void follow(@PathVariable("id") Long id, @PathVariable("isFollow") Boolean isFollow) {
        followService.follow(id, isFollow);
    }

    /**
     * 判定当前用户是否关注了目标用户
     * @param id 目标用户id
     */
    @GetMapping("/follow_or_not/{id}")
    @Operation(summary = "判定当前用户是否关注了目标用户", description = "判定当前用户是否关注了目标用户")
    public Boolean isFollow(@PathVariable("id") Long id) {
        return followService.isFollow(id);
    }

}