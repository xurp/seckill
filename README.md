总结：本项目使用的一些技术或者一些思路可以提供参考
1、异常处理：在exception包里用全局异常进行处理+参数错误验证
2、返回值：尽量用json，使用Result包住CodeMsg进行返回，这样前台可以收到success，error，code，msg等信息
3、使用interceptor+注解作为拦截器，进行最简单的登录（cookie验证）和单用户并发请求拦截（redis）
4、使用Filter进行全局配置用户seesion在redis里的有效期（有操作就更新）
5、使用自定义注解+自定义验证方法（validator包里）进行数据验证测试
6、使用generatorConfig读数据库的表，自动生成entity、dao、mappers文件（没试过，建议百度）