package com.nplat.convert.config;


public enum ApiMsgEnum {
    OK(200, "OK"),
    BAD_REQUEST(400, "Bad Request"),
    NOT_FOUND(404, "Not Found"),
    UNAUTHORIZED(401, "Unauthorized"),
    FORBIDDEN(403, "Forbidden"),
    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
    BAD_GATEWAY(502, "Bad Gateway"),
    SERVICE_UNAVAILABLE(503, "Service Unavailable"),
    GATEWAY_TIMEOUT(504, "Gateway Timeout"),
    LOGIN_REQUEST_ERROR(10000, "参数错误，请检查email和密码长度不能超过50个字符且不为空,验证码等信息"),
    REGISTER_EXISTS(10001, "参数错误，请检查email和密码长度不能超过50个字符且不为空,验证码等信息"),
    HTTP_ERROR(10002, "http请求失败"),
    CODE_VERIFY(10003, "验证码错误"),
    USER_NOT_EXISTS(10004, "用户名或密码错误"),
    USER_ALREADY_EXIST(10005, "用户已存在"),
    AUTH_ERROR(10006, "权限不够"),
    TOKEN_EXPIRED(10007, "token过期"),
    LOGIN_REQUEST_FAILED(10008, "请先在小程序登录"),
    PARAM_FAILED(10009, "参数不正确，请检查参数"),
    SERVER_TIMEUP(10010, "服务器运行时间是早上9:00~23:00"),;
    private int code;
    private String message;

    private ApiMsgEnum(int code, String msg) {
        this.code = code;
        this.message = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
