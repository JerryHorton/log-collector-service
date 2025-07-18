package cn.cug.sxy.trigger.http.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.*;

/**
 * @version 1.0
 * @Date 2025/7/16 11:57
 * @Description 请求体缓存过滤器（将原始请求包装为ContentCachingRequestWrapper，以便拦截器和控制器可以多次读取请求体）
 * @Author jerryhotton
 */

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestBodyCachingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // 只对POST, PUT等可能包含请求体的请求进行特殊处理
        if (HttpMethod.POST.matches(request.getMethod()) ||
                HttpMethod.PUT.matches(request.getMethod()) ||
                HttpMethod.PATCH.matches(request.getMethod())) {

            // 使用自定义的可重复读取请求包装器
            CachedBodyHttpServletRequest cachedBodyRequest = new CachedBodyHttpServletRequest(request);
            filterChain.doFilter(cachedBodyRequest, response);
        } else {
            // 对于GET等没有请求体的请求，直接使用原始请求
            filterChain.doFilter(request, response);
        }
    }

}
