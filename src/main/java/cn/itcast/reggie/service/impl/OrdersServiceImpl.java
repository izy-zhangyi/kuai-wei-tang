package cn.itcast.reggie.service.impl;

import cn.itcast.reggie.common.R;
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
import cn.itcast.reggie.service.UserService;
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
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {

    @Autowired
    private OrderDetailService orderDetailService;
    @Autowired
    private OrdersService ordersService;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserService userService;
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

    /**
     * 订单详细信息
     * @param page
     * @param pageSize
     * @return
     */
    @Override
    public Page<OrdersDto> userPage(Integer page, Integer pageSize) {
        /**
         * 先行查询出父类中的分页信息
         */
        //构造分页构造器
        Page<Orders> ordersPage = new Page<>(page,pageSize);

        /**
         * 构造子类分页构造器（OrderDto）
         * 接着将父类中的分页数据先行拷贝到子类的分页中
         */
        Page<OrdersDto> ordersDtoPage = new Page<>(page,pageSize);
        //先行创建 ordersDto 的集合对象，用来保存拷贝的数据
        List<OrdersDto> ordersDtoList = new ArrayList<>();

        //构造条件查询构造器,通过用户登录的id 获取数据
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getUserId,ReggieContext.get()).orderByDesc(Orders::getOrderTime);
        //将--》通过用户id获取到的数据--》 联合Orders 的分页数据-->调用IService中的分页方法--》 去构造一个新的Orders的分页构造器
        this.page(ordersPage, queryWrapper);
        /**
         * 查询完成之后，调用分页方法去分页，最后在封装成对象
         * 通过 这个新的分页构造器对象，调用Orders 中的 getRecords方法
         * 这就可以拿到-->查询出来的-->实体类（Orders）的-->新的list分页的数据了
         */
        List<Orders> ordersList = ordersPage.getRecords();
        /**
         * 获取 orders 中的id
         * 为了避免拿到重复id数据，要转map，最后要用map集合接收
         */
        Set<Long> orderIds = ordersList.stream().map(Orders::getId).collect(Collectors.toSet());
        //拿到Orders的id，就可以拿到,orders在 orderDetails中对应的数据
        List<OrderDetail> orderDetailList = orderDetailService.list(new LambdaQueryWrapper<OrderDetail>().in(OrderDetail::getOrderId, orderIds));
       //将拿到的 order在 orderDetails中对应的数据 转map
        Map<Long, List<OrderDetail>> map = orderDetailList.stream().collect(Collectors.groupingBy(OrderDetail::getOrderId));

        //遍历+处理数据
        ordersList.forEach(orders -> {

            /**
             * ordersDto中有orders所没有的一些字段
             * ordersDto继承了orders，所以要将数据拷贝到ordersDto
             * 将orders中的数据拷贝到ordersDto中
             * 拷贝---》将拷贝的数据封装---》new ordersDto
             */
            OrdersDto ordersDto = new OrdersDto();
            BeanUtils.copyProperties(orders,ordersDto);

            //要处理的数据---》在转换的map集合中
            List<OrderDetail> orDefault = map.getOrDefault(ordersDto.getId(), new ArrayList<>());
            //将处理好的数据 放入 ordersDto中
            ordersDto.setOrderDetails(orDefault);
            //最后，将ordersDto中所有拷贝的数据保存到ordersDto的集合中
            ordersDtoList.add(ordersDto);

        });
        //之后，再将所有数据都放入分页中（所有数据都保存到ordersDto的list中了）
        ordersDtoPage.setRecords(ordersDtoList);
        //将分页数据回显到页面--》就可以看到订单信息了
        return ordersDtoPage;
    }

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
        if (CollectionUtils.isEmpty(records)) {
            return ordersDtoPage;
        }
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
    /**
     *  B:
     * @param orders
     * @return
     */
//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public Orders again(Orders orders) {
//        //通过orders的id，可以获取到一天数据，--封装成对象
//        Orders orders1 = this.getById(orders.getId());
//        //判断 里面有没有数据
//        if (orders1 == null) {
//            throw new BusinessException("没有数据");
//        }
//        /**
//         * 没有数据
//         * 就可以执行添加了
//         * 添加之后保存
//         */
//        long id = IdWorker.getId();
//        long ordersDetailId = IdWorker.getId(); //订单id
//        orders1.setId(id);
//        orders1.setNumber(String.valueOf(id));
//        orders1.setStatus(2);
//        orders1.setOrderTime(LocalDateTime.now());
//        this.save(orders1);
//
//        //构造OrdersDetail的条件查询构造器, 通过 Orders中的id去获取OrdersDetail的中信息（orders ，orders_detail)
//        LambdaQueryWrapper<OrderDetail> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(OrderDetail::getOrderId,orders.getId());
//        OrderDetail one = orderDetailService.getOne(queryWrapper);
//        if (one == null) {
//            throw new BusinessException("查无此数据");
//        }
//        one.setId(ordersDetailId);
//        one.setOrderId(one.getId());
//        orderDetailService.save(one);
//        return orders;
//    }

}
