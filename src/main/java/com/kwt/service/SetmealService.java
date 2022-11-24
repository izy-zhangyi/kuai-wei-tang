package com.kwt.service;

import com.kwt.domain.Setmeal;
import com.kwt.dto.SetmealDto;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    void addSetMeal(SetmealDto setmealDto);

    Page<SetmealDto> pageSetMeal(Integer page, Integer pageSize, String name);

    void removeWithDish(List<Long> ids);

    void updateSetmealData(SetmealDto setmealDto);

    SetmealDto getDataById(Long id);



    /*    void updateSetMeal(Integer status, List<Long> ids);*/
}
