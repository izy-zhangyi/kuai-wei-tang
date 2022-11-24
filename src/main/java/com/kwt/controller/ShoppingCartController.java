package com.kwt.controller;

import com.kwt.common.R;
import com.kwt.common.ReggieContext;
import com.kwt.domain.ShoppingCart;
import com.kwt.service.ShoppingCartService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/shoppingCart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 用户端购物车数据显示
     * @return
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> getShoppingCartList(){
        //获取登录之后页面展示的信息，
        /**
         * 1.通过登录用户的id，获取登录之后的页面信息，购物车信息
         * 要查询购物车信息，--》构造器形参为购物车
         */
        Long userId = ReggieContext.get();

        //构造查询构造器
        LambdaQueryWrapper<ShoppingCart> queryWrapper= new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,userId).orderByDesc(ShoppingCart::getCreateTime);
        //将数据封装进集合,从而将数据回显给前端页面
        List<ShoppingCart> shoppingCartList = shoppingCartService.list(queryWrapper);
        return R.success(shoppingCartList);
    }

    /**
     * 购物车添加菜品
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    public R<ShoppingCart> addShoppingCart(@RequestBody ShoppingCart shoppingCart){
        log.info("shoppingCart:{}",shoppingCart);
       ShoppingCart cart= shoppingCartService.addShoppingCart(shoppingCart);
        return R.success(cart);
    }

    /**
     * 修改购物车中菜品数量
     * @param shoppingCart
     * @return
     */
    @PostMapping("/sub")
    public R<ShoppingCart> reduceShoppingCart(@RequestBody ShoppingCart shoppingCart){
        log.info("shoppingCart:{}",shoppingCart);
        ShoppingCart cart = shoppingCartService.reduceShoppingCartNum(shoppingCart);
        return R.success(cart);
    }

    /**
     * 清空购物车
     * @return
     */
    @DeleteMapping("/clean")
    public R<String> cleanShoppingCart(){
        //获取用户登录的id
        Long userId = ReggieContext.get();
        //构建查询构造器
        LambdaQueryWrapper<ShoppingCart> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,userId);
        this.shoppingCartService.remove(queryWrapper);
        return R.success("购物车清空成功");
    }















}
