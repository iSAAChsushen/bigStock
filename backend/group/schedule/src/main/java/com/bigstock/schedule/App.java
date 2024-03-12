package com.bigstock.schedule;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication(scanBasePackages = {"com.bigstock.schedule","com.bigstock.sharedComponent"})
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableCaching
public class App {
	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(App.class);
		app.run(args);
	}

}
