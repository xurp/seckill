package cn.hfbin.seckill;

import cn.hfbin.seckill.filter.SessionExpireFilter;
import org.apache.catalina.filters.RemoteIpFilter;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@MapperScan("cn.hfbin.seckill.dao")
public class SeckillApplication extends SpringBootServletInitializer {
//http://localhost:8888/page/login
//http://localhost:8888/goods/list
//18077200000 123456
	//两个注意点：一：可能是用mybatis，所以用了@MapperScan导入；二：extends了SpringBootServletInitializer，待查
	public static void main(String[] args) {
		SpringApplication.run(SeckillApplication.class, args);
	}


}
