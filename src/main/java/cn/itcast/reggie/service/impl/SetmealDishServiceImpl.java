package cn.itcast.reggie.service.impl;

import cn.itcast.reggie.domain.SetmealDish;
import cn.itcast.reggie.mapper.SetmealDishMapper;
import cn.itcast.reggie.service.SetmealDishService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SetmealDishServiceImpl extends ServiceImpl<SetmealDishMapper, SetmealDish> implements SetmealDishService {
}
