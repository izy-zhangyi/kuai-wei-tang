package cn.itcast.reggie.service;

import cn.itcast.reggie.dto.OrdersDto;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Date;

public interface OrdersDtoService extends IService<OrdersDto> {
    Page<OrdersDto> pageOrder(Integer page, Integer pageSize, String number, Date beginTime, Date endTime);
}
