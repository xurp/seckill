package cn.hfbin.seckill.service.ipml;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.hfbin.seckill.dao.AccessTimeMapper;
import cn.hfbin.seckill.dao.GoodsMapper;
import cn.hfbin.seckill.entity.AccessTime;
import cn.hfbin.seckill.service.MyCatService;

@Service("myCatService")
public class MyCatServiceImpl implements MyCatService {
	@Autowired
    AccessTimeMapper accessTimeMapper;
	@Override
	public void insert(AccessTime accessTime) {
		accessTimeMapper.insert(accessTime);
	}

}
