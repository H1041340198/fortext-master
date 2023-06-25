package com.nplat.convert.service;

import com.nplat.convert.config.UserNotFoundException;
import com.nplat.convert.entity.entity.User;
import com.nplat.convert.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Objects;

@Slf4j
@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;




    public User getInfoByEmailAndPassword(String email, String password) {
        return userMapper.getInfoByEmailAndPassword(email, password);
    }


    public User getInfoByEmail(String email) {
        return userMapper.getInfoByEmail(email);
    }


    public User getInfoById(Long id) {
        User user = userMapper.selectById(id);
        if (Objects.isNull(user)) {
            throw new UserNotFoundException("用户未找到");
        } else {
            return user;
        }
    }




}
