package com.yannqing.mackradio.service;

import com.yannqing.mackradio.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletRequest;

/**
* @author 67121
* @description 针对表【user】的数据库操作Service
* @createDate 2024-06-19 15:17:03
*/
public interface UserService extends IService<User> {

    long userRegister(String username, String password, String checkPassword);

    User getSafetyUser(User originUser);

    int getAccessTimes(HttpServletRequest request);

    User getInfo(HttpServletRequest request);
}
