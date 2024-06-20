package com.bigstock.sharedComponent.dto;

import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class SingleStockPriceBizVo {
	
	@Schema(name = "stockCode", description = "股票代號")
	private String stockCode;
	@Schema(name = "searchDate", description = "查詢日期")
	private Date searchDate;
}
