package cn.iocoder.boot.hmdianping.api.sms;

import cn.iocoder.boot.framework.common.api.sms.SmsApi;
import cn.iocoder.boot.hmdianping.service.sms.SmsService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;


@Service
public class SmsApiImpl implements SmsApi {

    @Resource
    private SmsService smsService;

    @Override
    public void sendSmsCode(String phone) {
        smsService.sendSmsCode(phone);
    }

    @Override
    public void validSmsCode(String phone, String code) {
        smsService.validSmsCode(phone, code);
    }

}
