package com.bigevent.lab.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bigevent.lab.entity.Category;
import com.bigevent.lab.mapper.CategoryMapper;
import com.bigevent.lab.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 分类Service实现类
 */
@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public List<Category> listByUserId(Integer userId) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getCreateUser, userId)
               .orderByDesc(Category::getCreateTime);
        return categoryMapper.selectList(wrapper);
    }

    @Override
    public void add(Category category, Integer userId) {
        category.setCreateUser(userId);
        categoryMapper.insert(category);
    }

    @Override
    public void update(Category category, Integer userId) {
        Category existing = categoryMapper.selectById(category.getId());
        if (existing == null) {
            throw new RuntimeException("分类不存在");
        }
        if (!existing.getCreateUser().equals(userId)) {
            throw new RuntimeException("无权操作该分类");
        }

        Category updateCategory = new Category();
        updateCategory.setId(category.getId());
        updateCategory.setName(category.getName());
        updateCategory.setAlias(category.getAlias());
        categoryMapper.updateById(updateCategory);
    }

    @Override
    public Category getById(Integer id) {
        return categoryMapper.selectById(id);
    }

    @Override
    public void deleteById(Integer id, Integer userId) {
        Category existing = categoryMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("分类不存在");
        }
        if (!existing.getCreateUser().equals(userId)) {
            throw new RuntimeException("无权操作该分类");
        }
        categoryMapper.deleteById(id);
    }
}
