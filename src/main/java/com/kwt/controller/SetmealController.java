package com.kwt.controller;

import com.kwt.common.R;
import com.kwt.domain.Setmeal;
import com.kwt.dto.SetmealDto;
import com.kwt.service.SetmealService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 套餐管理
 */
@RestController
@Slf4j
@RequestMapping("/setmeal")
@Api(tags = "套餐相关接口")
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
    @CacheEvict(value = "setmealCache", allEntries = true)
    @ApiOperation(value = "新增套餐接口")
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
    @ApiOperation(value = "套餐分页查询接口")
    @ApiImplicitParams({@ApiImplicitParam(name = "page", value = "页码", required = true),
            @ApiImplicitParam(name = "pageSzie", value = "每页记录个数", required = true),
            @ApiImplicitParam(name = "name", value = "套餐名称", required = false),})
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
    @CacheEvict(value = "setmealCache",allEntries = true)
    @ApiOperation(value = "套餐删除接口")
    public R<String> delete(@RequestParam List<Long> ids) {
        log.info("ids:{}", ids);
        //执行业务层接口的删除方法
        this.setmealService.removeWithDish(ids);
        return R.success("删除成功");
    }

    @GetMapping("/list")
    @Cacheable(value = "setmealCache",key = "#setmeal.categoryId + '_' + #setmeal.status")
    @ApiOperation(value = "套餐条件查询接口")
    public R<List<Setmeal>> list(Setmeal setmeal) {
        //查询构造器
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        //以套餐id和状态去匹配查询，条件--》套餐id与状态不可为空
        queryWrapper.eq(setmeal.getCategoryId() != null, Setmeal::getCategoryId, setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus() != null, Setmeal::getStatus, setmeal.getStatus());
        List<Setmeal> list = setmealService.list(queryWrapper);
        return R.success(list);
    }

    @GetMapping("/{id}")
    public R<SetmealDto> getByIdData(@PathVariable Long id) {
/*        数据没有回显，此方法行不通
        LambdaQueryWrapper<Setmeal> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(id!=null,Setmeal::getId,id);
        Setmeal setmeal = setmealService.getOne(queryWrapper);
        return R.success(setmeal);

 */
        SetmealDto setmealDto = setmealService.getDataById(id);
        return R.success(setmealDto);
    }

    @PutMapping
    public R<String> updateSetmealData(@RequestBody SetmealDto setmealDto) {
        this.setmealService.updateSetmealData(setmealDto);
        return R.success("修改成功");
    }
}
