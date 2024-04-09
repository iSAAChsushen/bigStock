package com.bigstock.biz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.bigstock.biz","com.bigstock.sharedComponent"})
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableCaching
@EnableDiscoveryClient // 启用服务发现客户端
public class BizApp {
	public static void main(String[] args) {
		SpringApplication.run(BizApp.class, args);
	}
}
