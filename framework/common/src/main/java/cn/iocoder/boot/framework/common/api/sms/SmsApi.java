package cn.iocoder.boot.framework.common.api.sms;

public interface SmsApi {
    public void sendSmsCode(String phone);

    public void validSmsCode(String phone, String code);
}
