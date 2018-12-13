package cn.hfbin.seckill.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(METHOD)
public @interface AccessLimit {
	int seconds();
	int maxCount();//也在interceptors里，SeckillController里的getMiaoshaPath用了@AccessLimit(seconds=5, maxCount=5, needLogin=true)
	//然后这个值就在interceptor里被获取了
	boolean needLogin() default true;//相同的，needLogin在interceptor里
}
//这里应该是自定义了注解，@Retention(RUNTIME)、@Target(METHOD)、@interface具体待查
//包括三个方法，其中一个是简单的权限控制