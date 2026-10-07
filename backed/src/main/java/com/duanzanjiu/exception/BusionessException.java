package com.duanzanjiu.exception;

public class BusionessException extends RuntimeException {
   private Integer code;
    public BusionessException(String message) {
        super(message);
        this.code = 500;
    }
    public BusionessException(Integer code,String message) {
        super(message);
        this.code = code;
    }
    public Integer getCode() {
        return code;
    }
    public void setCode(Integer code) {
        this.code = code;
    }
}
