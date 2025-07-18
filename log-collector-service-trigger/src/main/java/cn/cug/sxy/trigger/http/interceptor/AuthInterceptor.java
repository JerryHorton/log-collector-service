package cn.cug.sxy.trigger.http.interceptor;

import cn.cug.sxy.api.common.ApiConstants;
import cn.cug.sxy.api.response.Response;
import cn.cug.sxy.domain.reception.alc.IAuthGateway;
import cn.cug.sxy.trigger.http.filter.CachedBodyHttpServletRequest;
import cn.cug.sxy.types.dto.AuthRequestDTO;
import cn.cug.sxy.types.dto.AuthResultDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @version 1.0
 * @Date 2025/7/10 16:42
 * @Description 认证拦截器（负责在API请求处理前进行认证检查）
 * @Author jerryhotton
 */

@Slf4j
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final IAuthGateway authGateway;
    private final ObjectMapper objectMapper;

    public AuthInterceptor(IAuthGateway authGateway, ObjectMapper objectMapper) {
        this.authGateway = authGateway;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取请求路径
        String requestPath = request.getRequestURI();
        // 排除不需要认证的路径
        if (isExcludedPath(requestPath)) {
            return true;
        }
        // 提取认证信息
        String accessKey = request.getHeader(ApiConstants.HEADER_ACCESS_KEY);
        String signature = request.getHeader(ApiConstants.HEADER_SIGNATURE);
        String timestamp = request.getHeader(ApiConstants.HEADER_TIMESTAMP);
        // 验证必要的认证头信息
        if (StringUtils.isEmpty(accessKey) || StringUtils.isEmpty(signature) || StringUtils.isEmpty(timestamp)) {
            handleAuthFailure(response, "缺少必要的认证信息", HttpStatus.UNAUTHORIZED);
            return false;
        }
        // 获取客户端IP
        String clientIp = getClientIp(request);
        // 获取请求头信息
        Map<String, String> headers = extractHeaders(request);
        // 获取请求体
        String requestBody = "";
        if (request instanceof CachedBodyHttpServletRequest) {
            try {
                // 读取完整请求体
                BufferedReader reader = request.getReader();
                requestBody = reader.lines().collect(Collectors.joining());
                log.debug("获取完整请求体用于认证，长度: {} 字符", requestBody.length());
            } catch (Exception e) {
                log.error("读取请求体时发生异常", e);
            }
        } else {
            log.warn("请求未被包装为CachedBodyHttpServletRequest，无法安全读取请求体");
        }
        // 创建认证请求
        AuthRequestDTO authRequest = new AuthRequestDTO(
                accessKey,
                signature,
                timestamp,
                clientIp,
                AuthRequestDTO.AuthMethod.HMAC_SIGNATURE,
                headers,
                requestBody
        );
        try {
            // 调用认证网关进行认证
            AuthResultDTO authResult = authGateway.authenticate(authRequest);
            if (authResult.isSuccess()) {
                // 将clientIp存入请求属性中供后续使用
                request.setAttribute(ApiConstants.HEADER_CLIENT_IP, clientIp);
                // 认证成功
                return true;
            } else {
                // 认证失败
                handleAuthFailure(response, authResult.getErrorMessage(), HttpStatus.UNAUTHORIZED);
                return false;
            }
        } catch (Exception e) {
            log.error("认证过程发生异常", e);
            handleAuthFailure(response, "认证处理异常: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            return false;
        }
    }

    /**
     * 获取请求体内容
     */
    private String getRequestBody(ContentCachingRequestWrapper request) {
        byte[] buf = request.getContentAsByteArray();
        if (buf.length > 0) {
            return new String(buf, StandardCharsets.UTF_8);
        }
        return "";
    }

    /**
     * 判断是否为不需要认证的路径
     */
    private boolean isExcludedPath(String path) {
        // 不需要认证的路径列表
        String[] excludedPaths = {

        };
        for (String excludedPath : excludedPaths) {
            if (path.startsWith(excludedPath)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 处理认证失败的情况
     */
    private void handleAuthFailure(HttpServletResponse response, String message, HttpStatus status) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json;charset=UTF-8");
        Response<Void> apiResponse = Response.<Void>builder()
                .code(Integer.toString(status.value()))
                .info(message)
                .build();

        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }

    /**
     * 获取客户端真实IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (StringUtils.isNotEmpty(ip) && !"unknown".equalsIgnoreCase(ip)) {
            // 多次反向代理后会有多个IP值，第一个为真实IP
            int index = ip.indexOf(",");
            if (index != -1) {
                return ip.substring(0, index);
            } else {
                return ip;
            }
        }
        ip = request.getHeader("X-Real-IP");
        if (StringUtils.isNotEmpty(ip) && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }

        return request.getRemoteAddr();
    }

    /**
     * 提取所有请求头信息
     */
    private Map<String, String> extractHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.put(headerName, request.getHeader(headerName));
        }

        return headers;
    }

}
