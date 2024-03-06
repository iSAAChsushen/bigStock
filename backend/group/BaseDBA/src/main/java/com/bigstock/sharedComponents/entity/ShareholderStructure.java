package com.bigstock.sharedComponents.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.ToString;

@Entity
@ToString
@Data
@Table(schema = "big_stock", name = "shareholder_structure")
public class ShareholderStructure {
	@Id
	@Column(name = "id")
	private String id;

	@Column(name = "stock_code")
	private String stockCode;
	
	@Column(name = "stock_name")
	private String stockName;
	
	@Column(name = "week_of_year")
	private String weekOfYear;

	@Column(name = "count_date")
	private String countDate;

	@Column(name = "closing_price")
	private BigDecimal closingPrice;

	@Column(name = "price_change")
	private String priceChange;

	@Column(name = "price_change_percent")
	private String priceChangePercent;

	@Column(name = "tdcc_stock")
	private BigDecimal tdccStock;

	@Column(name = "less_1_board_lot")
	private BigDecimal lessThanOneBoardLot;

	@Column(name = "between_1_and_5_board_lot")
	private BigDecimal betweenOneAndFiveBoardLot;

	@Column(name = "between_5_and_10_board_lot")
	private BigDecimal betweenFiveAndTenBoardLot;

	@Column(name = "between_10_and_15_board_lot")
	private BigDecimal betweenTenAndFifteenBoardLot;

	@Column(name = "between_15_and_20_board_lot")
	private BigDecimal betweenFifteenAndTwentyBoardLot;

	@Column(name = "between_20_and_30_board_lot")
	private BigDecimal betweenTwentyAndThirtyBoardLot;

	@Column(name = "between_30_and_40_board_lot")
	private BigDecimal betweenThirtyAndFortyBoardLot;

	@Column(name = "between_40_and_50_board_lot")
	private BigDecimal betweenFortyAndFiftyBoardLot;

	@Column(name = "between_50_and_100_board_lot")
	private BigDecimal betweenFiftyAndOneHundredBoardLot;

	@Column(name = "between_100_and_200_board_lot")
	private BigDecimal betweenOneHundredAndTwoHundredBoardLot;

	@Column(name = "between_200_and_400_board_lot")
	private BigDecimal betweenTwoHundredAndFourHundredBoardLot;

	@Column(name = "between_400_and_600_board_lot")
	private BigDecimal betweenFourHundredAndSixHundredBoardLot;

	@Column(name = "between_600_and_800_board_lot")
	private BigDecimal betweenSixHundredAndEightHundredBoardLot;

	@Column(name = "between_800_and_1000_board_lot")
	private BigDecimal betweenEightHundredAndOneThousandBoardLot;

	@Column(name = "over_1000_board_lot")
	private BigDecimal overOneThousandBoardLot;
}
