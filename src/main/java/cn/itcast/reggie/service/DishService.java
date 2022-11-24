package cn.itcast.reggie.service;

import cn.itcast.reggie.domain.Dish;
import cn.itcast.reggie.dto.DishDto;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface DishService extends IService<Dish> {

    void saveWithFlavor(DishDto dishDto);

    Page<DishDto> pageDishByXML(Integer page, Integer pageSize, String name);


    DishDto getDishById(Long id);

    void modifyById(DishDto dishDto);

    List<DishDto> getDishDtoList(Long categoryId, String name,Integer status);

    void deleteById(List<Long> ids);
}

