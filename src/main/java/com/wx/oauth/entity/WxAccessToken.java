package com.wx.oauth.entity;

import lombok.Data;

/**
 * 微信 OAuth access_token 接口返回实体
 * 接收微信授权后返回的令牌信息
 *
 * @author AI Generator
 */
@Data
public class WxAccessToken {

    /** 访问令牌 */
    private String access_token;

    /** 过期时间（秒） */
    private Long expires_in;

    /** 刷新令牌 */
    private String refresh_token;

    /** 用户唯一标识 openid */
    private String openid;

    /** 用户授权的作用域 */
    private String scope;
}
