package cn.itcast.reggie.service;

import cn.itcast.reggie.common.R;
import cn.itcast.reggie.domain.Orders;
import cn.itcast.reggie.dto.OrdersDto;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

public interface OrdersService extends IService<Orders> {

    void submit(Orders orders);

    Page<OrdersDto> userPage(Integer page, Integer pageSize);

    // B:
    //Orders again(Orders orders);

}
