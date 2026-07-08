package com.wx.oauth.entity;

import lombok.Data;

@Data
public class WxUserInfo {
    private String openid;
    private String nickname;
    private String headimgurl;
    private Integer sex;
    private String province;
    private String city;
    private String country;
    private String unionid;
    private Integer errcode;
    private String errmsg;
}