package cn.iocoder.boot.framework.common.dto.token;

import java.io.Serializable;
import java.time.LocalDateTime;

public class RefreshTokenRespDTO implements Serializable {
    /**
     * 用户编号
     */
    private Long userId;

    private String refreshToken;
    /**
     * 过期时间
     */
    private LocalDateTime expiresTime;


}
