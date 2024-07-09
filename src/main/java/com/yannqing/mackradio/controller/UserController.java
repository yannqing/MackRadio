package com.yannqing.mackradio.controller;

import com.yannqing.mackradio.common.Code;
import com.yannqing.mackradio.domain.User;
import com.yannqing.mackradio.service.UserService;
import com.yannqing.mackradio.utils.ResultUtils;
import com.yannqing.mackradio.vo.BaseResponse;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.HttpMediaTypeException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/login")
    public BaseResponse<User> login(String username, String password, HttpServletRequest request) {
        User loginUser = userService.login(username, password, request);
        return ResultUtils.success(Code.SUCCESS, loginUser, "登录成功");
    }

    @PostMapping("/register")
    public BaseResponse<Object> register(String username, String password, String checkPassword) {
        long userId = userService.userRegister(username, password, checkPassword);
        return ResultUtils.success(Code.SUCCESS, userId, "注册成功");
    }

    @PostMapping("/logout")
    public BaseResponse<Object> logout(HttpServletRequest request) {
        int result = userService.userLogout(request);
        return ResultUtils.success(Code.SUCCESS, result, "退出成功");
    }
}
