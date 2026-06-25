package com.wx.oauth.util;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 * HTTP 请求工具类
 * 提供安全的 GET 请求方法，自动关闭资源避免内存泄漏
 *
 * @author AI Generator
 */
public class HttpUtil {

    private HttpUtil() {
        // 工具类禁止实例化
    }

    /**
     * 执行 HTTP GET 请求并返回响应内容
     * 使用 CloseableHttpClient 实现，自动关闭所有资源
     *
     * @param url 请求地址
     * @return 响应体字符串
     * @throws Exception 请求异常
     */
    public static String doGet(String url) throws Exception {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(url);
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                return EntityUtils.toString(response.getEntity(), "UTF-8");
            }
        }
    }
}
