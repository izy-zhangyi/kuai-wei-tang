package com.kwt.service;

import com.kwt.domain.Category;
import com.baomidou.mybatisplus.extension.service.IService;

public interface CategoryService extends IService<Category> {
    void deleteById(Long id);
}
