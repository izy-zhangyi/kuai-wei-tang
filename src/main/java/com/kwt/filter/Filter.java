package com.kwt.filter;

import com.kwt.common.R;
import com.kwt.common.ReggieConstants;
import com.kwt.common.ReggieContext;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter(filterName = "filter", urlPatterns = "/*")
@Slf4j
public class Filter implements javax.servlet.Filter {
    /**
     * 路径匹配，匹配所有要过滤放行的路径
     */
    private static final AntPathMatcher ANT_PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        /**
         * 将servlet的请求转成HttpServlet的请求
         */
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        /**
         * 获取本次请求的全部路径
         */
        String requestURI = request.getRequestURI();

        /**
         * 日志打印，控制台打印此次请求路径
         */
        log.info("拦截到请求：{}", requestURI);

        /**
         * 判断本次请求是否要处理
         * 如果不需要，直接放行
         */

        boolean check = check(ReggieConstants.URIS, requestURI);
        if (check) {
            log.info("本次请求{}不需要处理", requestURI);
            filterChain.doFilter(request, response);
            return; //请求到此结束，只会执行一次
        }

        /**
         * 管理端
         * 判断用户是否已经登录
         * 已登录，放行
         * 如果用户没有登录，则返回未登录结果，之后再通过输出流将数据序列化响应到前台页面
         */
        Long id = (Long) request.getSession().getAttribute(ReggieConstants.EMPLOYEE_SESSION_KEY);

        if ( id!= null) {
            log.info("用户已登录，id为：{}", request.getSession().getAttribute(ReggieConstants.EMPLOYEE_SESSION_KEY));
            //已登录
            //将已登录的用户id放入threadLocal中
            ReggieContext.set(id);
            filterChain.doFilter(request, response);
            //controller结束之后，移除
            ReggieContext.remove();
            return;
        }

        /**
         * 移动端，
         *  判断移动端登录状态
         *  如果已经登录，放行
         */
        Long userId = (Long) request.getSession().getAttribute(ReggieConstants.USER_SESSION_KEY);
        if (userId != null) {
            //移动端已经登录，
            //将已经登录的id放入TreadLoacl中
            ReggieContext.set(userId);
            filterChain.doFilter(request,response);
            //controller结束之后，移除id
            ReggieContext.remove();
            return;
        }

        log.info("用户未登录");
        response.getWriter().write(JSONObject.toJSONString(R.error(ReggieConstants.NOT_LOGIN_KEY)));

    }


    /**
     * 将本次请求路径与要放行的路径匹配，
     * 匹配得到 ===》放行，否则===》拦截
     *
     * @param urls
     * @param requestURI
     * @return
     */
    public boolean check(String[] urls, String requestURI) {
        for (String url : urls) {
            /**
             * 请求路径与放行路径匹配，结果为TRUE，放行
             */
            boolean match = ANT_PATH_MATCHER.match(url,requestURI);
            if (match) {
                return true;
            }
        }
        return false;
    }
}
