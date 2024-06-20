package com.bigstock.auth.infra;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.CorsUtils;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;

@Configuration
public class WebConfig {

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.addAllowedOrigin("*"); // 允许所有来源，生产环境中应该限制为特定的域名
		configuration.applyPermitDefaultValues();
		configuration.addAllowedMethod(HttpMethod.GET);
		configuration.addAllowedMethod(HttpMethod.POST);
		configuration.addAllowedMethod(HttpMethod.PATCH);
		configuration.addAllowedMethod(HttpMethod.PUT);
		configuration.addAllowedMethod(HttpMethod.DELETE);
		configuration.addAllowedMethod(HttpMethod.OPTIONS);
		configuration.addAllowedHeader("*"); // 允许所有Header
		configuration.setAllowCredentials(false); // 允许携带Cookie

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	@Bean
	public WebFilter corsFilter() {
		return new CorsWebFilter(corsConfigurationSource());
	}

	class CorsWebFilter implements WebFilter {

		private final CorsConfigurationSource configSource;

		public CorsWebFilter(CorsConfigurationSource configSource) {
			this.configSource = configSource;
		}

		@Override
		public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
			if (CorsUtils.isCorsRequest(exchange.getRequest())) {
				if (CorsUtils.isPreFlightRequest(exchange.getRequest())) {
					return handlePreFlight(exchange);
				} else {
					applyCorsHeaders(exchange);
				}
			}
			return chain.filter(exchange);
		}

		private Mono<Void> handlePreFlight(ServerWebExchange exchange) {
			applyCorsHeaders(exchange);
			exchange.getResponse().setStatusCode(HttpStatus.OK);
			return Mono.empty();
		}

		private void applyCorsHeaders(ServerWebExchange exchange) {
			var config = configSource.getCorsConfiguration(exchange);
			if (config != null) {
				HttpHeaders responseHeaders = exchange.getResponse().getHeaders();

				if (config.getAllowedOrigins() != null) {
					config.getAllowedOrigins()
							.forEach(origin -> responseHeaders.add("Access-Control-Allow-Origin", origin));
				}
				if (config.getAllowedMethods() != null) {
					config.getAllowedMethods()
							.forEach(method -> responseHeaders.add("Access-Control-Allow-Methods", method));
				}
				if (config.getAllowedHeaders() != null) {
					config.getAllowedHeaders()
							.forEach(header -> responseHeaders.add("Access-Control-Allow-Headers", header));
				}
				if (Boolean.TRUE.equals(config.getAllowCredentials())) {
					responseHeaders.add("Access-Control-Allow-Credentials", "true");
				}
			}
		}

	}
}