package com.bigevent.lab.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bigevent.lab.common.PageResult;
import com.bigevent.lab.entity.Article;
import com.bigevent.lab.mapper.ArticleMapper;
import com.bigevent.lab.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 文章Service实现类
 */
@Service
public class ArticleServiceImpl implements ArticleService {

    @Autowired
    private ArticleMapper articleMapper;

    @Override
    public PageResult<Article> pageList(Integer pageNum, Integer pageSize, Integer categoryId, String state, Integer userId) {
        Page<Article> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getCreateUser, userId);
        if (categoryId != null) {
            wrapper.eq(Article::getCategoryId, categoryId);
        }
        if (state != null && !state.isEmpty()) {
            wrapper.eq(Article::getState, state);
        }
        wrapper.orderByDesc(Article::getCreateTime);
        Page<Article> result = articleMapper.selectPage(page, wrapper);
        return new PageResult<>(result.getTotal(), result.getRecords());
    }

    @Override
    public void add(Article article, Integer userId) {
        article.setCreateUser(userId);
        article.setCreateTime(LocalDateTime.now());
        article.setUpdateTime(LocalDateTime.now());
        articleMapper.insert(article);
    }

    @Override
    public void update(Article article, Integer userId) {
        Article existing = articleMapper.selectById(article.getId());
        if (existing == null) {
            throw new RuntimeException("文章不存在");
        }
        if (!existing.getCreateUser().equals(userId)) {
            throw new RuntimeException("无权操作该文章");
        }

        Article updateArticle = new Article();
        updateArticle.setId(article.getId());
        updateArticle.setTitle(article.getTitle());
        updateArticle.setContent(article.getContent());
        updateArticle.setCoverImg(article.getCoverImg());
        updateArticle.setState(article.getState());
        updateArticle.setCategoryId(article.getCategoryId());
        updateArticle.setUpdateTime(LocalDateTime.now());
        articleMapper.updateById(updateArticle);
    }

    @Override
    public Article getById(Integer id, Integer userId) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new RuntimeException("文章不存在");
        }
        if (!article.getCreateUser().equals(userId)) {
            throw new RuntimeException("无权查看该文章");
        }
        return article;
    }

    @Override
    public void deleteById(Integer id, Integer userId) {
        Article existing = articleMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("文章不存在");
        }
        if (!existing.getCreateUser().equals(userId)) {
            throw new RuntimeException("无权操作该文章");
        }
        articleMapper.deleteById(id);
    }
}
