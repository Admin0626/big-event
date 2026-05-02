package com.bigevent.lab.controller;

import com.bigevent.lab.common.Result;
import com.bigevent.lab.entity.Category;
import com.bigevent.lab.service.CategoryService;
import com.bigevent.lab.utils.ThreadLocalUtil;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类控制器
 */
@RestController
@RequestMapping("/category")
@Validated
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * GET /category
     * 查询当前用户的分类列表，按创建时间倒序
     */
    @GetMapping
    public Result<List<Category>> list() {
        Integer userId = ThreadLocalUtil.get("userId");
        List<Category> categories = categoryService.listByUserId(userId);
        return Result.success(categories);
    }

    /**
     * POST /category (application/json)
     * 新增分类，自动绑定创建用户
     */
    @PostMapping
    public Result<Void> add(@Validated @RequestBody Category category) {
        Integer userId = ThreadLocalUtil.get("userId");
        categoryService.add(category, userId);
        return Result.success();
    }

    /**
     * PUT /category (application/json)
     * 更新分类名称与别名（校验归属权）
     */
    @PutMapping
    public Result<Void> update(@Validated @RequestBody Category category) {
        Integer userId = ThreadLocalUtil.get("userId");
        categoryService.update(category, userId);
        return Result.success();
    }

    /**
     * GET /category/detail (query string)
     * 根据ID查询分类详情
     */
    @GetMapping("/detail")
    public Result<Category> detail(@RequestParam("id") Integer id) {
        Category category = categoryService.getById(id);
        return Result.success(category);
    }

    /**
     * DELETE /category (query string)
     * 根据ID删除分类（校验归属权）
     */
    @DeleteMapping
    public Result<Void> delete(@RequestParam("id") Integer id) {
        Integer userId = ThreadLocalUtil.get("userId");
        categoryService.deleteById(id, userId);
        return Result.success();
    }
}
