package cn.hfbin.seckill.controller;

import cn.hfbin.seckill.annotations.AccessLimit;
import cn.hfbin.seckill.bo.GoodsBo;
import cn.hfbin.seckill.common.Const;
import cn.hfbin.seckill.entity.OrderInfo;
import cn.hfbin.seckill.entity.SeckillOrder;
import cn.hfbin.seckill.entity.User;
import cn.hfbin.seckill.mq.MQReceiver;
import cn.hfbin.seckill.mq.MQSender;
import cn.hfbin.seckill.mq.SeckillMessage;
import cn.hfbin.seckill.redis.GoodsKey;
import cn.hfbin.seckill.redis.RedisService;
import cn.hfbin.seckill.redis.UserKey;
import cn.hfbin.seckill.result.CodeMsg;
import cn.hfbin.seckill.result.Result;
import cn.hfbin.seckill.service.SeckillGoodsService;
import cn.hfbin.seckill.service.SeckillOrderService;
import cn.hfbin.seckill.util.CookieUtil;
import cn.hfbin.seckill.vo.OrderDetailVo;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;

/**
 * Created by: HuangFuBin
 * Date: 2018/7/15
 * Time: 23:55
 * Such description:
 */
@Controller
@RequestMapping("seckill")
public class SeckillController implements InitializingBean {


    @Autowired
    RedisService redisService;

    @Autowired
    SeckillGoodsService seckillGoodsService;

    @Autowired
    SeckillOrderService seckillOrderService;

    @Autowired
    MQSender mqSender;

    //Long：商品ID Boolean：商品是否秒杀完毕（库存），初始化为false
    private HashMap<Long, Boolean> localOverMap = new HashMap<Long, Boolean>();

    /**
     * 系统初始化，凡是implements InitializingBean的类，在bean初始化时候会运行afterPropertiesSet方法
     */
    public void afterPropertiesSet() throws Exception {
        List<GoodsBo> goodsList = seckillGoodsService.getSeckillGoodsList();
        if (goodsList == null) {
            return;
        }
        for (GoodsBo goods : goodsList) {
        	//初始化：把每个商品的库存放到redis里，本类的list方法用到了这个
            redisService.set(GoodsKey.getSeckillGoodsStock, "" + goods.getId(), goods.getStockCount(), Const.RedisCacheExtime.GOODS_LIST);
            localOverMap.put(goods.getId(), false);
        }
    }
    
    //这里用的是annotations里的注解
    //前台点秒杀，首先访问到这里
    @AccessLimit(seconds=5, maxCount=5, needLogin=true)
    @RequestMapping(value = "/path", method = RequestMethod.GET)
    @ResponseBody
    public Result<String> getMiaoshaPath(HttpServletRequest request, User user,
                                         @RequestParam("goodsId") long goodsId) {
        String loginToken = CookieUtil.readLoginToken(request);
        user = redisService.get(UserKey.getByName, loginToken, User.class);
        if (user == null) {
            return Result.error(CodeMsg.USER_NO_LOGIN);
        }
        String path = seckillOrderService.createMiaoshaPath(user, goodsId);
        //返回一个加密的后缀
        return Result.success(path);
    }
    
    //这个方法没用到
    @RequestMapping("/seckill2")
    public String list2(Model model,
                        @RequestParam("goodsId") long goodsId, HttpServletRequest request) {

        String loginToken = CookieUtil.readLoginToken(request);
        User user = redisService.get(UserKey.getByName, loginToken, User.class);
        model.addAttribute("user", user);
        if (user == null) {
            return "login";
        }
        //判断库存
        GoodsBo goods = seckillGoodsService.getseckillGoodsBoByGoodsId(goodsId);
        int stock = goods.getStockCount();
        if (stock <= 0) {
            model.addAttribute("errmsg", CodeMsg.MIAO_SHA_OVER.getMsg());
            return "miaosha_fail";
        }
        //判断是否已经秒杀到了
        SeckillOrder order = seckillOrderService.getSeckillOrderByUserIdGoodsId(user.getId(), goodsId);
        if (order != null) {
            model.addAttribute("errmsg", CodeMsg.REPEATE_MIAOSHA.getMsg());
            return "miaosha_fail";
        }
        //减库存 下订单 写入秒杀订单
        OrderInfo orderInfo = seckillOrderService.insert(user, goods);
        model.addAttribute("orderInfo", orderInfo);
        model.addAttribute("goods", goods);
        return "order_detail";
    }

