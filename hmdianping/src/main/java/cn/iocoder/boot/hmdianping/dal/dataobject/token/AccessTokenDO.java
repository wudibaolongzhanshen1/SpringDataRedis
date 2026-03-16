package cn.iocoder.boot.hmdianping.dal.dataobject.token;


import cn.iocoder.boot.hmdianping.dal.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@TableName(value = "access_token", autoResultMap = true)
@Builder
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class AccessTokenDO extends BaseDO {
    /**
     * 编号，数据库递增
     */
    @TableId
    private Long id;
    /**
     * 访问令牌
     */
    private String accessToken;
    /**
     * 刷新令牌
     */
    private String refreshToken;
    /**
     * 用户编号
     */
    private Long userId;
    /**
     * 用户信息
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, String> userInfo;
    /**
     * 过期时间
     */
    private LocalDateTime expiresTime;
}
