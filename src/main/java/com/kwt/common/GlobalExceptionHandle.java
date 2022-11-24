package com.kwt.common;

import com.kwt.exception.BusinessException;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.sql.SQLIntegrityConstraintViolationException;

@RestControllerAdvice //此注解包含了 @ControllerAdvice @Aspect 功能
@Slf4j
public class GlobalExceptionHandle {
    /**
     * @ExceptionHandler 处理哪些异常
     * 单独捕获的那个重复的异常
     * @return
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)

    public R<String> handle(SQLIntegrityConstraintViolationException exception){
        /**
         * 控制台打印异常日志
         */
        log.info("GlobalExceptionHandle::handle",exception);

        /**
         * 判断单独捕获的异常是否有内容，再根据空格切割获去对应的值
         */
        if (StringUtils.isNotBlank(exception.getMessage())&&exception.getMessage().contains("Duplicate entry")) {
            //信息重复,将重复信息以空格进行切割，直至取到重复值
            String field = exception.getMessage().split(" ")[2];
            return R.error(field+"已被注册，请修改后再次注册");
        }else {

            return R.error("未知异常");
        }
    }
    @ExceptionHandler(BusinessException.class)
    public R<String> handleException(BusinessException exception){
        return R.error(exception.getMessage());
    }
}
