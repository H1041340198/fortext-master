package com.nplat.convert.entity.domain;

public enum KeyWords {

    GUANG_QI_FENG_TIAN(1, "广汽丰田"),
    GUANG_QI_SAN_LING(2, "广汽三菱"),
    LING_KE(3, "领克"),
    QI_YA(4, "起亚"),
    GUANG_QI_CHUAN_QI(5, "广汽传祺"),
    YI_QI_FENG_TIAN(6, "一汽丰田"),
    /*RI_CHAN(5, "日产"),
    SHANG_QI_DA_ZHONG(6, "上汽大众"),
    YI_QI_HONG_QI(7, "一汽红旗"),
    CHANG_CHENG(8, "长城"),
    XUE_TIE_LONG(9, "雪铁龙"),
    RONG_WEI(10, "荣威"),
    MING_JUE(11, "名爵"),*/
//    RI_CHAN(3, "日产")
    ;



    private int code;
    private String message;

    KeyWords(int code, String message) {
        this.code = code;
        this.message = message;
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

    public static KeyWords findByCode(Integer code){
        for(KeyWords keyWords : KeyWords.values()){
            if(keyWords.code == code) {
                return keyWords;
            }
        }
        return null;
    }
}
