package cn.itcast.reggie.dto;

import cn.itcast.reggie.domain.Setmeal;
import cn.itcast.reggie.domain.SetmealDish;
import lombok.Data;
import java.util.List;

/**
 * 套餐分类
 */
@Data
public class SetmealDto extends Setmeal {

    /**
     * 套餐关联的菜品集合
     */
    private List<SetmealDish> setmealDishes;

    /**
     * 菜品分类名称
     */
    private String categoryName;
}
