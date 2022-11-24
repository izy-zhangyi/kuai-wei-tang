package cn.itcast.reggie.controller;

import cn.itcast.reggie.common.R;
import cn.itcast.reggie.domain.Category;
import cn.itcast.reggie.service.CategoryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
@Slf4j
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 新增菜品
     * @param category
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody Category category){
      categoryService.save(category);
      return R.success("新增分类成功");
    }

    /**
     * 对菜品分页查询
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<Page<Category>> pageCategory(Integer page,Integer pageSize){
        //分页构造器
        Page<Category> categoryPage = new Page<>(page,pageSize);

        //条件构造器
        LambdaQueryWrapper<Category> lqw = new LambdaQueryWrapper<>();
        lqw.orderByAsc(Category::getSort);
        //分页查询
        categoryService.page(categoryPage,lqw);
        return R.success(categoryPage);
    }

    /**
     * 删除
     * @param id
     * @return
     */
    @DeleteMapping
    public R<String> deleteById(Long id){
        this.categoryService.deleteById(id);
        return R.success("删除分类成功");
    }

    @PutMapping
    public R<String> updateById(@RequestBody Category category){
        log.info("params:{}",category);
        this.categoryService.updateById(category);
        return R.success("修改分类成功");
    }

    /**
     * 查询所有的菜品类别
     * 菜品管理，新建菜品，菜品分类
     * @param type
     * @return
     */
    @GetMapping("/list")
    public R<List<Category>> list(Integer type){
        List<Category> list = categoryService
                .list(new LambdaQueryWrapper<Category>()
                        .eq(type!=null,Category::getType, type)
                        .orderByDesc(Category::getSort));
        return R.success(list);
    }

}
