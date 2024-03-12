package com.bigstock.sharedComponent.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.ToString;

@Entity
@ToString
@Data
@Table(schema = "bstock", name = "shareholder_structure")
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
	private String closingPrice;

	@Column(name = "price_change")
	private String priceChange;

	@Column(name = "price_change_percent")
	private String priceChangePercent;

	@Column(name = "tdcc_stock")
	private String tdccStock;

	@Column(name = "less_1_board_lot")
	private String lessThanOneBoardLot;

	@Column(name = "between_1_and_5_board_lot")
	private String betweenOneAndFiveBoardLot;

	@Column(name = "between_5_and_10_board_lot")
	private String betweenFiveAndTenBoardLot;

	@Column(name = "between_10_and_15_board_lot")
	private String betweenTenAndFifteenBoardLot;

	@Column(name = "between_15_and_20_board_lot")
	private String betweenFifteenAndTwentyBoardLot;

	@Column(name = "between_20_and_30_board_lot")
	private String betweenTwentyAndThirtyBoardLot;

	@Column(name = "between_30_and_40_board_lot")
	private String betweenThirtyAndFortyBoardLot;

	@Column(name = "between_40_and_50_board_lot")
	private String betweenFortyAndFiftyBoardLot;

	@Column(name = "between_50_and_100_board_lot")
	private String betweenFiftyAndOneHundredBoardLot;

	@Column(name = "between_100_and_200_board_lot")
	private String betweenOneHundredAndTwoHundredBoardLot;

	@Column(name = "between_200_and_400_board_lot")
	private String betweenTwoHundredAndFourHundredBoardLot;

	@Column(name = "between_400_and_600_board_lot")
	private String betweenFourHundredAndSixHundredBoardLot;

	@Column(name = "between_600_and_800_board_lot")
	private String betweenSixHundredAndEightHundredBoardLot;

	@Column(name = "between_800_and_1000_board_lot")
	private String betweenEightHundredAndOneThousandBoardLot;

	@Column(name = "over_1000_board_lot")
	private String overOneThousandBoardLot;
}
