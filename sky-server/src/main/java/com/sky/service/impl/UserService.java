package com.sky.service.impl;

import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.result.Result;
import org.springframework.stereotype.Service;

@Service
public interface UserService {
     User wxLogin(UserLoginDTO userLoginDTO);
}
