package com.kwt.service.impl;

import com.kwt.domain.OrderDetail;
import com.kwt.mapper.OrderDetailMapper;
import com.kwt.service.OrderDetailService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail> implements OrderDetailService {
}
