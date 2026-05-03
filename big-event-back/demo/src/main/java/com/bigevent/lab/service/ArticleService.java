package com.bigevent.lab.service;

import com.bigevent.lab.common.PageResult;
import com.bigevent.lab.entity.Article;

/**
 * 文章Service接口
 */
public interface ArticleService {

    /**
     * 分页查询当前用户的文章列表
     */
    PageResult<Article> pageList(Integer pageNum, Integer pageSize, Integer categoryId, String state, Integer userId);

    /**
     * 新增文章
     */
    void add(Article article, Integer userId);

    /**
     * 更新文章（校验归属权）
     */
    void update(Article article, Integer userId);

    /**
     * 根据ID查询文章详情（校验归属权）
     */
    Article getById(Integer id, Integer userId);

    /**
     * 删除文章（校验归属权）
     */
    void deleteById(Integer id, Integer userId);
}
