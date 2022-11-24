package cn.itcast.reggie.service;

import cn.itcast.reggie.domain.Setmeal;
import cn.itcast.reggie.dto.SetmealDto;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    void addSetMeal(SetmealDto setmealDto);

    Page<SetmealDto> pageSetMeal(Integer page, Integer pageSize, String name);

    void removeWithDish(List<Long> ids);

/*    void updateSetMeal(Integer status, List<Long> ids);*/
}
