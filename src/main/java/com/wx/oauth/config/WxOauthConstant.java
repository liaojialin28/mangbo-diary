package com.wx.oauth.config;

public class WxOauthConstant {
    /** code换取token接口 */
    public static final String TOKEN_URL = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code";
    /** 获取用户信息接口 */
    public static final String USERINFO_URL = "https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s";
    /** 静默授权 */
    public static final String SCOPE_BASE = "snsapi_base";
    /** 弹窗授权拿用户信息 */
    public static final String SCOPE_USERINFO = "snsapi_userinfo";
}