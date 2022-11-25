package cn.itcast.reggie.service;

import cn.itcast.reggie.domain.ShoppingCart;
import com.baomidou.mybatisplus.extension.service.IService;

public interface ShoppingCartService extends IService<ShoppingCart> {
    ShoppingCart addShoppingCart(ShoppingCart shoppingCart);

    ShoppingCart reduceShoppingCartNum(ShoppingCart shoppingCart);
}
