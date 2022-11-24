package cn.itcast.reggie.service.impl;

import cn.itcast.reggie.domain.Category;
import cn.itcast.reggie.domain.Dish;
import cn.itcast.reggie.domain.Setmeal;
import cn.itcast.reggie.exception.BusinessException;
import cn.itcast.reggie.mapper.CategoryMapper;
import cn.itcast.reggie.mapper.DishMapper;
import cn.itcast.reggie.mapper.SetmealMapper;
import cn.itcast.reggie.service.CategoryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    @Override
    public void deleteById(Long id) {
        //先查询好分类(Category)与菜品的关系
        Long count = dishMapper.selectCount(new LambdaQueryWrapper<Dish>().eq(Dish::getCategoryId, id));
        if (count > 0) {
            //该分类下有菜品
            //抛出异常
            throw new BusinessException("该分类下有菜品，无法删除");
        }
        Long count1 = setmealMapper.selectCount(new LambdaQueryWrapper<Setmeal>().eq(Setmeal::getCategoryId, id));
        if (count1> 0) {
            //该分类下有套餐
            //抛出异常
            throw new BusinessException("该分类下有套餐，无法删除");
        }
        this.removeById(id);
    }
}
