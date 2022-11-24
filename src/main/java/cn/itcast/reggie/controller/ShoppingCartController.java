package cn.itcast.reggie.controller;

import cn.itcast.reggie.common.R;
import cn.itcast.reggie.domain.ShoppingCart;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/shoppingCart")
public class ShoppingCartController {

    @GetMapping("/list")
    public R<List<ShoppingCart>> getShoppingCartList(){
        return R.success(new ArrayList<>());
    }
}
