package com.wx.oauth.entity;

import lombok.Data;

@Data
public class LoginVO {
    private String openid;
    private String nickname;
    private String headimgurl;
    private String token;
}