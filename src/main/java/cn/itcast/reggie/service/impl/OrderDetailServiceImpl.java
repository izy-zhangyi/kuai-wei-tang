package cn.itcast.reggie.service.impl;

import cn.itcast.reggie.domain.OrderDetail;
import cn.itcast.reggie.mapper.OrderDetailMapper;
import cn.itcast.reggie.service.OrderDetailService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail> implements OrderDetailService {
}
