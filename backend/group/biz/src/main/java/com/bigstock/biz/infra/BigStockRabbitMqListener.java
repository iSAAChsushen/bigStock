package com.bigstock.biz.infra;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;
import javax.xml.validation.Schema;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

import feign.Headers;
import jakarta.persistence.SequenceGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class BigStockRabbitMqListener {

//	private static final SsiInfoDataAccessMapper mapper = Mappers.getMapper(SsiInfoDataAccessMapper.class);
//	private static final String URL_401_PREFIX = "stock/deliverySSI";
//
//	private static final String URL_001_PREFIX = "stock/delivery001";

//	private static final String URL_TRANSFER_DON_PREFIX = "dummy/bd/settlement";
//
//	private static final String URL_DUMMY_ICB = "dummy/icb";

//	private final RestTemplate restTemplate;

	private final RabbitTemplate rabbitTemplate;

//	private final SsiInfoRepository ssiInfoRepository;
//
//	private final SequenceGenerator sequenceGenerator;
//
//	@Qualifier(value = "xsdSchema")
//	private final Map<String, Schema> bcssSchemaMap;
//
//	private final XSDValidate xadValidate;

//	@Value("${bcss.url-redirection}")
//	private String urlRedirection;

	@Value("${server.port}")
	private String serverPort;

	@RabbitListener(queues = "receiveTest")
	public void receiveTransferDoneMessage(String message  , @Header(name= "UUID", required = false) String uuid) throws URISyntaxException {
//		log.info(new String(message.getBody()));
		// simulations
		//		convertAndSend(this.exchange, routingKey, message, messagePostProcessor, null);
	    MessagePostProcessor messagePostProcessor = messageProperties -> {
	        messageProperties.getMessageProperties().setHeader("UUID", uuid);
	        return messageProperties;
	    };
		rabbitTemplate.convertAndSend("sendExchange", uuid, message, messagePostProcessor,new CorrelationData());
//		rabbitTemplate.convertAndSend("sendExchange", message, message,new CorrelationData());
	}

//	private void sendMessageToController(Object message, String errorQueueName, String errorTopic, String urlPrefix,
//			HttpEntity<?> type, Class<?> responseType, @Nullable List<String> pathvariables) {
//		try {
//			StringBuilder sb = new StringBuilder(
//					String.join("/", String.join(":", "http://127.0.0.1", serverPort), urlPrefix));
//			pathvariables.forEach(pathvariable -> {
//				sb.append("/").append(pathvariable);
//			});
//			restTemplate.exchange(new URI(sb.toString()), HttpMethod.POST, type, responseType);
//		} catch (Exception e) {
//			log.info("procees occurred error , but message been deliver to other queue");
//			rabbitTemplate.convertAndSend(errorQueueName, errorTopic, message);
//		}
//	}
//
//	private void handleExceptionLog(Exception e, Optional<Object> userDefinedMessageOp) {
//		String exceptionMessage = Optional.ofNullable(e.getMessage()).isEmpty() ? "NullPointerException"
//				: e.getMessage();
//		log.error(exceptionMessage, userDefinedMessageOp.isPresent() ? userDefinedMessageOp.get() : e);
//	}
}
