package com.bigstock.sharedComponent.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.ToString;

@Entity
@Table(name = "StockInfo", schema = "big_stock")
@ToString
@Data
public class StockInfo {

	@Id
	@Column(name = "stock_code")
	private String stockCode;

	@Column(name = "stock_name")
	private String stockName;

	@Column(name = "stock_type")
	private Boolean stockType;

}
