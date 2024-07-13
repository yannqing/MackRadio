package com.yannqing.mackradio.controller;

import com.yannqing.mackradio.common.Code;
import com.yannqing.mackradio.domain.User;
import com.yannqing.mackradio.service.UserService;
import com.yannqing.mackradio.utils.ResultUtils;
import com.yannqing.mackradio.vo.BaseResponse;
import com.yannqing.mackradio.vo.LoginVo;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/register")
    public BaseResponse<Object> register(String username, String password, String checkPassword) {
        long userId = userService.userRegister(username, password, checkPassword);
        return ResultUtils.success(Code.SUCCESS, userId, "注册成功");
    }

    @GetMapping("/getAccessTimes")
    public BaseResponse<Integer> getAccessTimes(HttpServletRequest request) {
        int result = userService.getAccessTimes(request);
        return ResultUtils.success(Code.SUCCESS, result, "获取访问限制次数成功：");
    }

    @GetMapping("/getInfo")
    public BaseResponse<User> getInfo(HttpServletRequest request) {
        User user = userService.getInfo(request);
        return ResultUtils.success(Code.SUCCESS, user, "获取个人信息成功");
    }
}
