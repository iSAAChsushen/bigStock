//package com.bigstock.gateway.infra;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
//import org.springframework.cloud.client.loadbalancer.LoadBalancerInterceptor;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.client.RestTemplate;
//
//@Configuration
//public class RestTemplateConfig {
//	
//	@Autowired
//    private LoadBalancerClient loadBalancerClient;
//	
//	@Bean
//	public LoadBalancerInterceptor loadBalancerInterceptor() {
//		return new LoadBalancerInterceptor(loadBalancerClient);
//	}
//
//	@Bean
//	public RestTemplate restTemplate() {
//		RestTemplate restTemplate = new RestTemplate();
//		restTemplate.getInterceptors().add(loa
//@Configuration
//public class RestTemplateConfig {
//	
//	@Autowired
//    private LoadBalancerClient loadBalancerClient;
//	
//	@Bean
//	public LoadBalancerInterceptor loadBalancerInterceptor() {
//		return new LoadBalancerInterceptor(loadBalancerClient);
//	}
//
//	@Bean
//	public RestTemplate restTemplate() {
//		RestTemplate restTemplate = new RestTemplate();
//		restTemplate.getInterceptors().add(loadBalancerInterceptor());
//		return restTemplate;
//	}dBalancerInterceptor());
//		return restTemplate;
//	}
//}