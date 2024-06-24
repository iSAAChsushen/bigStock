package com.bigstock.biz.service;

import java.time.DayOfWeek;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.bigstock.sharedComponent.dto.SingleStockPriceVo;
import com.bigstock.sharedComponent.dto.StructureContinueIncreaseVo;
import com.bigstock.sharedComponent.entity.ShareholderStructure;
import com.bigstock.sharedComponent.entity.StockDayPrice;
import com.bigstock.sharedComponent.service.ShareholderStructureService;
import com.bigstock.sharedComponent.service.StockDayPriceService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BizService {

	private final ShareholderStructureService shareholderStructureService;

	private final StockDayPriceService stockDayPriceService;

	public List<ShareholderStructure> getStockShareholderStructure(String stockCode, int limit) {

		return shareholderStructureService.getShareholderStructureByStockCodeDesc(stockCode).subList(0, limit);
	}

	public SingleStockPriceVo getSingleStockPrice(String stockCode, Date searchDate) {
		var searchLocalDate = searchDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		var startOfWeekLocalDate = searchLocalDate.with(DayOfWeek.MONDAY);

		// 設置本周最後一天的日期
		var endOfWeekLocalDate = searchLocalDate.with(DayOfWeek.SUNDAY);

		var startOfWeeDate = Date.from(startOfWeekLocalDate.atStartOfDay().atZone(ZoneId.systemDefault()) // 使用 ZoneId
																											// 对象
				.toInstant());
		var endOfWeekDate = Date.from(endOfWeekLocalDate.atStartOfDay().atZone(ZoneId.systemDefault()) // 使用 ZoneId 对象
				.toInstant());
		List<StockDayPrice> stockDayPrices = stockDayPriceService.findByStockCodeAndDateRange(stockCode, startOfWeeDate,
				endOfWeekDate);
		Optional<StockDayPrice> highPriceStockDayPriceOp = stockDayPrices.stream()
				.max(Comparator.comparing(StockDayPrice::getHighPrice));
		String highPrice = highPriceStockDayPriceOp.isPresent() ? highPriceStockDayPriceOp.get().getHighPrice() : "0.0";
		Optional<StockDayPrice> lowPriceStockDayPriceOp = stockDayPrices.stream()
				.min(Comparator.comparing(StockDayPrice::getHighPrice));
		String lowPrice = lowPriceStockDayPriceOp.isPresent() ? lowPriceStockDayPriceOp.get().getHighPrice() : "0.0";
		Optional<StockDayPrice> openingPricStockDayPriceOp = stockDayPrices.stream().findFirst();
		String openingPrice = openingPricStockDayPriceOp.isPresent()
				? openingPricStockDayPriceOp.get().getOpeningPrice()
				: "0.0";
		Optional<StockDayPrice> closingPriceStockDayPriceOp = stockDayPrices.stream().reduce((first, second) -> second);
		String closingPrice = closingPriceStockDayPriceOp.isPresent()
				? closingPriceStockDayPriceOp.get().getClosingPrice()
				: "0.0";

		SingleStockPriceVo vo = new SingleStockPriceVo();
		vo.setClosingPrice(closingPrice);
		vo.setOpeningPrice(openingPrice);
		vo.setHighPrice(highPrice);
		vo.setLowPrice(lowPrice);
		vo.setStockCode(stockCode);
		return vo;
	}

	public List<StructureContinueIncreaseVo> getShareholderStructureContinueIncreaseLastTowWeeks() {
		String firstWeekOfYear = shareholderStructureService.getMaxWeekOfYear();
		String secondMaxWeekOfYear = shareholderStructureService
				.getMaxWeekOfYearExcludeSpecificDate(List.of(firstWeekOfYear));
		String thirdMaxWeekOfYear = shareholderStructureService
				.getMaxWeekOfYearExcludeSpecificDate(List.of(firstWeekOfYear, secondMaxWeekOfYear));
		return shareholderStructureService
				.getShareholderStructureLastTwoWeeks(firstWeekOfYear, secondMaxWeekOfYear, thirdMaxWeekOfYear).stream()
				.map(shareholderStructure -> {
					StructureContinueIncreaseVo vo = new StructureContinueIncreaseVo();
					vo.setStockCode(shareholderStructure.getStockCode());
					vo.setStockName(shareholderStructure.getStockName());
					vo.setWeekOfYear(shareholderStructure.getWeekOfYear());
					vo.setStockTotal(shareholderStructure.getStockTotal());
					vo.setBetweenFourHundredAndSixHundredBoardLot(
							shareholderStructure.getBetweenFourHundredAndSixHundredBoardLot());
					vo.setBetweenSixHundredAndEightHundredBoardLot(
							shareholderStructure.getBetweenSixHundredAndEightHundredBoardLot());
					vo.setBetweenEightHundredAndOneThousandBoardLot(
							shareholderStructure.getBetweenEightHundredAndOneThousandBoardLot());
					vo.setOverOneThousandBoardLot(shareholderStructure.getOverOneThousandBoardLot());
					return vo;
				}).toList();
	}
}
