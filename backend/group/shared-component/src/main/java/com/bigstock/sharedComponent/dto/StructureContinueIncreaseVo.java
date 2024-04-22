package com.bigstock.sharedComponent.dto;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import lombok.Data;

/**
 * "個股名稱","個股編號","個股總發行股數","400張以上大股東持股數","當週日期"
 */
@Data
public class StructureContinueIncreaseVo implements Cloneable, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -443915022909121243L;

	
	@Schema(name = "股票名稱", description = "", example = "")
	private String stockName;

	@Schema(name = "股票代號", description = "", example = "")
	private String stockCode;

	@Schema(name = "總發行股數", description = "是否執行刪除", example = "")
	private String stockTotal;

	@Schema(name = "isDelete", description = "是否執行刪除", example = "")
	private String weekOfYear;

	@Schema(name = "isDelete", description = "是否執行刪除", example = "")
	private String betweenFourHundredAndSixHundredBoardLot;

	@Schema(name = "isDelete", description = "是否執行刪除", example = "")
	private String betweenSixHundredAndEightHundredBoardLot;

	@Schema(name = "isDelete", description = "是否執行刪除", example = "")
	private String betweenEightHundredAndOneThousandBoardLot;

	@Schema(name = "isDelete", description = "是否執行刪除", example = "")
	private String overOneThousandBoardLot;
}
