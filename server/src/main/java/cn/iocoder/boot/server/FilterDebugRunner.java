package cn.iocoder.boot.server;

import jakarta.servlet.Filter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class FilterDebugRunner implements CommandLineRunner {

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public void run(String... args) {
        System.out.println("======= 已注册到 Servlet 容器的过滤器列表 =======");
        // 获取所有 FilterRegistrationBean 类型的 Bean
        Map<String, FilterRegistrationBean> beans = applicationContext.getBeansOfType(FilterRegistrationBean.class);

        beans.forEach((name, bean) -> {
            System.out.printf("BeanName: %-30s | FilterClass: %-40s | Order: %d\n",
                    name,
                    bean.getFilter().getClass().getName(),
                    bean.getOrder());
        });

        // 检查你的 TokenAuthenticationFilter 是否作为原始 Bean 被 Spring Boot 自动包裹
        String[] filterBeanNames = applicationContext.getBeanNamesForType(Filter.class);
        for (String beanName : filterBeanNames) {
            System.out.println("检测到原始 Filter Bean: " + beanName);
        }
        System.out.println("===============================================");
    }
}