package com.yannqing.mackradio.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yannqing.mackradio.domain.User;
import com.yannqing.mackradio.service.UserService;
import com.yannqing.mackradio.mapper.UserMapper;
import com.yannqing.mackradio.utils.RedisCache;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;
import org.springframework.session.web.http.SessionRepositoryFilter;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.yannqing.mackradio.common.UserConstant.USER_LOGIN_STATE;

/**
* @author 67121
* @description 针对表【user】的数据库操作Service实现
* @createDate 2024-06-19 15:17:03
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    @Resource
    private UserMapper userMapper;

    @Resource
    private RedisCache redisCache;

    @Resource
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public long userRegister(String username, String password, String checkPassword) {
        // 1. 校验
        if (StringUtils.isAnyBlank(username, password, checkPassword)) {
            throw new IllegalArgumentException("参数为空");
        }
        if (username.length() < 4) {
            throw new IllegalArgumentException("用户账号过短");
        }
        if (password.length() < 8 || checkPassword.length() < 8) {
            throw new IllegalArgumentException("用户密码过短");
        }
        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(username);
        if (matcher.find()) {
            return -1;
        }
        // 密码和校验密码相同
        if (!password.equals(checkPassword)) {
            return -1;
        }
        // 账户不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        long count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new IllegalArgumentException("账号重复");
        }
        // 2. 加密
        String encryptPassword = passwordEncoder.encode(password);
        // 3. 插入数据
        User user = new User();
        user.setUsername(username);
        user.setPassword(encryptPassword);
        boolean saveResult = this.save(user);
        if (!saveResult) {
            return -1;
        }
        return user.getId();
    }

    @Override
    public User getSafetyUser(User originUser) {
        if (originUser == null) {
            return null;
        }
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setUserStatus(originUser.getUserStatus());
        safetyUser.setCreatedTime(originUser.getCreatedTime());
        safetyUser.setAccessTimes(originUser.getAccessTimes());
        return safetyUser;
    }

    @Override
    public int getAccessTimes(HttpServletRequest request) {
        User loginUser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        return loginUser.getAccessTimes();
    }

    @Override
    public User getInfo(HttpServletRequest request) {
        int userId = Integer.parseInt(request.getHeader("userId"));

        return userMapper.selectById(userId);
    }
}




