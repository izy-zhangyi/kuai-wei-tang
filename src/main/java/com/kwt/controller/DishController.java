package com.kwt.controller;

import com.kwt.common.R;
import com.kwt.domain.Dish;
import com.kwt.dto.DishDto;
import com.kwt.service.DishService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;

    /**
     * 新增菜品
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> addDish(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());
        dishService.saveWithFlavor(dishDto);
        return R.success("菜品添加成");
    }

    /**
     * 分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page<DishDto>> pageDish(Integer page,Integer pageSize ,String name){
        log.info("page:{}, pageSize:{}, name:{}",page,pageSize,name);
        Page<DishDto> dishDtoPage=dishService.pageDishByXML(page,pageSize,name);
        return R.success(dishDtoPage);
    }

    /**
     * 根据id查询菜品信息和对应的口味信息
     * 菜品信息在 dish表，Dish 类中对应了 dish表 中所有的字段名，除了菜品分类的名称，菜品分类的名称在DishDto类中，
     * DishDto 继承了 Dish,就可以用 Dish 中所有的 方法与变量，这样就可以与 dish表中的所有字段对应起来了
     * 在实现查询时，就可以先将 Dish 中所有的数据内容拷贝到 DishDto中
     * 口味信息在 dish_flavor表
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> getById(@PathVariable Long id){
        DishDto dishDto = this.dishService.getDishById(id);
        return R.success(dishDto);
    }


    /**
    * 根据id查询菜品信息和对应的口味信息
    * 菜品信息在 dish表，Dish 类中对应了 dish表 中所有的字段名，除了菜品分类的名称，菜品分类的名称在DishDto类中，
    * DishDto 继承了 Dish,就可以用 Dish 中所有的 方法与变量，这样就可以与 dish表中的所有字段对应起来了
    * 在实现查询时，就可以先将 Dish 中所有的数据内容拷贝到 DishDto中
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> updateById(@RequestBody DishDto dishDto){
        this.dishService.modifyById(dishDto);
        return R.success("修改成功");
    }

    /**
     * 根据条件查询对应的菜品数据
     * 菜品的id要有，条件查询的条件要有
     * @return
     */
    @GetMapping("/list")
    public R<List<DishDto>> getDishList(Long categoryId,String name,Integer status){

        //查询完之后，调用业务层接口中的list方法，回显数据
        List<DishDto> list = dishService.getDishDtoList(categoryId,name,status);
        return R.success(list);
    }

    /**
     * 根据菜品id，批量修改菜品状态
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> updateDishStatus( @PathVariable Integer status, @RequestParam List<Long> ids){
        log.info("status:{},ids:{}",status,ids);
        // 构造条件修改构造器
        LambdaUpdateWrapper<Dish> updateWrapper = new LambdaUpdateWrapper<>();
        //根据id信息修改菜品状态
        /**
         * 对应SQL语句：
         *          update dish set status = {status} where id in(ids);
         */
        updateWrapper.set(Dish::getStatus,status).in(Dish::getId,ids);
        dishService.update(updateWrapper);
        return R.success("修改成功");
    }

    /**
     * 传的id不可为空
     * 菜品是直接删除的
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> deleteDish(@RequestParam List<Long> ids){
        log.info("ids:{}",ids);
        this.dishService.deleteById(ids);
        return R.success("菜品删除成功");
    }
}
