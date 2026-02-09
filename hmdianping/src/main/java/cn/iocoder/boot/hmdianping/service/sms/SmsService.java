package cn.iocoder.boot.hmdianping.service.sms;

public interface SmsService {

    public void sendSmsCode(String phone);

    public void validSmsCode(String phone, String code);

    public Boolean validPhoneLegal(String phone);
}
