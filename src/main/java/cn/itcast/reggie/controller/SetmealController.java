package cn.itcast.reggie.controller;

import cn.itcast.reggie.common.R;
import cn.itcast.reggie.domain.Setmeal;
import cn.itcast.reggie.dto.SetmealDto;
import cn.itcast.reggie.service.SetmealService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 套餐管理
 */
@RestController
@Slf4j
@RequestMapping("/setmeal")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    /**
     * 添加套餐
     *
     * @param setmealDto
     * @return
     */
    @PostMapping
    public R<String> addSetMeal(@RequestBody SetmealDto setmealDto) {
        log.info("setMeal:{}", setmealDto);
        setmealService.addSetMeal(setmealDto);
        return R.success("添加成功");
    }

    /**
     * 分页查询
     * 分页查询完成之后，会将数据以页面形式回显到前端页面
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page<SetmealDto>> pageSetmealDto(Integer page, Integer pageSize, String name) {

        //创建分类构造器对象,在用 套餐的业务层接口 调用 业务层中的 分页方法
        Page<SetmealDto> setMealDtoPage = this.setmealService.pageSetMeal(page, pageSize, name);
        return R.success(setMealDtoPage);
    }

    @PostMapping("/status/{status}")
    public R<String> update(@PathVariable Integer status, @RequestParam List<Long> ids) {
        log.info("status:{},ids:{}", status, ids);
        //条件构造器
        LambdaUpdateWrapper<Setmeal> updateWrapper = new LambdaUpdateWrapper<>();
        //对应执行的SQL语句
        // ，update 表名 set status = status where id in(ids);--->修改字段重新设置值
        updateWrapper.set(Setmeal::getStatus, status).in(Setmeal::getId, ids);
        // 调用接口，将修改的值进行修改
        this.setmealService.update(updateWrapper);
        return R.success("修改成功");
    }

    /**
     * 删除套餐
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids) {
        log.info("ids:{}", ids);
        //执行业务层接口的删除方法
        this.setmealService.removeWithDish(ids);
        return R.success("删除成功");
    }

    @GetMapping("/list")
    public R<List<Setmeal>> list( Setmeal setmeal) {
        //查询构造器
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        //以套餐id和状态去匹配查询，条件--》套餐id与状态不可为空
        queryWrapper.eq(setmeal.getCategoryId() != null, Setmeal::getCategoryId, setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus() != null, Setmeal::getStatus, setmeal.getStatus());
        List<Setmeal> list = setmealService.list(queryWrapper);
        return R.success(list);
    }
}
