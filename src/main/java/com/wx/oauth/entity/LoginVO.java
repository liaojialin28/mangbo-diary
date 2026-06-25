package com.wx.oauth.entity;

import lombok.Data;

/**
 * 登录成功后返回给前端的 VO 对象
 * 封装用户信息和登录凭证
 *
 * @author AI Generator
 */
@Data
public class LoginVO {

    /** 用户微信唯一标识 */
    private String openid;

    /** 用户昵称（静默模式为空字符串） */
    private String nickname = "";

    /** 用户头像地址（静默模式为空字符串） */
    private String headimgurl = "";

    /** 登录凭证（UUID 去除横线） */
    private String token;
}
