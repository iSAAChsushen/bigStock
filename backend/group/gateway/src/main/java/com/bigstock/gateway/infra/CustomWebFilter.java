//package com.bigstock.gateway.infra;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.web.server.ServerWebExchange;
//import org.springframework.web.server.WebFilter;
//import org.springframework.web.server.WebFilterChain;
//
//import reactor.core.publisher.Mono;
//
//public class CustomWebFilter implements WebFilter {
//
//    private static final Logger log = LoggerFactory.getLogger(CustomWebFilter.class);
//
//    @Override
//    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
//        // 在这里实现您的过滤逻辑
//        // 例如，记录每个请求的信息
//        log.info("Request URL: {}", exchange.getRequest().getURI().toString());
//
//        // 您可以在这里添加更多的逻辑，例如验证JWT令牌
//        // 如果验证失败，您可以直接返回Mono.error()或设置响应状态码
//        // 例如：return exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED).setComplete();
//
//        // 如果一切正常，继续过滤器链
//        return chain.filter(exchange);
//    }
//}
