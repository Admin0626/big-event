-- ============================================================
-- Big Event Lab 数据库建表 SQL
-- 数据库: big_event_lab
-- MySQL 8.0
-- ============================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS experiment_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE experiment_db;

-- 迁移: 如果数据库已存在，执行以下 SQL 更新字段类型
-- ALTER TABLE article MODIFY COLUMN cover_img LONGTEXT COMMENT '封面图Base64数据';

-- ============================================================
-- 1. 用户表 (user)
-- ============================================================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id`          INT UNSIGNED    NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    `username`    VARCHAR(32)     NOT NULL                 COMMENT '用户名',
    `password`    VARCHAR(128)    NOT NULL                 COMMENT '密码(MD5加密)',
    `nickname`    VARCHAR(32)     DEFAULT ''               COMMENT '昵称',
    `email`       VARCHAR(64)     DEFAULT ''               COMMENT '邮箱',
    `avatar_url`  VARCHAR(255)    DEFAULT ''               COMMENT '头像URL',
    `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ============================================================
-- 2. 文章分类表 (category)
-- ============================================================
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
    `id`          INT UNSIGNED    NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    `name`        VARCHAR(32)     NOT NULL                 COMMENT '分类名称',
    `alias`       VARCHAR(32)     NOT NULL                 COMMENT '分类别名',
    `create_user` INT UNSIGNED    NOT NULL                 COMMENT '创建人ID(关联user.id)',
    `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_create_user` (`create_user`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章分类表';

-- ============================================================
-- 3. 文章表 (article) - 预留结构
-- ============================================================
DROP TABLE IF EXISTS `article`;
CREATE TABLE `article` (
    `id`          INT UNSIGNED    NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    `title`       VARCHAR(128)    NOT NULL                 COMMENT '文章标题',
    `content`     TEXT                                     COMMENT '文章内容',
    `cover_img`   LONGTEXT                                 COMMENT '封面图Base64数据',
    `state`       VARCHAR(8)      DEFAULT '草稿'           COMMENT '状态: 草稿/已发布',
    `category_id` INT UNSIGNED    NOT NULL                 COMMENT '分类ID(关联category.id)',
    `create_user` INT UNSIGNED    NOT NULL                 COMMENT '创建人ID(关联user.id)',
    `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_create_user` (`create_user`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章表';
