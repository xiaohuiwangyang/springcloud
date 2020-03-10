package com.xuecheng.auth.control;

import com.xuecheng.api.oauth.AuthControllerApi;
import com.xuecheng.auth.service.AuthService;
import com.xuecheng.framework.domain.ucenter.ext.AuthToken;
import com.xuecheng.framework.domain.ucenter.request.LoginRequest;
import com.xuecheng.framework.domain.ucenter.response.LoginResult;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.utils.CookieUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@RestController
@RequestMapping("/")
public class AuthControl implements AuthControllerApi {

    @Value("${auth.clientId}")
    private String clientId;
    @Value("${auth.clientSecret}")
    private String clientSecret;

    @Value("${auth.cookieDomain}")
    private String cookieDomain;
    @Value("${auth.cookieMaxAge}")
    private int cookieMaxAge;
    @Autowired
    AuthService authService;

    /**
     * 用户登录
     * @param loginRequest
     * @return
     */
    @Override
    @RequestMapping(value = "/userlogin", method = RequestMethod.POST)
    public LoginResult login(LoginRequest loginRequest) {
        String password = loginRequest.getPassword();
        String username = loginRequest.getUsername();
            //校验账号是否输入
        if (loginRequest ==null || StringUtils.isEmpty(loginRequest.getUsername())) {
            System.out.println("用户名有误");
        }
            //校验密码是否输入
        if (StringUtils.isEmpty(loginRequest.getPassword())) {
            System.out.println("密码有误");
        }
        AuthToken authToken = authService.Apply(username, password, clientId, clientSecret);
        String jwt_token = authToken.getJwt_token();
        String access_token = authToken.getAccess_token();

        return new LoginResult(CommonCode.SUCCESS, access_token);
    }

    //将令牌保存到cookie
    private void saveCookie(String token) {
        HttpServletResponse response = ((ServletRequestAttributes)
                RequestContextHolder.getRequestAttributes()).getResponse();
    //添加cookie 认证令牌，最后一个参数设置为false，表示允许浏览器获取
        CookieUtil.addCookie(response, cookieDomain, "/", "uid", token, cookieMaxAge, false);
    }




    /**
     * 用户登出
     * @return
     */
    @Override
    @RequestMapping(value = "/userlogout", method = RequestMethod.POST)
    public ResponseResult logout() {

        //取出身份令牌
        String uid = getTokenFormCookie();
        //删除redis中token
        authService.deleteToken(uid);
        //清除cookie
        clearCookie(uid);
        return new ResponseResult(CommonCode.SUCCESS);

    }
    //取出cookie中的身份令牌
    private String getTokenFormCookie(){
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        Map<String, String> map = CookieUtil.readCookie(request, "uid");
        if(map!=null && map.get("uid")!=null){
            String uid = map.get("uid");
            return uid;
        }
        return null;
    }

    //从cookie中清除
    private void clearCookie(String token) {
        HttpServletResponse response = ((ServletRequestAttributes)
                RequestContextHolder.getRequestAttributes()).getResponse();
    //添加cookie 认证令牌，最后一个参数设置为false，表示允许浏览器获取
        CookieUtil.addCookie(response, cookieDomain, "/", "uid", token, 0, false);
    }
}
