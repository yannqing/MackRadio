package com.yannqing.mackradio.utils.task;

import com.yannqing.mackradio.domain.User;
import com.yannqing.mackradio.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class AccessLimit {

    @Resource
    private UserService userService;

    @Scheduled(cron = "0 0 */12 * * ?")
    public void task() {
        List<User> allUsers = userService.list();
        List<User> userList = allUsers.stream().peek(user -> {
            user.setAccessTimes(50);
        }).toList();
        boolean result = userService.saveOrUpdateBatch(userList);
        if (result) {
            log.info("同步所有用户的访问次数为：50");
        }
    }
}
