package com.bigstock.biz.infra;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

	// 401 Exchange
	public String singleStockPriceExchangeError = "SingleStockPriceExchangeError";

	// 401 Queue
	public String singleStockPrice = "SingleStockPriceQueue";
	public String singleStockPriceQueueError = "SingleStockPriceQueueError";

	// 401 Exchange
	public String receivedSingleStockPriceExchangeError = "ReceivedSingleStockPriceExchangeError";

	// 401 Queue
	public String receivedSingleStockPrice = "ReceivedSingleStockPrice";
	public String receivedStockPriceQueueError = "ReceivedStockPriceQueueError";

	// ---------401

	@Bean
	public DirectExchange singleStockPriceExchange() {
		return new DirectExchange("SingleStockPriceExchange", true, false);
	}

	@Bean
	public Queue singleStockPriceQueue() {
		return QueueBuilder.durable("SingleStockPriceQueue").build();
	}

	@Bean
	public Binding binding401() {
		return BindingBuilder.bind(singleStockPriceQueue()).to(singleStockPriceExchange()).withQueueName();
	}

	@Bean
	public DirectExchange singleStockPriceExchangeError() {
		return new DirectExchange("SingleStockPriceExchangeError", true, false);
	}

	@Bean
	public Queue singleStockPriceQueueError() {
		return QueueBuilder.durable("SingleStockPriceQueueError").build();
	}

	@Bean
	public Binding bindingSingleStockPriceError() {
		return BindingBuilder.bind(singleStockPriceQueueError()).to(singleStockPriceExchangeError()).withQueueName();
	}
	
	
	@Bean
	public DirectExchange shareholderStructureIncreaseExchange() {
		return new DirectExchange("ShareholderStructureIncreaseExchange", true, false);
	}

	@Bean
	public Queue shareholderStructureIncreaseQueue() {
		return QueueBuilder.durable("ShareholderStructureIncreaseQueue").build();
	}

	@Bean
	public Binding shareholderStructureIncreaseBinding() {
		return BindingBuilder.bind(shareholderStructureIncreaseQueue()).to(shareholderStructureIncreaseExchange()).withQueueName();
	}

	@Bean
	public DirectExchange shareholderStructureIncreaseExchangeError() {
		return new DirectExchange("ShareholderStructureIncreaseExchangeError", true, false);
	}

	@Bean
	public Queue shareholderStructureIncreaseQueueError() {
		return QueueBuilder.durable("ShareholderStructureIncreaseQueueError").build();
	}

	@Bean
	public Binding bindingSingleStockPriceQueueError() {
		return BindingBuilder.bind(shareholderStructureIncreaseQueueError()).to(shareholderStructureIncreaseExchangeError()).withQueueName();
	}
	
}