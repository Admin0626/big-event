package com.bigevent.lab.service;

import com.bigevent.lab.entity.Category;

import java.util.List;

/**
 * 分类Service接口
 */
public interface CategoryService {

    /**
     * 查询当前用户的所有分类，按创建时间倒序
     */
    List<Category> listByUserId(Integer userId);

    /**
     * 新增分类
     */
    void add(Category category, Integer userId);

    /**
     * 更新分类（校验归属权）
     */
    void update(Category category, Integer userId);

    /**
     * 根据ID查询分类详情
     */
    Category getById(Integer id);

    /**
     * 删除分类（校验归属权）
     */
    void deleteById(Integer id, Integer userId);
}
