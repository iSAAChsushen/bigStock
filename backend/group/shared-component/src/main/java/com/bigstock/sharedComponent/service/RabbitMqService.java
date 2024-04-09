package com.bigstock.sharedComponent.service;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Delivery;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.rabbitmq.client.Channel;

@Service
@RequiredArgsConstructor
@Slf4j
public class RabbitMqService {

	private final RabbitTemplate rabbitTemplate;

	public void sendMessage(String message, String exchange, String routeKey, String UUidString) {
		MessagePostProcessor messagePostProcessor = messageProperties -> {
			messageProperties.getMessageProperties().setHeader("UUID",
					UUidString);
			return messageProperties;
		};
		rabbitTemplate.convertAndSend(exchange, null, message,messagePostProcessor, new CorrelationData());
	}

//	public void sendMessage(String exchange, String routeKey, String UUidString) {
//		rabbitTemplate.convertAndSend("receiveTestExchange", null, UUidString, new CorrelationData());
//	}

	public String createConsumer() throws IOException, TimeoutException {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("127.0.0.1"); // 或者你的 RabbitMQ 服务器的主机名
		factory.setPort(5672); // AMQP 协议的默认端口
		String UUIDString = UUID.randomUUID().toString();
		factory.setUsername("admin");
		factory.setPassword("rabbitmq");
		Connection connection = null;
		Channel channel = null;

		connection = factory.newConnection();
		channel = connection.createChannel();

		// 将现有队列绑定到现有交换机上
		// 使用AtomicReference包装Channel
		channel.queueBind("sendQueue", "sendExchange", UUIDString);
		// 定义消息消费者

		// 开始消费消息
		System.out.println("Waiting for messages. To exit press CTRL+C");
		channel.basicConsume("sendQueue", true, new MyDeliverCallback(connection, channel), consumerTag -> {
		});
		return UUIDString;
	}

	public String createConsumer1() throws IOException, TimeoutException {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("127.0.0.1"); // 或者你的 RabbitMQ 服务器的主机名
		factory.setPort(5672); // AMQP 协议的默认端口
		String UUIDString = UUID.randomUUID().toString();
		factory.setUsername("admin");
		factory.setPassword("rabbitmq");
		Connection connection = null;
		Channel channel = null;

		connection = factory.newConnection();
		channel = connection.createChannel();

		// 将现有队列绑定到现有交换机上
		// 使用AtomicReference包装Channel
		channel.queueBind("sendQueue", "sendExchange", UUIDString);

		// 开始消费消息
		System.out.println("Waiting for messages. To exit press CTRL+C");
		channel.basicConsume("sendQueue", true, new MyDeliverCallback(connection, channel), consumerTag -> {
		});
		return UUIDString;
	}

	class MyDeliverCallback implements DeliverCallback {

		public Connection connection;

		public Channel channel;

		public MyDeliverCallback(Connection connection, Channel channel) {
			this.channel = channel;
			this.connection = connection;
		}

		@Override
		public void handle(String consumerTag, Delivery delivery) throws IOException {
			// TODO Auto-generated method stub
			try {
				String uuid = delivery.getProperties().getHeaders().get("UUID").toString();
				String message = new String(delivery.getBody(), "UTF-8");
				System.out.println(message);

				// 处理完消息后取消绑定指定的路由键
				channel.queueUnbind("sendQueue", "sendExchange", uuid);

			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				if (channel != null) {
					try {
						channel.close();
					} catch (IOException | TimeoutException e) {
						e.printStackTrace();
					}
				}
				if (connection != null) {
					try {
						connection.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}

	}
}
