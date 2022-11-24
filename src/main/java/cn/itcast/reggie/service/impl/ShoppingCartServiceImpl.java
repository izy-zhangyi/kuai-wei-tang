package cn.itcast.reggie.service.impl;

import cn.itcast.reggie.domain.ShoppingCart;
import cn.itcast.reggie.mapper.ShoppingCartMapper;
import cn.itcast.reggie.service.ShoppingCartService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {
}
