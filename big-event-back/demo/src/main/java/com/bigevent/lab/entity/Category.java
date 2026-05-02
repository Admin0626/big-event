package com.bigevent.lab.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文章分类实体类
 */
@Data
@TableName("category")
public class Category {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String name;

    private String alias;

    @TableField("create_user")
    private Integer createUser;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}
