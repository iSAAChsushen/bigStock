package com.bigstock.gateway.web;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.json.JSONObject;
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

import com.bigstock.gateway.domain.vo.UserInloginInfo;
import com.bigstock.gateway.protocol.OAuth2Client;
import com.bigstock.sharedComponent.service.RabbitMqService;

import lombok.extern.slf4j.Slf4j;

@RestController()
@RequestMapping(value = "gateway")
@Slf4j
public class GatewayController {

	@Autowired
	RabbitMqService rabbitMqService;

//	@Autowired
//	RestTemplate restTemplate;
	
	
	@Autowired
	OAuth2Client oauth2Client;
	
	@PostMapping("forTest")
	@PreAuthorize("hasRole('Admin7')")
	public String forTest() {
		return "Sueescc";
	}

	@PostMapping("testInnerRabbitMq/{message}")
//	@PreAuthorize("hasRole('Admin')")
	public ResponseEntity<String> testInnerRabbitMq(@PathVariable("message") String message)
			throws IOException, TimeoutException {
		// 构造 URL，包括用户名、密码和范围作为查询参数
		try {
		String url = "lb://big-stock-auth-server/auth/login";
		JSONObject json = new JSONObject();
		json.put("username", "0938017103");
		json.put("password", "0938017103");
		// 发送 POST 请求并获取响应
		UserInloginInfo userInloginInfo = new UserInloginInfo();
		userInloginInfo.setUserName("0938017103");
		userInloginInfo.setPassword("0938017103");
		String response = oauth2Client.authLogin(userInloginInfo);
		} catch(Exception e) {
			log.error(e.getMessage(), e);
		}
		String uuid = rabbitMqService.createConsumer();
//		rabbitMqService.createConsumer1();
		rabbitMqService.sendMessage(message, "receiveTestExchange", null, uuid);
		return ResponseEntity.ok().body(HttpStatus.ACCEPTED.toString());
	}

}
