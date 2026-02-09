package cn.iocoder.boot.framework.common.dto.token;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class AccessTokenCheckRespDTO implements Serializable {
    /**
     * 用户编号
     */
    private Long userId;

    private String accessToken;

    private String refreshToken;
    /**
     * 用户信息
     */
    private Map<String, String> userInfo;
    /**
     * 过期时间
     */
    private LocalDateTime expiresTime;

}
