package com.bigstock.sharedComponent.entity;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "securities_firms_day_operate", schema = "bstock")
@Data
public class SecuritiesFirmsDayOperate {

	@Id
	@Column(name = "stock_code", nullable = false)
	private String stockCode;

	@Id
	@Column(name = "trading_day", nullable = false)
	private Date tradingDay;

	@Id
	@Column(name = "seq", nullable = false)
	private String seq;

	@Column(name = "price")
	private String price;

	@Column(name = "stock_buy_amount")
	private Long stockBuyAmount;

	@Column(name = "stock_sell_amount")
	private Long stockSellAmount;

	@Getter
	@Setter
	public static class SecuritiesFirmsDayOperateId implements Serializable {
		private static final long serialVersionUID = 7425436497341174569L;

		@Id
		@Column(name = "stock_code", nullable = false)
		private String stockCode;

		@Id
		@Column(name = "trading_day", nullable = false)
		private Date tradingDay;

		@Id
		@Column(name = "seq", nullable = false)
		private String seq;
	}
}
