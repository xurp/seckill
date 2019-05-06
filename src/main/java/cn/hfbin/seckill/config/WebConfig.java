package cn.hfbin.seckill.config;

import cn.hfbin.seckill.filter.SessionExpireFilter;
import cn.hfbin.seckill.interceptor.AuthorityInterceptor;
import org.apache.catalina.filters.AddDefaultCharsetFilter;
import org.apache.catalina.filters.RemoteIpFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.servlet.Filter;

/**
 * Created by: HuangFuBin
 * Date: 2018/7/11
 * Time: 20:58
 * Such description:
 */
@Configuration
public class WebConfig extends WebMvcConfigurerAdapter {
    @Autowired
    AuthorityInterceptor authorityInterceptor;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // addPathPatterns 用于添加拦截规则
        // excludePathPatterns 用户排除拦截
        // 映射为 user 的控制器下的所有映射
        //registry.addInterceptor(authorityInterceptor).addPathPatterns("/user/login").excludePathPatterns("/index", "/");
    	/*  附：api项目里的写法，当时这样写是因为interceptor里注入service用不了，但本项目是可以的
    	 *  @Bean
             public HandlerInterceptor getApiAuthInterceptor(){
                 return new ApiAuthInterceptor();
             }

             @Override
             public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(getApiAuthInterceptor())
                    .addPathPatterns("/**");
             }
    	 */
        registry.addInterceptor(authorityInterceptor);
        super.addInterceptors(registry);
    }
    //我的理解是：以下两个方法注册过滤器SessionExpireFilter
    @Bean("myFilter")
    public Filter uploadFilter() {
        return new SessionExpireFilter();
    }
  
    @Bean
    public FilterRegistrationBean uploadFilterRegistration() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        //myFilter就是上面的Bean name
        registration.setFilter(new DelegatingFilterProxy("myFilter"));
        registration.addUrlPatterns("/**");
        registration.setName("MyFilter");
        registration.setOrder(1);
        return registration;
    }


}
