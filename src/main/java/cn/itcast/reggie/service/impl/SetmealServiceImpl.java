package cn.itcast.reggie.service.impl;

import cn.itcast.reggie.domain.Category;
import cn.itcast.reggie.domain.Setmeal;
import cn.itcast.reggie.domain.SetmealDish;
import cn.itcast.reggie.dto.SetmealDto;
import cn.itcast.reggie.exception.BusinessException;
import cn.itcast.reggie.mapper.SetmealMapper;
import cn.itcast.reggie.service.CategoryService;
import cn.itcast.reggie.service.SetmealDishService;
import cn.itcast.reggie.service.SetmealService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private CategoryService categoryService;

    /**
     * 新增套餐基本信息，同时需要保存套餐和菜品的关联关系
     * 关联关系：id关联
     * A. 保存套餐基本信息
     *
     * B. 获取套餐关联的菜品集合，并为集合中的每一个元素赋值套餐ID(setmealId)
     *
     * C. 批量保存套餐关联的菜品集合
     * @param setmealDto
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addSetMeal(SetmealDto setmealDto) {
        /**
         *  A：先将 套餐分类存储起来,如果没有继承关系，就要手动拷贝一个实体类对象
         *   保存套餐的基本信息，操作 setmeal表 去执行 insert操作
         */
        this.save(setmealDto);

        /**
         * 构建setmealDto和setmealDish的关系
         *     B. 获取套餐关联的菜品集合，并为集合中的每一个元素赋值套餐ID(setmealId)
         */
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> {
            //遍历集合，将 setmealDto 中的id 放入 集合中去
            setmealDish.setSetmealId(setmealDto.getId());
        });

        //之后，保存 套餐 和 菜品 的 关联信息，操作setmeal_dish表，去执行insert 批量添加的操作
        //调用 套餐菜品关系表的业务层接口保存数据
        this.setmealDishService.saveBatch(setmealDishes);
    }



    /**
     * 套餐分页查询
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @Override
    public Page<SetmealDto> pageSetMeal(Integer page, Integer pageSize, String name) {
        /** A:
         * 先对 Setmeal 分页
         * 对套餐分页
         * 要进行分页查询
         * 1、创建分页构造器
         */
        Page<Setmeal> setmealPage = new Page<>(page, pageSize);
        /**
         * 对套餐（setmeal）分页中还可以使用模糊分页查询
         * 1、创建查询构造器
         * 进行模糊查询
         * 之后，排序
         * ,调IService()中的方法分页
         * 至此，Page(Setmeal)分页完成
         */
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(name),Setmeal::getName,name);
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        this.page(setmealPage,queryWrapper);

        /** B:
         *   此方法核心就是 将Page(Setmeal) 转换成 Page(SetmealDto)
         * 转换数据，实际上就是转换里面的 records变量
         * List<T> records : 代表着就是 SetmealDto
         * 1、创建 SetmealDto分页构造器对象
         */
        Page<SetmealDto> setmealDtoPage = new Page<>();

        //拷贝数据 ，将 Page(Setmeal)中的分页数据，先行拷贝到 Page(SetmealDto)中
        BeanUtils.copyProperties(setmealPage,setmealDtoPage);

        /**
         * 拷贝分页完数据之后，创建一个 SetmealDto 的集合对象
         * 用来存放入 setmealDto 中的 setmeal 中的数据
         *
         * 可用 list集合 获取 setmeal中所有数据
         * 用Page(Setmeal)的分页构造器对象，调用getRecords方法
         * 就可以 获取 通过分页 查出的所有的 数据 到list集合中
         * Records：代表dto ;
         * 转换数据，实际上就是转换里面的 records变量
         * List<T> records : 代表着就是 SetmealDto
         */

        List<SetmealDto> setmealDtoList = new ArrayList<>();
        List<Setmeal> setmealList = setmealPage.getRecords();
        setmealDtoPage.setRecords(setmealDtoList);
        /**
         * stream().map(id)流 查出Page(Setmeal)中所有对应的分类
         * 就是拿到了 所有分类的id
         * 过滤条件：过滤所有为空的id
         * 为了避免重复，使用Set集合存，
         */
        Set<Long> categoryIdSet = setmealList.stream()
                .map(Setmeal::getCategoryId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        /**
         * 拿到所有分类的id之后
         * 就可以拿到所有的（菜品分类）category 对象,(category的list集合对象)
         */
        List<Category> categoryList = this.categoryService.listByIds(categoryIdSet);
        /**
         * 拿到这两个list对象之后，就要想着怎么把他们匹配上
         * 建议方法：
         *        a: jdk8 流式运算
         *        b: list 转 map
         */
        //jdk 8 流式运算
        setmealList.forEach(setmeal -> {
            /**
             * 通过foreach 遍历setmeal的list集合 ，就可以拿到 setmeal中的所有数据
             * 拿到数据之后，就要将数据拷贝到 setmealDto 的集合中去
             * 如何拷贝：------》要想将 setmeal中的数据 拷贝到 setmealDto 中，
             * 首先：获取 setmealDto对象，用来封装拷贝的数据
             * 拷贝完成之后，将数据 添加到 setmealDtoList 集合中去
             */
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(setmeal,setmealDto);
            setmealDtoList.add(setmealDto);
            /**
             * 之后，处理 categoryName:(菜品分类名称)
             * important： 实际开发，不允许在循环内操作数据库
             */
            categoryList.stream()
                    .filter(category -> category.getId().equals(setmeal.getCategoryId()))
                    .findAny().ifPresent(category -> {
                setmealDto.setCategoryName(category.getName());
            });
        });
        return setmealDtoPage;
    }

    /**
     * 套餐删除
     * 删除套餐，要同时删除套餐和菜品的关联数据
     * @param ids
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeWithDish(List<Long> ids) {

        /**
         * 删除之前，查询套餐状态，是否可被删除
         * 通过id查询到数据，通过状态判断是否可被删除
         */
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId,ids);
        queryWrapper.eq(Setmeal::getStatus,1);

        /**
         * 获得所有的查询结果
         * count接收
         */

        long count = this.count(queryWrapper);
        if (count >0) {
            throw new BusinessException("已有在售套餐，无法删除");
        }

        //先删除套餐
        this.removeByIds(ids);

        //之后再删除套餐对象的菜品关系表
        setmealDishService.remove(new LambdaQueryWrapper<SetmealDish>().in(SetmealDish::getDishId,ids));
/*
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(SetmealDish::getSetmealId,ids);
        //删除关系表中的数据----setmeal_dish
        setmealDishService.remove(lambdaQueryWrapper);
 */
    }
/*
    *//**
     * 1、修改数据
     * 2、修改状态，
     * @param status
     * @param ids
     *//*
    @Override
    public void updateSetMeal(Integer status, List<Long> ids) {
        //通过id查出要修改的字段的数据
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ids !=null,Setmeal::getId,ids);
        List<SetmealDish> list = setmealDishService.list(queryWrapper);
    }*/
}
