package cn.iocoder.boot.framework.common.enums.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RegexPatterns {
    // 枚举项必须放在最前面，且以分号结尾
    PHONE("^1([38][0-9]|4[579]|5[0-3,5-9]|6[6]|7[0135678]|9[89])\\d{8}$"),
    EMAIL("^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$"),
    PASSWORD("^\\w{4,32}$"),
    VERIFY_CODE("^[a-zA-Z\\d]{6}$");

    private final String pattern;
}
