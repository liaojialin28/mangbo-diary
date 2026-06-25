package com.wx.oauth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * 微信公众号 OAuth2 双模式登录应用启动类
 * 支持静默登录(snsapi_base)和显式授权登录(snsapi_userinfo)
 *
 * @author AI Generator
 */
@SpringBootApplication
@EnableConfigurationProperties
public class WxOauthApplication {

    public static void main(String[] args) {
        SpringApplication.run(WxOauthApplication.class, args);
    }
}
