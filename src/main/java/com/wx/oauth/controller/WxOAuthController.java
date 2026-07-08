package com.wx.oauth.controller;

import com.wx.oauth.config.WxOauthConstant;
import com.wx.oauth.entity.LoginVO;
import com.wx.oauth.service.WxOauthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 微信公众号OAuth授权控制器
 * 流程：页面跳转/login后端生成授权链接 → 微信回调/callback后端拿code登录 → 重定向回静态页面
 */
@Slf4j
@RestController
@RequestMapping("/wx/oauth")
public class WxOAuthController {

    @Resource
    private WxOauthService wxOauthService;

    /** 公众号配置 */
    @Value("${wx.oauth.app-id}")
    private String appId;

    /** 微信回调后端接口地址（redirect_uri） */
    @Value("${wx.oauth.redirectUrl}")
    private String redirectUrl;

    /** 登录成功跳转的静态页面地址 */
    @Value("${wx.oauth.successPage}")
    private String successPage;

    /**
     * 授权入口：接收scope，后端拼接微信授权链接302跳转微信
     * @param scope 授权范围 snsapi_base / snsapi_userinfo
     * @param state 自定义透传参数，可选
     * @param response 响应跳转
     * @throws IOException 重定向异常
     */
    @GetMapping("/login")
    public void wxLogin(@RequestParam String scope,
                        @RequestParam(required = false) String state,
                        HttpServletResponse response) throws IOException {
        log.info("[微信授权入口] 进入登录跳转，scope={}, state={}", scope, state);

        // 默认state
        String realState = (state == null || state.trim().length() == 0) ? "wxLogin" : state;
        // 对回调地址整体URL编码
        String encodeRedirect = URLEncoder.encode(redirectUrl, StandardCharsets.UTF_8.name());

        // 拼接微信标准OAuth授权链接
        String authUrl = "https://open.weixin.qq.com/connect/oauth2/authorize"
                + "?appid=" + appId
                + "&redirect_uri=" + encodeRedirect
                + "&response_type=code"
                + "&scope=" + scope
                + "&state=" + realState
                + "#wechat_redirect";

        log.info("[微信授权入口] 跳转微信授权页，authUrl={}", authUrl);
        response.sendRedirect(authUrl);
    }

    /**
     * 微信回调地址 redirect_uri，微信自动携带code访问此接口
     * 后端拿code换取用户信息、生成登录token，完成后重定向回前端静态页面
     * @param code 微信临时授权凭证
     * @param state 透传参数
     * @param response 页面重定向
     * @throws IOException 跳转异常
     */
    @GetMapping("/callback")
    public void callback(@RequestParam String code,
                         @RequestParam(required = false) String state,
                         HttpServletResponse response) throws IOException {
        log.info("[微信回调接口] 接收微信回调，code={}, state={}", code, state);
        String targetUrl;

        try {
            // 静默授权，如需头像昵称改为 WxOauthConstant.SCOPE_USERINFO
            LoginVO loginVO = wxOauthService.login(code, WxOauthConstant.SCOPE_BASE);
            log.info("[微信回调接口] code登录成功 openid={}, token={}", loginVO.getOpenid(), loginVO.getToken());

            // 拼接参数带回前端页面
            StringBuilder sb = new StringBuilder(successPage);
            sb.append("?token=").append(URLEncoder.encode(loginVO.getToken(), StandardCharsets.UTF_8.name()));
            sb.append("&openid=").append(URLEncoder.encode(loginVO.getOpenid(), StandardCharsets.UTF_8.name()));

            // 昵称
            String nick = loginVO.getNickname();
            if (nick != null && nick.trim().length() > 0) {
                sb.append("&nickname=").append(URLEncoder.encode(nick, StandardCharsets.UTF_8.name()));
            }
            // 头像
            String headImg = loginVO.getHeadimgurl();
            if (headImg != null && headImg.trim().length() > 0) {
                sb.append("&headimgurl=").append(URLEncoder.encode(headImg, StandardCharsets.UTF_8.name()));
            }
            // state透传
            if (state != null && state.trim().length() > 0) {
                sb.append("&state=").append(URLEncoder.encode(state, StandardCharsets.UTF_8.name()));
            }

            targetUrl = sb.toString();
            log.info("[微信回调接口] 登录成功，重定向至页面：{}", targetUrl);
        } catch (RuntimeException e) {
            log.error("[微信回调接口] 授权登录异常", e);
            // 异常携带错误信息跳转页面
            String errMsg = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8.name());
            targetUrl = successPage + "?err=" + errMsg;
            log.info("[微信回调接口] 授权失败，跳转错误页面：{}", targetUrl);
        }

        response.sendRedirect(targetUrl);
    }
}