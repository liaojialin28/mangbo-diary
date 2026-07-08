package com.wx.oauth.service;


import com.wx.oauth.entity.LoginVO;

/**
 * 微信公众号网页OAuth授权服务接口
 */
public interface WxOauthService {

    /**
     * 微信授权登录，code换取用户信息并生成登录凭证
     * @param code 微信临时授权code
     * @param scope 授权范围 snsapi_base / snsapi_userinfo
     * @return 登录返回实体
     */
    LoginVO login(String code, String scope);
}