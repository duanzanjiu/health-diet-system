package com.duanzanjiu.common;

public enum ResultCode {
    // 成功
    SUCCESS(200, "成功"),
    ERROR(-1, "失败"),
    UNAUTHORIZED(401, "未授权"),
    SYSTEM_ERROR(500, "系统错误");

    private Integer code;
    private String msg;
    ResultCode(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}

