package com.yannqing.mackradio.vo;

import com.yannqing.mackradio.domain.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginVo {

    private Integer id;
    private String username;
    private String password;
    private String gender;
    private String phone;
    private String email;
    private int userStatus;
    private int accessTimes;
    private String token;
    private int role;

    public LoginVo(User user, String token, int role) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.phone = user.getPhone();
        this.email = user.getEmail();
        this.userStatus = user.getUserStatus();
        this.accessTimes = user.getAccessTimes();
        this.token = token;
        this.role = role;
    }

}
