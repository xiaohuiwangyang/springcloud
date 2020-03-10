package com.xuecheng.govern.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.govern.gateway.service.AuthService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LoginFilter extends ZuulFilter {
    @Autowired
    AuthService authService;
    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() throws ZuulException {
        RequestContext currentContext = RequestContext.getCurrentContext();
        HttpServletRequest request = currentContext.getRequest();
        //获取cookie里面的token
        String accessToken = authService.getTokenFormCookie(request);
        //判断redis里面的token
        long expire = authService.getExpire(accessToken);

        //从header里面取出token
        String jwttoken = authService.getJwtTokenFromHeader(request);
        if(StringUtils.isEmpty(accessToken)){
            accessDeny();
            return null;
        }

        if(StringUtils.isEmpty(jwttoken)){
            accessDeny();
            return null;
        }
        if (expire<0){
            accessDeny();
            return null;
        }

        return null;
    }



    //拒绝访问
    private void accessDeny(){
        RequestContext currentContext = RequestContext.getCurrentContext();
        //设置拒绝访问
        currentContext.setSendZuulResponse(false);
        //设置响应码
        currentContext.setResponseStatusCode(200);

        //设置相应内容
        ResponseResult responseResult = new ResponseResult(CommonCode.UNAUTHENTICATED);
        String body = JSON.toJSONString(responseResult);
        currentContext.setResponseBody(body);
        HttpServletResponse response = currentContext.getResponse();
        //转成json，设置contentType
        response.setContentType("application/json;charset=utf-8");
    }


}
