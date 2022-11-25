package cn.itcast.reggie.service.impl;

import cn.itcast.reggie.common.ReggieContext;
import cn.itcast.reggie.domain.*;
import cn.itcast.reggie.dto.OrdersDto;
import cn.itcast.reggie.exception.BusinessException;
import cn.itcast.reggie.mapper.AddressBookMapper;
import cn.itcast.reggie.mapper.OrdersMapper;
import cn.itcast.reggie.mapper.ShoppingCartMapper;
import cn.itcast.reggie.mapper.UserMapper;
import cn.itcast.reggie.service.OrderDetailService;
import cn.itcast.reggie.service.OrdersService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {

    @Autowired
    private OrderDetailService orderDetailService;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submit(Orders orders) {
        //插入一条orders记录
        //插入对应的OrdersDetail
        //补充orders的其他字段
        //addressBookId,payMethod,remark
        //原子性
        //地址簿
        Long userId = ReggieContext.get();
        User user = userMapper.selectById(userId);
        Long id = IdWorker.getId();
        AddressBook addressBook = addressBookMapper.selectById(orders.getAddressBookId());


        //查询购物车
        List<ShoppingCart> shoppingCartList = shoppingCartMapper
                .selectList(new LambdaQueryWrapper<ShoppingCart>().eq(ShoppingCart::getUserId,userId));
        if(CollectionUtils.isEmpty(shoppingCartList)){
            throw new BusinessException("购物车为空，不能下单");
        }
        //计算amount
        //顺便构建咱们的OrderDetail
        List<OrderDetail> orderDetailList = new ArrayList<>();
        AtomicInteger amount = new AtomicInteger(0);
        shoppingCartList.forEach(cart->{
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(id);
            orderDetail.setNumber(cart.getNumber());
            orderDetail.setDishFlavor(cart.getDishFlavor());
            orderDetail.setDishId(cart.getDishId());
            orderDetail.setSetmealId(cart.getSetmealId());
            orderDetail.setName(cart.getName());
            orderDetail.setImage(cart.getImage());
            orderDetail.setAmount(cart.getAmount());
            amount.addAndGet(cart.getAmount().multiply(new BigDecimal(cart.getNumber())
                    .multiply(new BigDecimal("100"))).intValue());
            orderDetailList.add(orderDetail);
        });
        orders.setId(id);
        orders.setAddress(addressBook.getDetail());
        orders.setOrderTime(LocalDateTime.now());
        orders.setConsignee(addressBook.getConsignee());
        //正常情况，先下单，再去让用户支付
        //支付成功后再回头来改订单状态、支付时间
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setNumber("RGOD"+id);
        orders.setPhone(addressBook.getPhone());
        orders.setStatus(2);
        orders.setUserId(userId);
        orders.setUserName(user.getName());
        //一会再说amount
        //如果要计算amount，要根据购物车的数据来算
        orders.setAmount(new BigDecimal(amount.get()).divide(new BigDecimal("100"),2, RoundingMode.HALF_UP));
        this.save(orders);

        this.orderDetailService.saveBatch(orderDetailList);
        //清空购物车
        this.shoppingCartMapper.deleteBatchIds(shoppingCartList.stream().map(ShoppingCart::getId).collect(Collectors.toSet()));
    }

    @Override
    public Page<OrdersDto> userPage(Integer page, Integer pageSize) {
        /**
         * 先行查询出父类中的分页信息
         */
        //构造分页构造器
        Page<Orders> ordersPage = new Page<>(page,pageSize);

        //构造条件查询构造器
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Orders::getOrderTime);
        this.page(ordersPage,queryWrapper);

        /**
         * 构造子类分页构造器（OrderDto）
         * 接着将父类中的分页数据先行拷贝到子类的分页中
         */
        Page<OrderDetail> orderDetailPage = new Page<>();
        BeanUtils.copyProperties(ordersPage,orderDetailPage,"records");

        List<Orders> ordersList = ordersPage.getRecords();

        return null;
    }
}
