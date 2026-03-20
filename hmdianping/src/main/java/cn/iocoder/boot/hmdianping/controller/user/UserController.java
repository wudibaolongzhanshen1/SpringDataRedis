package cn.iocoder.boot.hmdianping.controller.user;


import cn.iocoder.boot.framework.common.pojo.CommonResult;
import cn.iocoder.boot.hmdianping.dal.dataobject.user.UserDO;
import cn.iocoder.boot.hmdianping.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/user")
@Tag(name = "用户模块", description = "用户相关接口")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/getByPhone")
    @Operation(summary = "根据手机号获取用户信息", description = "根据手机号获取用户信息")
    @Cacheable(value = "userCache", key = "'phone:'+ #phone", unless = "#result == null")
    public CommonResult<UserDO> getByPhone(@RequestParam("phone") String phone) {
        return CommonResult.success(userService.getUserByPhone(phone));
    }
}
