package com.bigstock.schedule.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.bigstock.schedule.utils.ChromeDriverUtils;
import com.bigstock.sharedComponent.entity.ShareholderStructure;
import com.bigstock.sharedComponent.entity.StockInfo;
import com.bigstock.sharedComponent.service.ShareholderStructureService;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class GraspShareholderStructureService {

	@Value("${schedule.chromeDriverPath.windows.active}")
	private boolean windowsActive;

	@Value("${schedule.chromeDriverPath.windows.path}")
	private String windowsChromeDriverPath;

	@Value("${schedule.chromeDriverPath.linux.active}")
	private boolean linuxActive;

	@Value("${schedule.chromeDriverPath.linux.driver-path}")
	private String linuxChromeDriverPath;
	@Value("${schedule.task.scheduling.cron.expression.sync-start-date}")
	private String syncStartDate;
	
	
//	@Value("${schedule.chromeDriverPath.linux.chrome-path}")
//	private String linuxChromePath;

	@Value("${schedule.tdccQryStockUrl}")
	private String tdccQryStockUrl;

	@Value("${schedule.overTheCounterUrl}")
	private String overTheCounterUrl;

	private final RedissonClient redissonClient;

	private final ShareholderStructureService shareholderStructureService;

	private final StockInfoService stockInfoService;

	@PostConstruct
	// 每周日早上8点触发更新
//	@Scheduled(cron = "${schedule.task.scheduling.cron.expression.update-shareholder-structure}")
	public void updateShareholderStructure() {
		// 先抓DB裡面全部的代號資料
		List<String> allDataBaseStockCode = stockInfoService.getAllStockCode();
		allDataBaseStockCode.stream().forEach(stockCode -> {
			try {
				log.info("begining sync stockCode {}",stockCode);
				List<ShareholderStructure> ssList = shareholderStructureService
						.getShareholderStructureByStockCodeDesc(stockCode);
				if (!ssList.isEmpty()) {
					ShareholderStructure lastesShareholderStructure = shareholderStructureService
							.getShareholderStructureByStockCodeDesc(stockCode).get(0);
					String weekOfYear = shareholderStructureService.getMaxWeekOfYear();
					int wIndex = weekOfYear.indexOf('W');
					String year = weekOfYear.substring(wIndex - 2, wIndex);
					List<String> areadyFinshGrapsStockCodes = shareholderStructureService.getAreadyFinshGrapsStockCode(weekOfYear);
					if(areadyFinshGrapsStockCodes.contains(stockCode)) {
						return;
					}
					refreshStockLatestInfo(stockCode, lastesShareholderStructure.getStockName(),
							convertToDate(year + lastesShareholderStructure.getCountDate()));
				} else {
					Optional<StockInfo> stockInfoOp = stockInfoService.findById(stockCode);
					if (stockInfoOp.isPresent()) {
						log.info("ssList is empty : {}, so create data", stockCode);
						refreshStockLatestInfo(stockCode, stockInfoOp.get().getStockName(), syncStartDate);
					} else {
						log.info(String.format("ssList is empty : %1s , and StockInfo is not exsits either", stockCode)   );
					}
				}
			} catch (InterruptedException e) {
				log.error(e.getMessage(), e);
			}
			log.info("finsh sync stockCode {}",stockCode);
		});
	}

	private String convertToDate(String inputDate) {
		// 解析年份的后两位
		String year = "20" + inputDate.substring(0, 2);

		// 解析周数和天数
		String mounth = inputDate.substring(2, 4);
		String day = inputDate.substring(5);

		// 将日期格式化为字符串
		return year + mounth + day;
	}

	private void refreshStockLatestInfo(String stockCode, String stockName, String latestCountDateStr)
			throws InterruptedException {
		List<Map<Integer, String>> weekInfos = ChromeDriverUtils.graspShareholderStructure(
				windowsActive ? windowsChromeDriverPath : linuxChromeDriverPath, tdccQryStockUrl, stockCode,
				latestCountDateStr);
		if (CollectionUtils.isNotEmpty(weekInfos)) {
			List<ShareholderStructure> shareholderStructures = weekInfos.stream().map(weekInfo -> {
				return createShareholderStructure(weekInfo, stockCode, stockName);
			}).toList();
			shareholderStructureService.insert(shareholderStructures);
			clearCache(stockCode);
		}
	}

	private void clearCache(String stockCode) {
		// 设置最大缓存大小
		RMapCache<String, RMapCache<String, ShareholderStructure>> outerMapCache = redissonClient
				.getMapCache("shareholderStructures");

		// 尝试从缓存获取数据
		RMapCache<String, ShareholderStructure> innerMapCache = outerMapCache.get(stockCode);
		innerMapCache.delete();
		outerMapCache.remove(stockCode);
		// 插入最新的缓存数据
//		sortedValues.stream()
	}

	private ShareholderStructure createShareholderStructure(Map<Integer, String> weekInfo, String stockCode,
			String stockName) {
		ShareholderStructure shareholderStructure = new ShareholderStructure();

		weekInfo.forEach((key, value) -> {
			switch (key) {
			case 0 -> shareholderStructure.setWeekOfYear(value);
			case 1 -> shareholderStructure.setCountDate(value);
			case 2 -> shareholderStructure.setClosingPrice(value);
			case 3 -> shareholderStructure.setPriceChange(value);
			case 4 -> shareholderStructure.setPriceChangePercent(value);
			case 5 -> shareholderStructure.setTdccStock(value);
			case 6 -> shareholderStructure.setLessThanOneBoardLot(value);
			case 7 -> shareholderStructure.setBetweenOneAndFiveBoardLot(value);
			case 8 -> shareholderStructure.setBetweenFiveAndTenBoardLot(value);
			case 9 -> shareholderStructure.setBetweenTenAndFifteenBoardLot(value);
			case 10 -> shareholderStructure.setBetweenFifteenAndTwentyBoardLot(value);
			case 11 -> shareholderStructure.setBetweenTwentyAndThirtyBoardLot(value);
			case 12 -> shareholderStructure.setBetweenThirtyAndFortyBoardLot(value);
			case 13 -> shareholderStructure.setBetweenFortyAndFiftyBoardLot(value);
			case 14 -> shareholderStructure.setBetweenFiftyAndOneHundredBoardLot(value);
			case 15 -> shareholderStructure.setBetweenOneHundredAndTwoHundredBoardLot(value);
			case 16 -> shareholderStructure.setBetweenTwoHundredAndFourHundredBoardLot(value);
			case 17 -> shareholderStructure.setBetweenFourHundredAndSixHundredBoardLot(value);
			case 18 -> shareholderStructure.setBetweenSixHundredAndEightHundredBoardLot(value);
			case 19 -> shareholderStructure.setBetweenEightHundredAndOneThousandBoardLot(value);
			case 20 -> shareholderStructure.setOverOneThousandBoardLot(value);
			case 21 -> shareholderStructure.setStockTotal(value);
			}
		});
		shareholderStructure.setStockCode(stockCode);
		shareholderStructure.setStockName(stockName);
		shareholderStructure.setId(stockCode + shareholderStructure.getWeekOfYear());
		return shareholderStructure;
	}

	@PostConstruct
//	@Scheduled(cron = "${schedule.task.scheduling.cron.expression.update-stock-info}")
	public void updateStockInfo() throws InterruptedException {
		List<StockInfo> stockInfos = ChromeDriverUtils
				.grepStockInfo(windowsActive ? windowsChromeDriverPath : linuxChromeDriverPath, overTheCounterUrl);
		stockInfoService.insertAll(stockInfos);
		log.info("finsh sync updateStockInfo ");
	}
}
