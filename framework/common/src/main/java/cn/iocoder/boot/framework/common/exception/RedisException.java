package cn.iocoder.boot.framework.common.exception;

import cn.iocoder.boot.framework.common.exception.enums.GlobalErrorCodeConstants;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Redis操作异常
 */
@Data
@EqualsAndHashCode(callSuper = true)
public final class RedisException extends RuntimeException {

    /**
     * 业务错误码
     */
    private Integer code;
    /**
     * 错误提示
     */
    private String message;

    /**
     * 空构造方法，避免反序列化问题
     */
    public RedisException() {
        super();
    }

    public RedisException(String message){
        super(message);
        this.code = 500;
        this.message = message;
    }

    public RedisException(ErrorCode errorCode) {
        super(errorCode.getMsg());
        this.code = errorCode.getCode();
        this.message = errorCode.getMsg();
    }

    public RedisException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public RedisException(String message, Throwable cause) {
        super(message, cause);
        this.code = GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode();
        this.message = message;
    }

    public RedisException(Throwable cause) {
        super(cause);
        this.code = GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode();
        this.message = cause.getMessage();
    }

    public RedisException setCode(Integer code) {
        this.code = code;
        return this;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public RedisException setMessage(String message) {
        this.message = message;
        return this;
    }

}