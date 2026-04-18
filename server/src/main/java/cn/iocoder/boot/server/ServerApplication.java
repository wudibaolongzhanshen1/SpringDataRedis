package cn.iocoder.boot.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication(scanBasePackages = {"cn.iocoder.boot"})
@EnableAspectJAutoProxy(exposeProxy = true, proxyTargetClass = false) // 使用JDK动态代理，避免CGLIB问题
public class ServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }

}
