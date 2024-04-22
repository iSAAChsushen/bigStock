package com.bigstock.sharedComponent.dto;

import java.util.Date;

import lombok.Data;

@Data
public class SingleStockPriceBizVo {
	
	private String stockCode;
	private Date searchDate;
}
