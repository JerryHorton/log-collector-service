package cn.cug.sxy.domain.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * @version 1.0
 * @Date 2025/7/10 18:59
 * @Description AOP配置类（启用AOP代理暴露，使得可以通过AopContext获取当前代理对象）
 * @Author jerryhotton
 */

@Configuration
@EnableAspectJAutoProxy(exposeProxy = true)
public class AopConfig {
    // 无需额外配置，仅启用AOP代理暴露
}
