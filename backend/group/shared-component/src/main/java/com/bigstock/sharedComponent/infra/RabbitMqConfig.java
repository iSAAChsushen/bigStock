package com.bigstock.sharedComponent.infra;

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
@ConditionalOnProperty(name = "spring.rabbitmq.host", havingValue = "true", matchIfMissing = false)
public class RabbitMqConfig {

	public static final String INNER_EXCAHNGE_QUEUE = "bigStock_inner_queue";

	@Value("${spring.rabbitmq.host}")
	private String rabbitmqHost;

	@Value("${spring.rabbitmq.port}")
	private int rabbitmqPort;

	@Value("${spring.rabbitmq.username}")
	private String rabbitmqUsername;

	@Value("${spring.rabbitmq.password}")
	private String rabbitmqPassword;

	@Value("${spring.rabbitmq.virtual-host}")
	private String rabbitmqVirtualHost;

	@Value("${spring.rabbitmq.pool.channelCacheSize}")
	private int channelCacheSize;

	@Bean
	public ConnectionFactory rabbitConnectionFactory() {
		CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
		connectionFactory.setChannelCacheSize(channelCacheSize);
		connectionFactory.setHost(rabbitmqHost);
		connectionFactory.setPort(rabbitmqPort);
		connectionFactory.setUsername(rabbitmqUsername);
		connectionFactory.setPassword(rabbitmqPassword);
		connectionFactory.setVirtualHost(rabbitmqVirtualHost);
		return connectionFactory;
	}

	@Bean
	public RabbitTemplate rabbitTemplate() {
		return new RabbitTemplate(rabbitConnectionFactory());
	}

	
}
