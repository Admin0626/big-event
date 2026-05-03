package com.bigevent.lab.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文章实体类
 */
@Data
@TableName("article")
public class Article {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @NotBlank(message = "标题不能为空")
    @Size(max = 100, message = "标题长度不能超过100个字符")
    private String title;

    @NotBlank(message = "内容不能为空")
    @Size(max = 50000, message = "内容长度不能超过50000个字符")
    private String content;

    @TableField("cover_img")
    private String coverImg;

    @NotBlank(message = "状态不能为空")
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
