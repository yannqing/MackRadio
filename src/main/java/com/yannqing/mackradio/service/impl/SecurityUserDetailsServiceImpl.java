package com.yannqing.mackradio.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yannqing.mackradio.domain.User;
import com.yannqing.mackradio.mapper.UserMapper;
import com.yannqing.mackradio.vo.SecurityUser;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SecurityUserDetailsServiceImpl implements UserDetailsService {

    @Resource
    private UserMapper userMapper;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //获取到用户的全部信息
//        User user = userDao.getUserByUsername(username);
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        //获取用户role角色
        int rid = 0;
        SecurityUser securityUser = new SecurityUser(user,rid);
        securityUser.setSimpleGrantedAuthorities(null);
        return securityUser;
    }
}
