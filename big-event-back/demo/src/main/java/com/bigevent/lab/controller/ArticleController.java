package com.bigevent.lab.controller;

import com.bigevent.lab.common.PageResult;
import com.bigevent.lab.common.Result;
import com.bigevent.lab.entity.Article;
import com.bigevent.lab.service.ArticleService;
import com.bigevent.lab.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 文章控制器
 */
@RestController
@RequestMapping("/article")
@Validated
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    /**
     * GET /article
     * 分页查询当前用户的文章列表
     */
    @GetMapping
    public Result<PageResult<Article>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "5") Integer pageSize,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String state) {
        Integer userId = ThreadLocalUtil.get("userId");
        PageResult<Article> result = articleService.pageList(pageNum, pageSize, categoryId, state, userId);
        return Result.success(result);
    }

    /**
     * POST /article
     * 新增文章
     */
    @PostMapping
    public Result<Void> add(@Validated @RequestBody Article article) {
        Integer userId = ThreadLocalUtil.get("userId");
        articleService.add(article, userId);
        return Result.success();
    }

    /**
     * PUT /article
     * 更新文章
     */
    @PutMapping
    public Result<Void> update(@Validated @RequestBody Article article) {
        Integer userId = ThreadLocalUtil.get("userId");
        articleService.update(article, userId);
        return Result.success();
    }

    /**
     * GET /article/detail
     * 查看文章详情
     */
    @GetMapping("/detail")
    public Result<Article> detail(@RequestParam("id") Integer id) {
        Integer userId = ThreadLocalUtil.get("userId");
        Article article = articleService.getById(id, userId);
        return Result.success(article);
    }

    /**
     * DELETE /article
     * 删除文章
     */
    @DeleteMapping
    public Result<Void> delete(@RequestParam("id") Integer id) {
        Integer userId = ThreadLocalUtil.get("userId");
        articleService.deleteById(id, userId);
        return Result.success();
    }
}
