package cn.itcast.reggie.dto;

import cn.itcast.reggie.domain.Dish;
import cn.itcast.reggie.domain.DishFlavor;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class DishDto extends Dish {

    //菜品口味
    private List<DishFlavor> flavors = new ArrayList<>();

    //菜品分类名称
    private String categoryName;

    private Integer copies;
}
