package com.yannqing.mackradio.common;

import lombok.Data;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

/**
 * 常量类
 */
@Data
@Configuration
public class Constant {

    /**
     * 允许匿名访问的路径
     */
    public static String[] annos = {
            "/user/register",
            "/user/login",
            "/user/logout",
            "/ws"
    };
    public static List<String> annosList = Arrays.asList(annos);

    /**
     * token过期时间
     */
    public static final Integer tokenExpireTime = 60 * 60 * 24 * 3;

}
