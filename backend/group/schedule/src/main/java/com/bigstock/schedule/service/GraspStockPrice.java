package com.bigstock.schedule.service;

import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import com.bigstock.schedule.utils.ChromeDriverUtils;
import com.bigstock.sharedComponent.entity.StockDayPrice;
import com.bigstock.sharedComponent.service.StockDayPriceService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class GraspStockPrice {
	@Value("${schedule.chromeDriverPath.windows.active}")
	private boolean windowsActive;

	@Value("${schedule.chromeDriverPath.windows.path}")
	private String windowsChromeDriverPath;

	@Value("${schedule.chromeDriverPath.linux.active}")
	private boolean linuxActive;

	@Value("${schedule.chromeDriverPath.linux.driver-path}")
	private String linuxChromeDriverPath;
	
//	@Value("${schedule.chromeDriverPath.linux.chrome-path}")
//	private String linuxChromePath;

	@Value("${schedule.stock-price.url.tpex}")
	private String stockPriceTPEXUrl;

	@Value("${schedule.stock-price.url.twse}")
	private String stockPriceTWSEUrl;

	private final StockInfoService stockInfoService;

	private final StockDayPriceService stockDayPriceService;

//	@PostConstruct
	// 每周日早上8点触发更新
	@Scheduled(cron = "${schedule.task.scheduling.cron.expression.grasp-stock-price}")
	public void updateShareholderStructure() throws RestClientException, URISyntaxException {
		// 先抓DB裡面全部的代號資料
		List<String> allDataBaseStockCode = stockInfoService.getAllStockCode();
		List<StockDayPrice> stockDayPrices = ChromeDriverUtils
				.graspStockPrice(stockPriceTWSEUrl, stockPriceTPEXUrl, allDataBaseStockCode)
				.stream().filter(map -> StringUtils.isNotBlank(map.get("stock_code"))).map(map -> {
					StockDayPrice stockDayPrice = new StockDayPrice();
					try {
						LocalDate today = Instant.ofEpochMilli(new SimpleDateFormat("yyyyMMdd").parse(map.get("trading_day")).getTime())
							      .atZone(ZoneId.systemDefault())
							      .toLocalDate();
						
						// 設置本周第一天的日期
						LocalDate startOfWeekLocalDate = today.with(DayOfWeek.MONDAY);

						// 設置本周最後一天的日期
						LocalDate endOfWeekLocalDate = today.with(DayOfWeek.SUNDAY);
						// 獲取系統默認時區
						ZoneId zoneId = ZoneId.systemDefault();

						// 獲取偏移量
						ZoneOffset zoneOffset = zoneId.getRules().getOffset(startOfWeekLocalDate.atStartOfDay());

						// 將 LocalDate 轉換為 Date
						Date startOfWeeDate = Date.from(startOfWeekLocalDate.atStartOfDay().toInstant(zoneOffset));
						Date endOfWeekDate = Date.from(endOfWeekLocalDate.atStartOfDay().toInstant(zoneOffset));
						stockDayPrice.setStockCode(map.get("stock_code"));
						stockDayPrice.setOpeningPrice(map.get("opening_price"));
						stockDayPrice.setClosingPrice(map.get("closing_price"));
						stockDayPrice.setHighPrice(map.get("high_price"));
						stockDayPrice.setLowPrice(map.get("low_price"));
						stockDayPrice.setTradingDay(new SimpleDateFormat("yyyyMMdd").parse(map.get("trading_day")));
						stockDayPrice.setStartOfWeekDate(startOfWeeDate);
						stockDayPrice.setEndOfWeekDate(endOfWeekDate);
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						log.error(e.getMessage(), e);
						return null;
					}
					return stockDayPrice;
				}).filter(stockDayPrice -> Optional.ofNullable(stockDayPrice).isPresent()).toList();
		stockDayPriceService.saveAll(stockDayPrices);
	}
}
