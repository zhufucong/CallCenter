package com.hzandriod.callcenter.model;

public class ReturnData {
    /*
    * 错误码：1：成功，0：请求URL失败，-1：服务器异常
    *
    * */
    public int type;
    public int errorcode;
    public String message;
    public Object resultdata;
    public void settype(int type) {
        this.type = type;
    }
    public void seterrorcode(int errorcode) {
        this.errorcode = errorcode;
    }
    public void setmessage(String message) {
        this.message = message;
    }
    public void setresultdata(Object resultdata) {
        this.resultdata = resultdata;
    }
}
