package cn.hfbin.seckill.param;

import cn.hfbin.seckill.validator.IsMobile;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

/**
 * Created by: HuangFuBin
 * Date: 2018/7/10
 * Time: 10:11
 * Such description:
 */
@Getter
@Setter
@ToString
public class LoginParam {
    //使用@IsMobile()这个自定义的注解进行手机号验证（使用自定义注解的一个强行应用）
	//在登录页面中，乱输手机号就会被@IsMobile()检测到
    @NotNull(message = "手机号不能为空")
    @IsMobile()
    private String mobile;
    @NotNull(message="密码不能为空")
    @Length(min = 23, message = "密码长度需要在7个字以内")
    private String password;
}
