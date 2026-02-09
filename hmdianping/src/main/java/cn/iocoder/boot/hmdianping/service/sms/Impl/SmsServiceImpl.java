package cn.iocoder.boot.hmdianping.service.sms.Impl;

import cn.hutool.core.util.RandomUtil;
import cn.iocoder.boot.framework.common.enums.redis.RedisKey;
import cn.iocoder.boot.hmdianping.service.sms.SmsService;
import cn.iocoder.boot.framework.common.util.RegexUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class SmsServiceImpl implements SmsService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void sendSmsCode(String phone) {
        validPhoneLegal(phone);
        String code = RandomUtil.randomNumbers(6);
        stringRedisTemplate.opsForValue().set(RedisKey.LOGIN_CODE.join(phone), code, RedisKey.LOGIN_CODE.getTtl(), RedisKey.LOGIN_CODE.getUnit());
        log.info("发送短信验证码成功，验证码：{}", code);
        return;
    }

    @Override
    public void validSmsCode(String phone, String code) {
        validPhoneLegal(phone);
        validSmsCode0(phone, code);
    }

    @Override
    public Boolean validPhoneLegal(String phone) {
        boolean phoneInvalid = RegexUtils.isPhoneInvalid(phone);
        if (phoneInvalid) {
            throw new IllegalArgumentException("手机号格式错误");
        }
        return true;
    }

    private void validSmsCode0(String phone, String code) {
        String redisCode = stringRedisTemplate.opsForValue().get(RedisKey.LOGIN_CODE.join(phone));
        if (redisCode == null || !redisCode.equals(code)) {
            throw new IllegalArgumentException("验证码不正确");
        }
    }
}
