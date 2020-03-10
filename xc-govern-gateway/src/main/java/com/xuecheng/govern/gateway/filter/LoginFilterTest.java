package com.xuecheng.govern.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.logging.Filter;

@Component
public class LoginFilterTest extends ZuulFilter {
    @Override
    public String filterType() {
        /**
         pre：请求在被路由之前执行

         routing：在路由请求时调用

         post：在routing和errror过滤器之后调用

         error：处理请求时发生错误调用

         */
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
    //过虑器的内容
    //测试的需求：过虑所有请求，判断头部信息是否有Authorization，如果没有则拒绝访问，否则转发到微服务。
    @Override
    public Object run() throws ZuulException {
        RequestContext currentContext = RequestContext.getCurrentContext();
        //得到request
        HttpServletRequest request = currentContext.getRequest();
        //得到response
        HttpServletResponse response = currentContext.getResponse();
        //得到authorization的头信息
        String authorization = request.getHeader("Authorization");
        //拒绝访问
        if (StringUtils.isEmpty(authorization)){
            //拒绝访问
            currentContext.setSendZuulResponse(false);
            //设置响应码
            currentContext.setResponseStatusCode(200);
            //设置相应详细
            ResponseResult responseResult = new ResponseResult(CommonCode.UNAUTHENTICATED);
            String body = JSON.toJSONString(responseResult);
            currentContext.setResponseBody(body);
            //转成json，设置contentType
            response.setContentType("application/json;charset=utf-8");
        }


        return null;
    }
}
