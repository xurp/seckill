package cn.hfbin.seckill.service.ipml;

import cn.hfbin.seckill.bo.GoodsBo;
import cn.hfbin.seckill.dao.GoodsMapper;
import cn.hfbin.seckill.service.SeckillGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by: HuangFuBin
 * Date: 2018/7/12
 * Time: 19:47
 * Such description:
 */

@Service("seckillGoodsService")
public class SeckillGoodsServiceImpl implements SeckillGoodsService {
	//这些mapper在generatorConfig.xml里面扫描进来，所以Mapper类没有加注解
    @Autowired
    GoodsMapper goodsMapper;
    @Override
    public List<GoodsBo> getSeckillGoodsList() {
        return goodsMapper.selectAllGoodes();
    }

    @Override
    public GoodsBo getseckillGoodsBoByGoodsId(long goodsId) {
        return goodsMapper.getseckillGoodsBoByGoodsId(goodsId);
    }

    @Override
    public int reduceStock(long goodsId) {
    	//这个方法只被SeckillOrder的service调用，所以不用加事务
        return goodsMapper.updateStock(goodsId);
    }
}
