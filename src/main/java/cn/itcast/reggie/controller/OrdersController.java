package cn.itcast.reggie.controller;

import cn.itcast.reggie.common.R;
import cn.itcast.reggie.domain.Orders;
import cn.itcast.reggie.domain.User;
import cn.itcast.reggie.dto.OrdersDto;
import cn.itcast.reggie.service.OrdersDtoService;
import cn.itcast.reggie.service.OrdersService;
import cn.itcast.reggie.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/order")
public class OrdersController {
    @Autowired
    private OrdersService ordersService;
    @Autowired
    private OrdersDtoService ordersDtoService;
    @Autowired
    private UserService userService;
    /**
     * 订单--分页查询
     *
     * @param page
     * @param pageSize
     * @param number
     * @param beginTime
     * @param endTime
     * @return
     */
    @GetMapping("/page")
    public R<Page<OrdersDto>> pageOrder(Integer page, Integer pageSize, String number, @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")Date beginTime, @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime) {
//        log.info("订单分页查询：page：{}，pageSize：{}，number：{}，beginTime：{}，endTime：{}",page,pageSize,beginTime,endTime);
//        //构建分页构造器
//        Page<Orders> ordersPage = new Page<>(page,pageSize);
//
//        //构建条件查询构造器
//        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.like(number!=null,Orders::getNumber,number)
//                .between(beginTime!=null&endTime!=null,Orders::getOrderTime,beginTime,endTime);
//        //最后，调用接口方法执行分页查询
//        this.ordersService.page(ordersPage,queryWrapper);
//        return R.success(ordersPage);
        Page<OrdersDto> ordersDtoPage = ordersDtoService.pageOrder(page,pageSize,number,beginTime,endTime);
        return R.success(ordersDtoPage);
    }

    /**
     * 修改订单状态
     * JSON数据
     * 直接修改
     * @param orders
     * @return
     */
    @PutMapping
    public R<String> updateOrders(@RequestBody Orders orders){
        log.info("订单状态：{}",orders);
        this.ordersService.updateById(orders);
        return R.success("修改成功");
    }

    /**
     * 提交订单
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submitOrder(@RequestBody Orders orders){
        ordersService.submit(orders);
        return R.success("下单成功");
    }

    /**
     * 订单信息
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/userPage")
    public R<Page<OrdersDto>> userPage(Integer page , Integer pageSize){
        Page<OrdersDto> ordersPage =ordersService.userPage(page,pageSize);
        return R.success(ordersPage);
    }

    /**
     *    A:
     * 再来一单
     * @param orders
     * @return
     */
    @PostMapping("/again")
    public R<String> again(@RequestBody Orders orders){
        Orders temp = ordersService.getById(orders.getId());
        temp.setId(null);
        temp.setStatus(2);
        long orderId = IdWorker.getId(); // 订单号
        temp.setNumber(String.valueOf(orderId));
        temp.setOrderTime(LocalDateTime.now());
        temp.setCheckoutTime(LocalDateTime.now());
        ordersService.save(temp);
        return R.success("下单成功");
    }
/*
   //  B:
    @PostMapping("/again")
    public R<Orders> againShopping(@RequestBody Orders orders){
        log.info("再次购买：{}",orders);
        Orders orders1 = ordersService.again(orders);
        return R.success(orders1);
    }
*/

}