    //似乎这个是优化上面的，因为用了mq，goods_detail的doMiaosha()使用的是这个方法
    @RequestMapping(value = "/{path}/seckill", method = RequestMethod.POST)
    @ResponseBody
    public Result<Integer> list(Model model,
                                @RequestParam("goodsId") long goodsId,
                                @PathVariable("path") String path,
                                HttpServletRequest request) {

        String loginToken = CookieUtil.readLoginToken(request);
        User user = redisService.get(UserKey.getByName, loginToken, User.class);
        if (user == null) {
            return Result.error(CodeMsg.USER_NO_LOGIN);
        }
        //验证加密过的后缀path
        boolean check = seckillOrderService.checkPath(user, goodsId, path);
        if (!check) {
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        }
        //内存标记，减少redis访问
        boolean over = localOverMap.get(goodsId);
        if (over) {
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }/**/
        //预减库存，看是否秒杀结束（库存没了）
        long stock = redisService.decr(GoodsKey.getSeckillGoodsStock, "" + goodsId);//10
        if (stock < 0) {
            localOverMap.put(goodsId, true);
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }
        //判断是否已经秒杀到了（同用户同商品重复秒杀）
        SeckillOrder order = seckillOrderService.getSeckillOrderByUserIdGoodsId(user.getId(), goodsId);
        if (order != null) {
            return Result.error(CodeMsg.REPEATE_MIAOSHA);
        }
        //入队
        SeckillMessage mm = new SeckillMessage();
        mm.setUser(user);
        mm.setGoodsId(goodsId);
        mqSender.sendSeckillMessage(mm);//在消息队列的接收方MQReceiver完成操作
        return Result.success(0);//排队中，直接返回结果（用户并不关心后台数据库之类的操作）
        /*
         * 另一种做法，也就是list2的做法：不用redis维护库存，直接数据库操作
         *
        //判断库存
        GoodsBo goods = seckillGoodsService.getseckillGoodsBoByGoodsId(goodsId);
        if(goods == null) {
            return Result.error(CodeMsg.NO_GOODS);
        }
        int stock = goods.getStockCount();
        if(stock <= 0) {
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }
        //判断是否已经秒杀到了
        SeckillOrder order = seckillOrderService.getSeckillOrderByUserIdGoodsId(user.getId(), goodsId);
        if(order != null) {
            return Result.error(CodeMsg.REPEATE_MIAOSHA);
        }
        //减库存 下订单 写入秒杀订单
        OrderInfo orderInfo = seckillOrderService.insert(user, goods);
        return Result.success(orderInfo);*/
    }

    /**
     * 客户端轮询查询是否下单成功，如果成功弹出“恭喜你，秒杀成功！查看订单？”
     * 某orderId：成功，已生成订单
     * -1：秒杀失败
     * 0： 排队中
     */
    //见static/goods_detail.htm的getMiaoshaResult。当前台点了秒杀后，不停轮询（setTimeout）直到秒杀结果出来为止
    @RequestMapping(value = "/result", method = RequestMethod.GET)
    @ResponseBody
    public Result<Long> miaoshaResult(@RequestParam("goodsId") long goodsId, HttpServletRequest request) {
        String loginToken = CookieUtil.readLoginToken(request);
        User user = redisService.get(UserKey.getByName, loginToken, User.class);
        if (user == null) {
            return Result.error(CodeMsg.USER_NO_LOGIN);
        }
        long result = seckillOrderService.getSeckillResult((long) user.getId(), goodsId);
        return Result.success(result);
    }
    
}
