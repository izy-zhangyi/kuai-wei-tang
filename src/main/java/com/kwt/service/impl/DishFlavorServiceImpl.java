package com.kwt.service.impl;

import com.kwt.domain.DishFlavor;

import com.kwt.mapper.DishFlavorMapper;
import com.kwt.service.DishFlavorService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;


@Service
public class DishFlavorServiceImpl extends ServiceImpl<DishFlavorMapper, DishFlavor> implements DishFlavorService{
}
