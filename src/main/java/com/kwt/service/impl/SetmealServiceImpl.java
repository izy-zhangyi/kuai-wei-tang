package com.kwt.service.impl;

import com.kwt.domain.Category;
import com.kwt.domain.Setmeal;
import com.kwt.domain.SetmealDish;
import com.kwt.dto.SetmealDto;
import com.kwt.exception.BusinessException;
import com.kwt.mapper.SetmealMapper;
import com.kwt.service.CategoryService;
import com.kwt.service.SetmealDishService;
import com.kwt.service.SetmealService;
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
     * <p>
     * B. 获取套餐关联的菜品集合，并为集合中的每一个元素赋值套餐ID(setmealId)
     * <p>
     * C. 批量保存套餐关联的菜品集合
     *
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
        queryWrapper.like(StringUtils.isNotBlank(name), Setmeal::getName, name);
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        this.page(setmealPage, queryWrapper);

        /** B:
         *   此方法核心就是 将Page(Setmeal) 转换成 Page(SetmealDto)
         * 转换数据，实际上就是转换里面的 records变量
         * List<T> records : 代表着就是 SetmealDto
         * 1、创建 SetmealDto分页构造器对象
         */
        Page<SetmealDto> setmealDtoPage = new Page<>();

        //拷贝数据 ，将 Page(Setmeal)中的分页数据，先行拷贝到 Page(SetmealDto)中
        BeanUtils.copyProperties(setmealPage, setmealDtoPage);

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
            BeanUtils.copyProperties(setmeal, setmealDto);
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
     *
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
        queryWrapper.in(Setmeal::getId, ids);
        queryWrapper.eq(Setmeal::getStatus, 1);

        /**
         * 获得所有的查询结果
         * count接收
         */

        long count = this.count(queryWrapper);
        if (count > 0) {
            throw new BusinessException("已有在售套餐，无法删除");
        }

        //先删除套餐
        this.removeByIds(ids);

        //之后再删除套餐对象的菜品关系表
        setmealDishService.remove(new LambdaQueryWrapper<SetmealDish>().in(SetmealDish::getDishId, ids));
        /*
                LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
                lambdaQueryWrapper.in(SetmealDish::getSetmealId,ids);
                //删除关系表中的数据----setmeal_dish
                setmealDishService.remove(lambdaQueryWrapper);
         */
    }

    /**
     * 修改套餐中数据，
     * 套餐中包含了套餐分类字段，以及关联的菜品信息，而setmeal 中没有分类与关联的菜品信息，setmetDto中有
     * setmealDto 继承了 setmeal，同时也新增了一些 setmeal中没有的字段
     *
     * 修改套餐信息，不仅要修改 setmeal 中的数据，也要修改 setmeal_dish 表中所对应的菜品数据（多表操作）
     *
     * 总而言之，该方法核心就是 围绕 setmealDto 展开的
     *
     * @param setmealDto
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSetmealData(SetmealDto setmealDto) {
            //先判断是否接收到数据
            if(setmealDto==null){
               throw new BusinessException("请求异常");
            }
            //判断套餐下面是否还有关联菜品
            if(setmealDto.getSetmealDishes()==null){
                throw new BusinessException("套餐没有菜品，请添加");
            }
        /**
         * 修改套餐其他的字段，一般都会根据id进行修改数据
         * 通过id获取setmeal表中要被更新的基本数据
         */
        this.updateById(setmealDto);

        /**
         * 修改套餐的数据，无非就是修改一些基本的信息
         * 而遇到多数据，多个表之间，要做统一修改时，
         * 一般是全部删除，之后再全部添加,这个前提就是这些数据与其他数据没有关系
         * 之后，就可以条件删除，最后就可以添加数据
         * 或者是先查出再删除，之后再添加
         */
        /**
         * 构造查询构造器
         * 先根据id获取 原始的套餐 所对应的 菜品 的数据
         * 查到数据之后，就可以先调用接口方法删除了
         */
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId, setmealDto.getId());
        setmealDishService.remove(queryWrapper);

        /**
         * 删除之后，接着就可以添加数据了
         * 添加数据时，要设置套餐与菜品的关系
         * 套餐中对应的菜品的id，套餐的id
         * setmealDto.getSetmealDishes():代表就是套餐下关联的菜品（套餐中的菜品）
         */
        setmealDto.getSetmealDishes().forEach(setmealDish -> {
            setmealDish.setSetmealId(setmealDto.getId());
        });
        //最后要 重新保存 套餐 所对应的 菜品数据
        setmealDishService.saveBatch(setmealDto.getSetmealDishes());
    }

    /**
     * setmealDto 继承了 setmeal，
     * 同时也新增了一些 setmeal中没有的字段
     * <p>
     * 总而言之，该方法核心就是 围绕 setmealDto 展开的
     *
     * @param id
     * @return
     */
    @Override
    public SetmealDto getDataById(Long id) {
        // 通过id获取套餐数据信息
        Setmeal setmeal = this.getById(id);

        //构造 setmealDto对象，用于封装拷贝的父类中的数据
        SetmealDto setmealDto = new SetmealDto();

        //构造菜品套餐关联的查询构造器，通过id查出关联的菜品信息
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(id != null, SetmealDish::getSetmealId, id);

        //调用接口中的方法，将查出的数据储存到 list集合中
        List<SetmealDish> setmealDishList = setmealDishService.list(queryWrapper);

        //如果根据id获取的套餐数据不为空
        if (setmeal != null) {
            /**
             * 1.
             *  直接拷贝数据，
             *   也就是将setmeal中的数据拷贝到 setmealDto中
             * 2.
             *   直接将查到的菜品数据 也放入 setmealDto中
             * 由于本方法核心就是围绕 setmealDto 展开的
             * 所以最后的所有数据，也都会封装到setmealDto中
             */
            BeanUtils.copyProperties(setmeal, setmealDto);
            setmealDto.setSetmealDishes(setmealDishList);
            return setmealDto; //这边return结果之后,执行就会在此结束，后面的那个return null就不会被执行的到
        }
        return null;
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