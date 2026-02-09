package cn.iocoder.boot.hmdianping.dal.dataobject.token;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;


@Data
@TableName(value = "refresh_token", autoResultMap = true)
@Builder
public class RefreshTokenDO {
    /**
     * 编号，数据库字典
     */
    private Long id;
    /**
     * 刷新令牌
     */
    private String refreshToken;
    /**
     * 用户编号
     */
    private Long userId;
    /**
     * 过期时间
     */
    private LocalDateTime expiresTime;

}
