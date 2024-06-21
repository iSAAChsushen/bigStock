package com.bigstock.biz.infra;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApiConfig {

	@Value("${server.port}")
	String serverPort;

	@Value("${spring.application.name}")
	String springApplicationName;

	@Bean
	public OpenAPI customOpenAPI() {
		return new OpenAPI()
				.addServersItem(new Server().url("http://127.0.0.1:18080"))
				.info(new Info().title("Auth Service API").version("v1"));
	}

	@Bean
	public GroupedOpenApi bizApi() {
		return GroupedOpenApi.builder().group("biz").pathsToMatch("/api/biz/**").build();
	}
}
