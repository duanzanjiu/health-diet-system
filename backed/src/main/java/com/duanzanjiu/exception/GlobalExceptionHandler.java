package com.duanzanjiu.exception;


import com.duanzanjiu.common.Result;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

//扫描异常
@RestControllerAdvice

public class GlobalExceptionHandler {
    //参数的异常
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidException(MethodArgumentNotValidException e) {
        //从异常信息中获取所有的字段报错信息
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        //构建错误信息
        String message = fieldErrors.stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        //返回错误信息
        return Result.error(400, message, null);
    }

    //处理业务异常
    @ExceptionHandler(BusionessException.class)
    public Result<?> handleBusionessException(BusionessException e) {

        return Result.error(e.getCode(), e.getMessage(), null);
    }
}
