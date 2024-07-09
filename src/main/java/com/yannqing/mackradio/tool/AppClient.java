package com.yannqing.mackradio.tool;

import com.yannqing.mackradio.common.sign.Hmac256Signature;
import com.yannqing.mackradio.util.AuthUtil;
import com.yannqing.mackradio.util.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.security.SignatureException;

/**
 * 调用请求的客户端
 */
@Slf4j
public class AppClient {

    private final String apiKey;
    private final String apiSecret;

    public AppClient(String apiKey, String apiSecret) {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
    }

    public String doRequest(String requestData, String requestUrl) {
        String authRequestUrl = buildAuthRequestUrl(requestUrl);
        log.info("request url = {}, data = {}", requestUrl, requestData);
        byte[] bytes = HttpUtil.postBytes(authRequestUrl, requestData);
        String respData = new String(bytes);
        log.info("response data = {}", respData);
        return respData;
    }

    /**
     * 生成鉴权接口url
     */
    private String buildAuthRequestUrl(String requestUrl){
        Hmac256Signature signature = new Hmac256Signature(this.apiKey, this.apiSecret, requestUrl, "POST");
        try {
            return AuthUtil.generateRequestUrl(signature);
        } catch (MalformedURLException | SignatureException e) {
            throw new RuntimeException("buildAuthRequestUrl failed, message : " + e.getMessage());
        }
    }

}
