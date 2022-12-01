package cn.itcast.reggie.mapper;

import cn.itcast.reggie.dto.OrdersDto;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface OrdersDtoMapper extends BaseMapper<OrdersDto> {
}
