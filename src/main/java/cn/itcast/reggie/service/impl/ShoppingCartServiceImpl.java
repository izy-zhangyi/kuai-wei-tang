package cn.itcast.reggie.service.impl;

import cn.itcast.reggie.common.ReggieContext;
import cn.itcast.reggie.domain.ShoppingCart;
import cn.itcast.reggie.exception.BusinessException;
import cn.itcast.reggie.mapper.ShoppingCartMapper;
import cn.itcast.reggie.service.ShoppingCartService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {


    /**
     * 添加购物车
     *
     * @param shoppingCart
     * @return
     */
    @Override
    public ShoppingCart addShoppingCart(ShoppingCart shoppingCart) {
        // 获取用户登录id
        Long userId = ReggieContext.get();
        /**
         * 判断购物车中是否有要添加的菜品信息
         * dishId or setmealId
         * 1.有 dishId 没有 setmealId or 有 setmealId 没有 dishId
         * 2.两种都没有(这种和我们没有关系)
         * 3.两种全都有
         */
        // 构造查询构造器
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId).
                eq(shoppingCart.getDishId() != null, ShoppingCart::getDishId, shoppingCart.getDishId()).
                eq(shoppingCart.getSetmealId() != null, ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        //查询完成之后，将数据封装
        ShoppingCart cart = this.getOne(queryWrapper);
        // 如果购物车中不存在要添加的菜品
        if (cart == null) {
            /**
             * 直接添加进购物车中
             * 通过购物车中对应的用户登录id去添加数据
             * 添加该菜品到购物车中，对应的数量要+1
             * 添加完成之后，保存
             * 最后将数据返回
             */
            shoppingCart.setUserId(userId);
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            this.save(shoppingCart);
            return shoppingCart;
        } else {
            /**
             * 已经存在
             * 在原有的数量上直接+1
             * 之后数据保存
             * 结果返回即可
             * 查到的购物车中的数据，已经全部封装进了 cart 这个对象中了
             * 要拿什么数据，直接用这个对象调用即可
             */
            cart.setNumber(cart.getNumber() + 1);
            this.updateById(cart);
            return cart;
        }
    }

    /**
     * 减少购物车数量
     *
     * @param shoppingCart
     * @return
     */
    @Override
    public ShoppingCart reduceShoppingCartNum(ShoppingCart shoppingCart) {
        /**
         * 购物车中是否存在要该菜品（减少的菜品）
         * 1.存在--》数量—1
         * a.有 dsihId没有setmealId ,有setmealId 没有dishId
         * b.两种都有
         * 2.不存在--》抛出异常
         */
        //获取登录用户的id
        Long userId = ReggieContext.get();

        //构造查询构造器
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);
        queryWrapper.eq(shoppingCart.getDishId() != null, ShoppingCart::getDishId, shoppingCart.getDishId());
        queryWrapper.eq(shoppingCart.getSetmealId() != null, ShoppingCart::getSetmealId, shoppingCart.getSetmealId());

        //将查到的数据封装
        ShoppingCart cart = this.getOne(queryWrapper);
        if (cart == null) {
            // 说明 没有 该菜品，直接抛异常
            throw new BusinessException("购物车不存在菜品");
        } else {
            //
            /**
             * 存在，数量直接—1，
             * a: 最终数量》0
             * b:最终数据=0
             * ，最后保存修改的数据
             */
            if ((cart.getNumber()-1)==0){
                this.remove(new LambdaQueryWrapper<ShoppingCart>().eq(ShoppingCart::getUserId,userId));
                return cart;
            }else {
                cart.setNumber(cart.getNumber()-1);
                this.updateById(cart);
                return cart;
            }
        }
    }

}
