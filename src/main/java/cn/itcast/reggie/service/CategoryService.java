package cn.itcast.reggie.service;

import cn.itcast.reggie.domain.Category;
import com.baomidou.mybatisplus.extension.service.IService;

public interface CategoryService extends IService<Category> {
    void deleteById(Long id);
}
