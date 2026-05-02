package com.bigevent.lab.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文章实体类（预留结构）
 */
@Data
@TableName("article")
public class Article {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String title;

    private String content;

    @TableField("cover_img")
    private String coverImg;

    private String state;

    @TableField("category_id")
    private Integer categoryId;

    @TableField("create_user")
    private Integer createUser;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}
