package com.wx.oauth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信公众号 OAuth 配置属性类
 * 从 application.yml 读取 wx.oauth.* 配置项
 *
 * @author AI Generator
 */
@Component
@Data
@ConfigurationProperties(prefix = "wx.oauth")
public class WxOauthConfig {

    /** 公众号 AppID */
    private String appId;

    /** 公众号 AppSecret */
    private String appSecret;
}
