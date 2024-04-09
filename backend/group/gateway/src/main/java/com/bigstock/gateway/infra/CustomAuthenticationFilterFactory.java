package com.bigstock.gateway.infra;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class CustomAuthenticationFilterFactory extends AbstractGatewayFilterFactory<CustomAuthenticationFilterFactory.Config> {

    public CustomAuthenticationFilterFactory() {
        super(Config.class);
    }

    public static class Config {
        // 可以在这里定义配置属性
    }

    @Override
    public GatewayFilter apply(Config config) {
        return new GatewayFilter() {
            @Override
            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
                // 这里实现您的身份验证逻辑
                // 例如，从请求中提取JWT令牌，并验证它
                
                // 假设验证逻辑通过，继续过滤器链
                // 如果验证失败，可以这样拒绝请求:
                // exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                // return exchange.getResponse().setComplete();
                
                return chain.filter(exchange); // 如果验证通过
            }

        };
    }
}
