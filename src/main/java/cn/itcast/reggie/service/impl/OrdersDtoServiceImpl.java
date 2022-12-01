package cn.itcast.reggie.service.impl;

import cn.itcast.reggie.domain.Orders;
import cn.itcast.reggie.domain.User;
import cn.itcast.reggie.dto.OrdersDto;
import cn.itcast.reggie.mapper.OrdersDtoMapper;
import cn.itcast.reggie.service.OrdersDtoService;
import cn.itcast.reggie.service.OrdersService;
import cn.itcast.reggie.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrdersDtoServiceImpl extends ServiceImpl<OrdersDtoMapper,OrdersDto> implements OrdersDtoService {
    @Autowired
    private OrdersService ordersService;
    @Autowired
    private UserService userService;
    @Override
    public Page<OrdersDto> pageOrder(Integer page, Integer pageSize, String number, Date beginTime, Date endTime) {
        log.info("订单分页查询：page：{}，pageSize：{}，number：{}，beginTime：{}，endTime：{}",page,pageSize,beginTime,endTime);
        //构建Orders分页构造器 以及OrdersDto的分页构造器对象
        Page<Orders> ordersPage = new Page<>(page,pageSize);
        Page<OrdersDto> ordersDtoPage = new Page<>(page,pageSize);//核心
        //先行将orders中的分页数据，拷贝到 ordersDto中去-->records:代表记录拷贝的分页数据
        BeanUtils.copyProperties(ordersPage,ordersDtoPage,"records");
        //构建条件查询构造器
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(number!=null,Orders::getNumber,number)
                .between(beginTime!=null&endTime!=null,Orders::getOrderTime,beginTime,endTime);

        /**
         * 将--》通过构造器查询获取到的数据--》 联合Orders 的分页数据-->调用IService中的分页方法--》 去构造一个新的Orders的分页构造器
         *  查询完成之后，调用分页方法去分页，最后在封装成对象
         */
        Page<Orders> ordersPage1 = ordersService.page(ordersPage, queryWrapper);
        /**
         * 通过 这个新的分页构造器对象，调用Orders 中的 getRecords方法
         * 这就可以拿到-->查询出来的-->实体类（Orders）的-->新的list分页的数据了
         */

        List<Orders> records = ordersPage1.getRecords();
        List<OrdersDto> ordersDtoList = new ArrayList<>();
        /**
         * 之后再新的list集合中去获取 orders 中的 用户登录的id信息
         * 由于id有多个，为了避免拿到重复数据，要 toSet 一下，最后用set集合保存
         */
        Set<Long> orderIds = records.stream().map(Orders::getUserId).collect(Collectors.toSet());
        //拿到orders中的用户的登录的id之后，就可以得到数据-->orders在 orderDto中对应的数据
        //通过用户登录的id拿到所对应的用户订单信息
        List<User> userList = userService.listByIds(orderIds);

        //将拿到的orders在 orderDto中对应的数据 根据ordersDto的id分组处理后 转为 map
        //就是将通过用户登录的id拿到所对应的订单数据根据id分组处理后 转为 map
        Map<Long, User> map = userList.stream().collect(Collectors.toMap(User::getId, Function.identity()));

        /**
         * 之后遍历通过 orders的新的分页数据对象调用 getRecodes拿到的数据---》list集合 对象
         * 接着处理数据
         */
        records.forEach(orders -> {
            /**
             * 遍历之后，要将得到的orders数据 拷贝到 ordersDto中
             * 拷贝完成之后，对数据进行封装
             */
            OrdersDto ordersDto = new OrdersDto();
            BeanUtils.copyProperties(orders,ordersDto);
            ordersDtoList.add(ordersDto);
            /**
             * 封装完拷贝数据之后，紧接着就要处理数据了
             * 要处理的数据在 map 集合中
             * 通过 ordersDto的id，可获取处理的数据
             */
            User orDefault = map.getOrDefault(orders.getUserId(), new User());

            //得到处理好的数据之后，将数据放入 ordersDtode的集合中去
            ordersDto.setUserName(orDefault.getName());

        });
        //最后，调用接口方法执行分页查询
        ordersDtoPage.setRecords(ordersDtoList);
        return ordersDtoPage;
    }
}
