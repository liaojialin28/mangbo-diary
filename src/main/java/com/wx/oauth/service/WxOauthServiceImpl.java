package com.wx.oauth.service;


import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.wx.oauth.config.WxOauthConfig;
import com.wx.oauth.config.WxOauthConstant;
import com.wx.oauth.entity.LoginVO;
import com.wx.oauth.entity.WxAccessToken;
import com.wx.oauth.entity.WxUserInfo;
import com.wx.oauth.util.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.UUID;

/**
 * 微信公众号网页OAuth授权服务
 * 功能：code换取openid、静默/显式授权获取用户信息、生成登录凭证
 */
@Slf4j
@Service
public class WxOauthServiceImpl implements WxOauthService {

    @Resource
    private WxOauthConfig wxOauthConfig;

    /**
     * 微信网页授权登录核心方法
     * @param code 微信授权临时凭证，一次性有效，5分钟过期
     * @param scope 授权范围：snsapi_base静默仅openid / snsapi_userinfo弹窗获取昵称头像
     * @return LoginVO 登录结果：openid、昵称、头像、自定义登录token
     */
    @Override
    public LoginVO login(String code, String scope) {
        // ========== 1. 入参合法性校验 ==========
        log.info("[微信OAuth登录] 开始执行，code={}，scope={}", code, scope);
        if (StrUtil.isBlank(code)) {
            log.error("[微信OAuth登录] 入参校验失败，授权code为空");
            throw new RuntimeException("微信授权code不能为空，请重新发起授权");
        }
        // scope为空默认走静默授权
        if (StrUtil.isBlank(scope)) {
            scope = WxOauthConstant.SCOPE_BASE;
            log.info("[微信OAuth登录] scope为空，自动使用静默授权snsapi_base");
        }

        // ========== 2. 调用微信接口：code换取access_token、openid ==========
        String tokenRequestUrl = String.format(WxOauthConstant.TOKEN_URL,
                wxOauthConfig.getAppId(),
                wxOauthConfig.getAppSecret(),
                code);
        log.info("[微信OAuth登录] 请求code换token接口，请求地址={}", tokenRequestUrl);

        String tokenHttpResp;
        try {
            // 发起GET请求调用微信服务
            tokenHttpResp = HttpUtil.doGet(tokenRequestUrl);
            log.info("[微信OAuth登录] code换token接口原始响应报文：{}", tokenHttpResp);
        } catch (Exception e) {
            log.error("[微信OAuth登录] code换token接口网络请求异常，code={}", code, e);
            throw new RuntimeException("调用微信授权服务网络异常：" + e.getMessage());
        }

        // 解析返回JSON为实体
        WxAccessToken accessTokenResp;
        try {
            accessTokenResp = JSON.parseObject(tokenHttpResp, WxAccessToken.class);
        } catch (JSONException e) {
            log.error("[微信OAuth登录] 解析token接口返回JSON失败，原始报文={}", tokenHttpResp, e);
            throw new RuntimeException("微信返回数据格式解析失败");
        }

        // 校验微信业务错误码
        if (accessTokenResp == null) {
            log.error("[微信OAuth登录] token接口返回空对象");
            throw new RuntimeException("微信授权服务无返回数据");
        }
        // errcode不为0代表微信侧报错（code失效、密钥错误、权限不足等）
        if (accessTokenResp.getErrcode() != null && accessTokenResp.getErrcode() != 0) {
            log.error("[微信OAuth登录] 微信返回业务错误，errcode={}, errmsg={}",
                    accessTokenResp.getErrcode(), accessTokenResp.getErrmsg());
            throw new RuntimeException("微信授权失败：" + accessTokenResp.getErrmsg());
        }
        // 校验access_token非空
        if (StrUtil.isBlank(accessTokenResp.getAccess_token())) {
            log.error("[微信OAuth登录] access_token为空，授权流程异常");
            throw new RuntimeException("获取微信访问凭证失败");
        }
        log.info("[微信OAuth登录] code换取token成功，openid={}, access_token={}, 有效期={}秒",
                accessTokenResp.getOpenid(), accessTokenResp.getAccess_token(), accessTokenResp.getExpires_in());

        // 组装返回VO基础信息
        LoginVO loginVO = new LoginVO();
        loginVO.setOpenid(accessTokenResp.getOpenid());

        // ========== 3. scope=snsapi_userinfo 显式授权，拉取用户昵称、头像 ==========
        if (WxOauthConstant.SCOPE_USERINFO.equals(scope)) {
            log.info("[微信OAuth登录] 当前为显式授权，准备拉取用户基础信息，openid={}", accessTokenResp.getOpenid());
            String userInfoUrl = String.format(WxOauthConstant.USERINFO_URL,
                    accessTokenResp.getAccess_token(), accessTokenResp.getOpenid());
            log.info("[微信OAuth登录] 用户信息接口地址={}", userInfoUrl);

            String userInfoHttpResp = new String();
            try {
                userInfoHttpResp = HttpUtil.doGet(userInfoUrl);
                log.info("[微信OAuth登录] 用户信息接口原始响应报文：{}", userInfoHttpResp);
            } catch (Exception e) {
                log.error("[微信OAuth登录] 拉取用户信息网络异常，openid={}", accessTokenResp.getOpenid(), e);
                // 用户信息拉取失败不阻断登录，仅日志告警
                log.warn("[微信OAuth登录] 用户信息获取失败，跳过昵称头像赋值，保留openid登录");
            }

            // 解析用户信息（存在响应才解析）
            if (StrUtil.isNotBlank(userInfoHttpResp)) {
                WxUserInfo wxUserInfo;
                try {
                    wxUserInfo = JSON.parseObject(userInfoHttpResp, WxUserInfo.class);
                } catch (JSONException e) {
                    log.error("[微信OAuth登录] 解析用户信息JSON失败，报文={}", userInfoHttpResp, e);
                    wxUserInfo = null;
                }

                if (wxUserInfo != null) {
                    // 无错误码才赋值昵称头像
                    if (wxUserInfo.getErrcode() == null || wxUserInfo.getErrcode() == 0) {
                        loginVO.setNickname(wxUserInfo.getNickname());
                        loginVO.setHeadimgurl(wxUserInfo.getHeadimgurl());
                        log.info("[微信OAuth登录] 用户信息拉取成功，昵称={}, 头像地址={}",
                                wxUserInfo.getNickname(), wxUserInfo.getHeadimgurl());
                    } else {
                        log.warn("[微信OAuth登录] 用户信息接口返回错误，errcode={}, errmsg={}",
                                wxUserInfo.getErrcode(), wxUserInfo.getErrmsg());
                    }
                }
            }
        } else {
            log.info("[微信OAuth登录] 当前为静默授权snsapi_base，无需调用用户信息接口");
        }

        // ========== 4. 生成全局登录凭证token ==========
        String loginToken = UUID.randomUUID().toString().replace("-", "");
        loginVO.setToken(loginToken);
        log.info("[微信OAuth登录] 生成登录token完成，token={}", loginToken);

        // ========== 5. 登录流程结束，返回登录VO ==========
        log.info("[微信OAuth登录] 整套授权流程执行完毕，openid={}, scope={}",
                loginVO.getOpenid(), scope);
        return loginVO;
    }
}