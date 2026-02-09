package cn.iocoder.boot.framework.common.enums.mysql;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TokenExpiry {

    /**
     * Access Token 有效期：2 小时
     */
    ACCESS_TOKEN(2 * 60 * 60L),

    /**
     * Refresh Token 有效期：30 天
     */
    REFRESH_TOKEN(30 * 24 * 60 * 60L);

    private final Long expiry; // 有效期，单位：秒

}
