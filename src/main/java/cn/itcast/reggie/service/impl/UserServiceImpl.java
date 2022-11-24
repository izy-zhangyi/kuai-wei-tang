package cn.itcast.reggie.service.impl;

import cn.itcast.reggie.domain.User;
import cn.itcast.reggie.mapper.UserMapper;
import cn.itcast.reggie.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
