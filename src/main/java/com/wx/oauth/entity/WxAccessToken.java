package com.wx.oauth.entity;

import lombok.Data;

@Data
public class WxAccessToken {
    private String access_token;
    private Integer expires_in;
    private String refresh_token;
    private String openid;
    private String scope;
    private String unionid;
    // 错误返回字段
    private Integer errcode;
    private String errmsg;
}