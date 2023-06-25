package com.nplat.convert.config;


public class BaseResponse{
    protected int code;
    protected String message;
    protected Object data;

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

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public BaseResponse() {
        this.code = ApiMsgEnum.OK.getCode();
        this.message = ApiMsgEnum.OK.getMessage();
    }



    public BaseResponse(Object data) {
        this.code = ApiMsgEnum.OK.getCode();
        this.message = ApiMsgEnum.OK.getMessage();
        this.data = data;
    }

    public void setMsgEnum(ApiMsgEnum msgEnum) {
        this.code = msgEnum.getCode();
        this.message = msgEnum.getMessage();
        this.data = "";
    }
}

