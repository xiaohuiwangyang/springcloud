package com.xuecheng.auth.service;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.client.XcServiceList;
import com.xuecheng.framework.domain.ucenter.ext.AuthToken;
import com.xuecheng.framework.domain.ucenter.response.AuthCode;
import com.xuecheng.framework.exception.ExceptionCast;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

@Service
public class AuthService {
    @Value("${auth.tokenValiditySeconds}")
    Long tokenValiditySeconds;

    @Autowired
    LoadBalancerClient loadBalancerClient;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    //认证方法
    public AuthToken login(String username, String password, String clientId, String clientSecret) {
        AuthToken authToken = Apply(username, password, clientId, clientSecret);
        if (authToken == null) {
            System.out.println("申请token");
        }
        String access_token = authToken.getAccess_token();
        String key = "access_token" + access_token;
        String content = JSON.toJSONString(authToken);
        Boolean saveResult = save(key, content, tokenValiditySeconds);
        if (!saveResult) {
            System.out.println("存储失败");

        }
        return authToken;
    }


    //申请token
    public AuthToken Apply(String username, String password, String clientId, String clientSecret) {
        //从eureka中获取认证服务的地址（因为spring security在认证服务中）
        //从eureka中获取认证服务的一个实例的地址
        ServiceInstance serviceInstance = loadBalancerClient.choose(XcServiceList.XC_SERVICE_UCENTER_AUTH);
        //此地址就是http://ip:port
        URI uri = serviceInstance.getUri();
        //令牌申请的地址 http://localhost:40400/auth/oauth/token
        String authUrl = uri + "/auth/oauth/token";
        //定义header
        LinkedMultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        String httpBasic = getHttpBasic(clientId, clientSecret);
        header.add("Authorization", httpBasic);

        //定义body
        LinkedMultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("username", username);
        body.add("password", password);

        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(body, header);
        //String url, HttpMethod method, @Nullable HttpEntity<?> requestEntity, Class<T> responseType, Object... uriVariables

        //设置restTemplate远程调用时候，对400和401不让报错，正确返回数据
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                if (response.getRawStatusCode() != 400 && response.getRawStatusCode() != 401) {
                    super.handleError(response);
                }
            }
        });

        ResponseEntity<Map> exchange = restTemplate.exchange(authUrl, HttpMethod.POST, httpEntity, Map.class);

        //申请令牌信息
        Map bodyMap = exchange.getBody();
        System.out.println(bodyMap);
        if (bodyMap == null
                || bodyMap.get("access_token") == null
                || bodyMap.get("refresh_token") == null
                || bodyMap.get("jti") == null

        ) {
            return null;
        }
        //解析spring security返回的错误信息
        String error_description = (String) bodyMap.get("error_description");
        if (!StringUtils.isEmpty(error_description)) {
            if (error_description.indexOf("UserDetailsService returned null") >= 0) {
                ExceptionCast.cast(AuthCode.AUTH_ACCOUNT_NOTEXISTS);
            } else if (error_description.indexOf("坏的凭证") >= 0) {
                ExceptionCast.cast(AuthCode.AUTH_CREDENTIAL_ERROR);
            }
        }


        AuthToken authToken = new AuthToken();
        authToken.setAccess_token((String) bodyMap.get("jti"));
        authToken.setRefresh_token((String) bodyMap.get("refresh_token"));
        authToken.setJwt_token((String) bodyMap.get("access_token"));
        return authToken;
    }

    //获取httpbasic的串
    private String getHttpBasic(String clientId, String clientSecret) {
        String string = clientId + ":" + clientSecret;
        //将串进行base64编码
        byte[] encode = Base64Utils.encode(string.getBytes());
        return "Basic " + new String(encode);
    }

    //将token到redis里面
    private Boolean save(String key, String content, Long time) {
        stringRedisTemplate.boundValueOps(key).set(content, time);

        Long expire = stringRedisTemplate.getExpire(key);
        return expire > 0;
    }

    //删除token
    public   Boolean deleteToken(String access_token){
        String str="access_token" + access_token;
        stringRedisTemplate.delete(str);
        Long expire = stringRedisTemplate.getExpire(str);
        return expire < 0;

    }
}
