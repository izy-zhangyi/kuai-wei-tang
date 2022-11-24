package cn.itcast.reggie.dto;

import cn.itcast.reggie.domain.OrderDetail;
import cn.itcast.reggie.domain.Orders;
import lombok.Data;

import java.util.List;

@Data
public class OrdersDto extends Orders {

    private String userName;

    private String phone;

    private String address;

    private String consignee;

    private List<OrderDetail> orderDetails;
	
}
