package cn.hfbin.seckill.validator;
import  javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import cn.hfbin.seckill.util.ValidatorUtil;
import org.apache.commons.lang3.StringUtils;
//实现ConstraintValidator<注解，参数>
public class IsMobileValidator implements ConstraintValidator<IsMobile, String> {

	private boolean required = false;
	
	public void initialize(IsMobile constraintAnnotation) {
		required = constraintAnnotation.required();
	}

	public boolean isValid(String value, ConstraintValidatorContext context) {
		if(required) {
			return ValidatorUtil.isMobile(value);
		}else {
			if(StringUtils.isEmpty(value)) {
				return true;
			}else {
				return ValidatorUtil.isMobile(value);
			}
		}
	}

}
