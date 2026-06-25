package com.wx.oauth.controller;

import com.wx.oauth.common.Result;
import com.wx.oauth.entity.LoginVO;
import com.wx.oauth.service.WxOauthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 微信 OAuth 登录控制器
 * 提供统一的微信授权登录接口
 *
 * @author AI Generator
 */
@Slf4j
@RestController
@RequestMapping("/wx/oauth")
@RequiredArgsConstructor
public class WxOauthController {

    private final WxOauthService wxOauthService;

    /**
     * 微信 OAuth 登录接口
     * 接收前端传来的微信回调 code 和授权 scope，完成登录并返回用户信息
     *
     * @param code  微信回调授权码（一次性有效）
     * @param scope 授权类型：snsapi_base / snsapi_userinfo
     * @return 统一结果封装，包含用户信息和登录凭证
     */
    @GetMapping("/login")
    public Result<LoginVO> login(
            @RequestParam("code") String code,
            @RequestParam("scope") String scope) {

        try {
            LoginVO vo = wxOauthService.login(code, scope);
            return Result.success(vo);
        } catch (Exception e) {
            log.error("微信OAuth登录失败", e);
            return Result.fail(e.getMessage());
        }
    }
}
