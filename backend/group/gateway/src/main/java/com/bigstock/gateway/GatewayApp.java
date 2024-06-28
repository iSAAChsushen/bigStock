package com.bigstock.gateway;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication(scanBasePackages = {"com.bigstock.gateway","com.bigstock.sharedComponent"})
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
@EnableCaching
@EnableDiscoveryClient // 启用服务发现客户端
@EnableFeignClients // 启用Feign客户端
public class GatewayApp {
	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(GatewayApp.class);
		app.run(args);
	}

}
