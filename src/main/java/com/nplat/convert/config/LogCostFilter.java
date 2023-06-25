package com.nplat.convert.config;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @Author zhangchenghui
 * @create 2020/9/1 14:33
 */
@Slf4j
public class LogCostFilter implements Filter {

    private static List<String> whiteUrls = new ArrayList<>();

    @Override
    public void init(FilterConfig filterConfig) {
        whiteUrls.add("/common/send");
        whiteUrls.add("/common/register");
        whiteUrls.add("/common/login");
        whiteUrls.add("/common/download");
        whiteUrls.add("/upload");
        whiteUrls.add("/common/file/delete");
        whiteUrls.add("/common/file/up");
        whiteUrls.add("/common/file/json");
        whiteUrls.add("/common/file/one");
        whiteUrls.add("/common/files");
        whiteUrls.add("/common/analyze/files");
        whiteUrls.add("/thirdpart/data/json/v1");
        whiteUrls.add("/common/file/type");
        whiteUrls.add("/error");
        whiteUrls.add("/favicon.ico");


    }


    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        JSONObject json = new JSONObject();
        String url = ((HttpServletRequest) servletRequest).getRequestURI();
        log.info("url={}",url);


        if (whiteUrls.contains(url)) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        String token = ((HttpServletRequest) servletRequest).getHeader("token");
        if (Objects.isNull(token)) {

            log.error("token non");
            json.put("resCode", 5001);
            json.put("resDes", "token过期，请重新登录");
            render((HttpServletResponse) servletResponse, json.toJSONString());
        } else {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }


    @Override
    public void destroy() {

    }


    public static void render(HttpServletResponse res, String text) throws IOException {
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        res.setStatus(200);
        res.getWriter().append(text);
        res.getWriter().flush();
        res.getWriter().close();
    }

}
