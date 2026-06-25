package com.wx.oauth.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.wx.oauth.config.WxOauthConfig;
import com.wx.oauth.entity.LoginVO;
import com.wx.oauth.entity.WxAccessToken;
import com.wx.oauth.entity.WxUserInfo;
import com.wx.oauth.util.HttpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 微信 OAuth 授权核心业务服务
 * 处理静默登录和显式授权登录两种模式
 *
 * @author AI Generator
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WxOauthService {

    /** 微信获取 access_token 接口地址 */
    private static final String TOKEN_URL = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code";

    /** 微信获取用户信息接口地址（仅 snsapi_userinfo 模式使用） */
    private static final String USERINFO_URL = "https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s&lang=zh_CN";

    private final WxOauthConfig wxOauthConfig;

    /**
     * 执行微信 OAuth 登录流程
     *
     * @param code  微信回调授权码（一次性有效）
     * @param scope 授权类型：snsapi_base（静默）或 snsapi_userinfo（显式）
     * @return 登录结果 VO，包含用户信息和 token
     * @throws Exception 授权或接口调用异常
     */
    public LoginVO login(String code, String scope) throws Exception {
        // 第一步：通过 code 获取 access_token 和 openid
        String tokenUrl = String.format(TOKEN_URL, wxOauthConfig.getAppId(), wxOauthConfig.getAppSecret(), code);
        String tokenResponse = HttpUtil.doGet(tokenUrl);

        WxAccessToken accessToken;
        try {
            accessToken = JSON.parseObject(tokenResponse, WxAccessToken.class);
        } catch (JSONException e) {
            throw new RuntimeException("解析微信 access_token 响应失败：" + tokenResponse, e);
        }

        // 校验 access_token 是否获取成功
        if (accessToken == null || accessToken.getAccess_token() == null || accessToken.getAccess_token().isEmpty()) {
            throw new RuntimeException("获取微信 access_token 失败：" + tokenResponse);
        }

        // 构建返回对象
        LoginVO vo = new LoginVO();
        vo.setOpenid(accessToken.getOpenid());

        // 第二步：根据 scope 类型决定是否获取用户详细信息
        if ("snsapi_userinfo".equals(scope)) {
            // 显式授权模式：调用用户信息接口获取昵称、头像等
            String userInfoUrl = String.format(USERINFO_URL, accessToken.getAccess_token(), accessToken.getOpenid());
            String userInfoResponse = HttpUtil.doGet(userInfoUrl);

            WxUserInfo userInfo;
            try {
                userInfo = JSON.parseObject(userInfoResponse, WxUserInfo.class);
            } catch (JSONException e) {
                throw new RuntimeException("解析微信用户信息响应失败：" + userInfoResponse, e);
            }

            if (userInfo != null) {
                vo.setNickname(userInfo.getNickname());
                vo.setHeadimgurl(userInfo.getHeadimgurl());
            }
        }
        // 静默模式（snsapi_base）：不调用用户信息接口，昵称和头像保持默认空字符串

        // 第三步：生成 UUID 作为登录 token（去除横线）
        String token = java.util.UUID.randomUUID().toString().replace("-", "");
        vo.setToken(token);

        log.info("微信OAuth登录成功 openid={} scope={}", vo.getOpenid(), scope);
        return vo;
    }
}
