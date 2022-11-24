package com.kwt.service.impl;

import com.kwt.domain.User;
import com.kwt.mapper.UserMapper;
import com.kwt.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
