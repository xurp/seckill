总结：本项目使用的一些技术或者一些思路可以提供参考
1、异常处理：在exception包里用全局异常进行处理+参数错误验证
2、返回值：尽量用json，使用Result包住CodeMsg进行返回，这样前台可以收到success，error，code，msg等信息
3、使用interceptor+注解作为拦截器，进行最简单的登录（cookie验证）和单用户并发请求拦截（redis）
4、使用Filter进行全局配置用户seesion在redis里的有效期（有操作就更新）
5、使用自定义注解+自定义验证方法（validator包里）进行数据验证测试
6、使用generatorConfig读数据库的表，自动生成entity、dao、mappers文件（可参考mybatisTest项目）
附：jwt和session
在 session 机制下，当用户登录时，服务器从数据库读出用户信息，把用户信息保存在 session 容器里，然后创建一个 sessionId 指向这个用户信息。然后把 sessionId 返回给客户端保存。下次客户端请求时带上 sessionId，服务器查找 sessionId 对应的 session，即可得到用已认证的户信息。
可以理解成服务器创建并保存会员卡，然后把卡号告诉你。你要消费的时候提供卡号就行。

在 JWT 机制下，当用户登录时，服务器从数据库读出用户信息，把用户信息加密生成一个 token。然后把 token 返回给客户端保存。下次客户的请求时带上 token，服务器解密 token，从中得到已认证的用户信息。
可以理解成服务器创建会员卡，然后把卡给你。你要消费的时候要提供会员卡。