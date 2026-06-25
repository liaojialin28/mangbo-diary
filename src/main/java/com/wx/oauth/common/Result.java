package com.wx.oauth.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应结果封装类
 *
 * @param <T> 数据泛型
 * @author AI Generator
 */
@Data
public class Result<T> implements Serializable {

    /** 响应状态码：200-成功，500-业务异常 */
    private Integer code;

    /** 响应消息 */
    private String msg;

    /** 响应数据 */
    private T data;

    private Result() {
    }

    /**
     * 构建成功响应
     *
     * @param data 响应数据
     * @param <T>  数据类型
     * @return 成功结果
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg("成功");
        result.setData(data);
        return result;
    }

    /**
     * 构建失败响应
     *
     * @param msg 错误信息
     * @param <T> 数据类型
     * @return 失败结果
     */
    public static <T> Result<T> fail(String msg) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMsg(msg);
        return result;
    }
}
