package cn.hfbin.seckill.exception;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import cn.hfbin.seckill.result.CodeMsg;
import cn.hfbin.seckill.result.Result;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

//这个应该是全局异常处理
@ControllerAdvice
@ResponseBody
public class HfbinExceptionHandler {
	@ExceptionHandler(value=Exception.class)
	public Result<String> exceptionHandler(HttpServletRequest request, Exception e){
		e.printStackTrace();
		//应该是捕捉所有Exception.class，按照种类返回，Result是封装好的
		if(e instanceof HfbinException) {
			HfbinException ex = (HfbinException)e;
			return Result.error(ex.getCm());
		}else if(e instanceof BindException) {
			//参数校验异常
			BindException ex = (BindException)e;
			List<ObjectError> errors = ex.getAllErrors();
			ObjectError error = errors.get(0);
			String msg = error.getDefaultMessage();
			return Result.error(CodeMsg.BIND_ERROR.fillArgs(msg));
		}else {
			return Result.error(CodeMsg.SERVER_ERROR);
		}
	}
}
