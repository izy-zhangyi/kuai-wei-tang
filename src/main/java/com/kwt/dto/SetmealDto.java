package com.kwt.dto;

import com.kwt.domain.Setmeal;
import com.kwt.domain.SetmealDish;
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
