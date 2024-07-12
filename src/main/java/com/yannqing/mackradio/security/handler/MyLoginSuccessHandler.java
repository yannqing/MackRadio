package com.yannqing.mackradio.security.handler;

import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yannqing.mackradio.common.Code;
import com.yannqing.mackradio.domain.User;
import com.yannqing.mackradio.utils.JwtUtils;
import com.yannqing.mackradio.utils.RedisCache;
import com.yannqing.mackradio.utils.ResultUtils;
import com.yannqing.mackradio.vo.LoginVo;
import com.yannqing.mackradio.vo.SecurityUser;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.yannqing.mackradio.common.Constant.tokenExpireTime;

@Slf4j
public class MyLoginSuccessHandler implements AuthenticationSuccessHandler {


    RedisCache redisCache;


    public MyLoginSuccessHandler(RedisCache redisCache) {
        this.redisCache = redisCache;
    }

    /**
     * 登录成功处理器：返回用户信息，对应用户的权限信息，登录生成token
     * @param request
     * @param response
     * @param authentication
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        response.setContentType("application/json;charset=UTF-8");

        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
        User user = securityUser.getUser();
        ObjectMapper objectMapper = new ObjectMapper();
        String userInfo = objectMapper.writeValueAsString(user);

        //生成token
        String token = JwtUtils.token(userInfo);

        //查看此用户之前是否登录未退出
        String oldToken = redisCache.getCacheObject("token:" + user.getId());
        if (oldToken != null) {
            redisCache.deleteObject("token:" + user.getId());
        }

        //将token存入redis中
        redisCache.setCacheObject("token:"+user.getId(), token,tokenExpireTime, TimeUnit.SECONDS);

        LoginVo userInfoVo = new LoginVo(user,token,securityUser.getRole());

        response.getWriter().write(JSONUtil.toJsonStr(ResultUtils.success(Code.LOGIN_SUCCESS, userInfoVo,"登录成功")));
        log.info("登录成功！");
    }
}
