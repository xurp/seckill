package cn.hfbin.seckill.interceptor;

import cn.hfbin.seckill.annotations.AccessLimit;
import cn.hfbin.seckill.common.Const;
import cn.hfbin.seckill.entity.User;
import cn.hfbin.seckill.redis.AccessKey;
import cn.hfbin.seckill.redis.BasePrefix;
import cn.hfbin.seckill.redis.RedisService;
import cn.hfbin.seckill.redis.UserKey;
import cn.hfbin.seckill.result.CodeMsg;
import cn.hfbin.seckill.result.Result;
import cn.hfbin.seckill.util.CookieUtil;
import cn.hfbin.seckill.util.JsonUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

/**
 * 使用拦截器统一校验用户权限
 */
@Component
public class AuthorityInterceptor implements HandlerInterceptor {
    @Autowired
    RedisService redisService;

    private Logger logger = LoggerFactory.getLogger(AuthorityInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //请求controller中的方法名
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        //解析HandlerMethod
        String methodName = handlerMethod.getMethod().getName();
        String className = handlerMethod.getBean().getClass().getSimpleName();

        StringBuffer requestParamBuffer = new StringBuffer();
        Map paramMap = request.getParameterMap();
        Iterator it = paramMap.entrySet().iterator();
        //这里是把参数放到requestParamBuffer里，方便日志打印，格式例如param:goodsId=[1]
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String mapKey = (String) entry.getKey();
            String mapValue = "";

            //request的这个参数map的value返回的是一个String[]
            Object obj = entry.getValue();
            if (obj instanceof String[]) {
                String[] strs = (String[]) obj;
                mapValue = Arrays.toString(strs);
            }
            requestParamBuffer.append(mapKey).append("=").append(mapValue);
        }
        //接口限流
        AccessLimit accessLimit = handlerMethod.getMethodAnnotation(AccessLimit.class);
        if(accessLimit == null) {
            return true;
        }
        int seconds = accessLimit.seconds();
        int maxCount = accessLimit.maxCount();
        boolean needLogin = accessLimit.needLogin();
        String key = request.getRequestURI();
        logger.info("key:"+key);//key:/seckill/path

        //对于拦截器中拦截manage下的login.do的处理,对于登录不拦截，直接放行（实际上目前只拦截SeckillController里的getMiaoshaPath）
        if (!StringUtils.equals(className, "SeckillController")) {
            //如果是拦截到登录请求，不打印参数，因为参数里面有密码，全部会打印到日志中，防止日志泄露
            logger.info("权限拦截器拦截到请求 SeckillController ,className:{},methodName:{}", className, methodName);
            return true;
        }
        //似乎这里只拦截SeckillContorller里的方法，目前也只有SeckillController里的getMiaoshaPath用了@AccessLimit
        logger.info("--> 权限拦截器拦截到请求,className:{},methodName:{},param:{}", className, methodName, requestParamBuffer);
        User user = null;
        String loginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isNotEmpty(loginToken)) {
            user = redisService.get(UserKey.getByName, loginToken, User.class);//这个UserKey可以在fastoRedis里找到
        }

        if(needLogin) {
            if(user == null) {
            	//如果需要登录但没登录（cookie取user取不到），那么用render方法写new CodeMsg(500216, "用户未登录")
            	//这样，goods_detail页面进入success但data.code != 0，所以打印data.msg（code和msg都通过Result的构造把CodeMsg里的同名变量封装在Result里了）
                render(response, CodeMsg.USER_NO_LOGIN);
                return false;
            }
            key += "_" + user.getId();
        }else {
            //do nothing
        }
        AccessKey ak = AccessKey.withExpire;//extends BasePrefix的一个标识，只用作redis key的生成
        Integer count = redisService.get(ak, key, Integer.class);
        if(count  == null) {
            redisService.set(ak, key, 1, seconds);
        }else if(count < maxCount) {
            redisService.incr(ak, key);//当同一个路径访问到5的时候就说访问太频繁了(测试时需要迅速访问5次模拟单用户恶意并发，否则count会清0）
            //因为上面seconds的注解设置为5，redis设置key的时候超过5秒就过期了，所以fastoRedis里很难看到accessKey
        }else {
            render(response, CodeMsg.ACCESS_LIMIT_REACHED);
            return false;
        }

        /*if (user == null) {
            //重置 重写response一定要重置 这里要添加reset，否则报异常 getWriter() has already been called for this response
            response.reset();
            //geelynote 这里要设置编码，否则会乱码
            response.setCharacterEncoding("UTF-8");
            // 这里要设置返回值类型，因为全部是json接口。
            response.setContentType("application/json;charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.print(JsonUtil.obj2String(Result.error(CodeMsg.USER_NO_LOGIN)));
            //response.sendRedirect(request.getContextPath()+"/page/login");
            // 这里要关闭流
            out.flush();
            out.close();
            return false;
        }*/
        logger.info("拦截没问题，即将放行");
        return true;
    }


    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
    }
    private void render(HttpServletResponse response, CodeMsg cm)throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        OutputStream out = response.getOutputStream();
        String str  = JSON.toJSONString(Result.error(cm));
        out.write(str.getBytes("UTF-8"));
        out.flush();
        out.close();
    }

}
