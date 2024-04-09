package com.bigstock.gateway.web;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.bigstock.sharedComponent.service.RabbitMqService;

@RestController()
@RequestMapping(value = "gateway")
public class GatewayController {

	@Autowired
	RabbitMqService rabbitMqService;

	@PostMapping("forTest")
	@PreAuthorize("hasRole('Admin7')")
	public String forTest() {
		return "Sueescc";
	}

	@PostMapping("testInnerRabbitMq/{message}")
//	@PreAuthorize("hasRole('Admin')")
	public ResponseEntity<String> testInnerRabbitMq(@PathVariable("message") String message)
			throws IOException, TimeoutException {
		RestTemplate restTemplate = new RestTemplate();
		// 构造 URL，包括用户名、密码和范围作为查询参数
		String url = "http://big-stock-auth-server/auth/token?username=093801710&password=0938017103&scope=read";

		// 发送 POST 请求并获取响应
		String response = restTemplate.postForObject(url, null, String.class);
		String uuid = rabbitMqService.createConsumer();
//		rabbitMqService.createConsumer1();
		rabbitMqService.sendMessage(message, "receiveTestExchange", null, uuid);
		return ResponseEntity.ok().body(HttpStatus.ACCEPTED.toString());
	}

}
