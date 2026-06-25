package com.wx.oauth.entity;

import lombok.Data;

/**
 * 微信用户信息接口返回实体
 * 仅在 snsapi_userinfo 显式授权模式下获取
 *
 * @author AI Generator
 */
@Data
public class WxUserInfo {

    /** 用户唯一标识 */
    private String openid;

    /** 用户昵称 */
    private String nickname;

    /** 用户头像 URL */
    private String headimgurl;

    /** 性别：1-男，2-女，0-未知 */
    private Integer sex;

    /** 城市 */
    private String city;

    /** 省份 */
    private String province;

    /** 国家 */
    private String country;
}
