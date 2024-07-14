package com.yannqing.mackradio.common;

public class Code {

    public static final Integer SUCCESS = 200;              //一般的成功操作
    public static final Integer FAILURE = 500;              //一般的失败操作

    public static final Integer LOGIN_SUCCESS = 20001;      //登录成功
    public static final Integer LOGIN_FAILURE = 20000;      //登录失败
    public static final Integer LOGOUT_SUCCESS = 20010;     //退出成功

    public static final Integer REGISTER_SUCCESS = 30001;   //注册成功
    public static final Integer REGISTER_FAILURE = 30000;   //注册失败

    public static final Integer TOKEN_EXPIRE = 10000;       //token过期
    public static final Integer TOKEN_AUTHENTICATE_FAILURE = 10001;       //token认证失败

    public static final Integer AUTHENTICATE_FAILURE = 50000;       //认证失败

    public static final Integer MESSAGE_TOO_LARGE = 30010;  //用户输入字符过多



}
