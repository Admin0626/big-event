package com.bigevent.lab.common;

import lombok.Data;

/**
 * 统一响应格式
 * code: 0-成功, 1-失败
 * msg: 提示信息
 * data: 返回数据
 */
@Data
public class Result<T> {

    private Integer code;
    private String msg;
    private T data;

    public Result() {
    }

    public Result(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> Result<T> success(T data) {
        return new Result<T>(0, "操作成功", data);
    }

    public static <T> Result<T> success() {
        return new Result<T>(0, "操作成功", null);
    }

    public static <T> Result<T> error(String msg) {
        return new Result<T>(1, msg, null);
    }

    public static <T> Result<T> error(String msg, T data) {
        return new Result<T>(1, msg, data);
    }
}
