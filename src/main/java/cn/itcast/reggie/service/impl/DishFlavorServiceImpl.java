package cn.itcast.reggie.service.impl;

import cn.itcast.reggie.domain.DishFlavor;

import cn.itcast.reggie.mapper.DishFlavorMapper;
import cn.itcast.reggie.service.DishFlavorService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;


@Service
public class DishFlavorServiceImpl extends ServiceImpl<DishFlavorMapper, DishFlavor> implements DishFlavorService{
}
