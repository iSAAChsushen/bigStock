package com.bigstock.schedule;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication(scanBasePackages = {"com.bigstock.schedule","com.bigstock.sharedComponent"})
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
@EnableCaching
public class ScheduleApp {
	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(ScheduleApp.class);
		app.run(args);
	}

}
