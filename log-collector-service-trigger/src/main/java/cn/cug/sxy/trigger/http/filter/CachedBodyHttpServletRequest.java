package cn.cug.sxy.trigger.http.filter;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * @version 1.0
 * @Date 2025/7/16 14:18
 * @Description 可重复读取请求体的HttpServletRequest包装器
 * @Author jerryhotton
 */

@Slf4j
public class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {

    private byte[] cachedBody;

    public CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
        super(request);
        // 读取并缓存请求体
        cachedBody = getRequestBodyBytes(request);
    }

    /**
     * 从原始请求中读取请求体并缓存
     */
    private byte[] getRequestBodyBytes(HttpServletRequest request) throws IOException {
        int contentLength = request.getContentLength();
        if (contentLength <= 0) {
            return new byte[0];
        }
        // 创建足够大的缓冲区
        byte[] buffer = new byte[contentLength];
        int totalBytesRead = 0;
        int bytesRead;
        try (ServletInputStream inputStream = request.getInputStream()) {
            // 读取所有数据到缓冲区
            while ((bytesRead = inputStream.read(buffer, totalBytesRead, contentLength - totalBytesRead)) != -1) {
                totalBytesRead += bytesRead;
                if (totalBytesRead >= contentLength) {
                    break;
                }
            }
        }
        if (totalBytesRead < contentLength) {
            log.warn("读取请求体不完整: 读取了 {} bytes, 预期 {} bytes", totalBytesRead, contentLength);
        } else {
            log.debug("成功缓存请求体: {} bytes", totalBytesRead);
        }

        // 如果内容长度小于1000字节，记录请求体内容用于调试
        if (contentLength > 0 && contentLength < 1000) {
            String bodyContent = new String(buffer, 0, totalBytesRead, StandardCharsets.UTF_8);
            log.debug("请求体内容: {}", bodyContent);
        }

        return buffer;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(cachedBody);

        return new ServletInputStream() {
            @Override
            public int read() throws IOException {
                return byteArrayInputStream.read();
            }

            @Override
            public boolean isFinished() {
                return byteArrayInputStream.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener listener) {
                throw new UnsupportedOperationException("不支持ReadListener");
            }
        };
    }

}
