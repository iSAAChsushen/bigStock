package com.bigstock.auth;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication(scanBasePackages = {"com.bigstock.auth","com.bigstock.sharedComponent"})
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableCaching
@EnableDiscoveryClient // 启用服务发现客户端
public class App {
	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(App.class);
		app.run(args);
	}

}
