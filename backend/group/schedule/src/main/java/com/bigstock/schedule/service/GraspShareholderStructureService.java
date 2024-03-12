package com.bigstock.schedule.service;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.bigstock.schedule.utils.ChromeDriverUtils;
import com.bigstock.sharedComponent.entity.ShareholderStructure;
import com.bigstock.sharedComponent.service.ShareholderStructureService;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class GraspShareholderStructureService {

	@Value("schedule.chromeDriverPath.windows.active")
	private boolean windowsActive;

	@Value("schedule.chromeDriverPath.windows.path")
	private String windowsChromeDriverPath;

	@Value("schedule.chromeDriverPath.linux.active")
	private boolean linuxActive;

	@Value("schedule.chromeDriverPath.linux.path")
	private String linuxChromeDriverPath;

	@Value("schedule.tdccQryStockUrl")
	private String tdccQryStockUrl;
	
	@Value("schedule.listedCompanyUrl")
	private String listedCompanyUrl;
	
	@Value("schedule.overTheCounterUrl")
	private String overTheCounterUrl;

	private final RedissonClient redissonClient;

	private final ShareholderStructureService shareholderStructureService;

	private final StockInfoService stockInfoService;

	// 每周日早上8点触发更新
	@Scheduled(cron = "0 0 8 ? * SUN")
	public void updateShareholderStructure() {
		// 先抓DB裡面全部的代號資料
		List<String> allDataBaseStockCode = stockInfoService.getAllStockCode();
		allDataBaseStockCode.stream().forEach(stockCode -> {
			ShareholderStructure lastesShareholderStructure = shareholderStructureService
					.getShareholderStructureByStockCodeDesc(stockCode).get(0);
			try {
				refreshStockLatestInfo(stockCode, lastesShareholderStructure.getStockName(),
						lastesShareholderStructure.getCountDate());
			} catch (InterruptedException e) {
				log.error(e.getMessage(), e);
			}
		});
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
			refresh(stockCode, shareholderStructures);
		}
	}

	private void refresh(String stockCode, List<ShareholderStructure> shareholderStructures) {
		// 设置最大缓存大小
		RMapCache<String, ShareholderStructure> weekInfoMapCache = redissonClient.getMapCache(stockCode);
		weekInfoMapCache.setMaxSize(26); // 设置缓存最大大小为 26 条

		// 插入最新的缓存数据
		for (ShareholderStructure ss : shareholderStructures) {
			weekInfoMapCache.put(ss.getWeekOfYear(), ss);
		}
		// 设置缓存数据的过期时间
		weekInfoMapCache.expire(Duration.ofHours(24));
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
			}
		});
		shareholderStructure.setStockCode(stockCode);
		shareholderStructure.setStockName(stockName);
		shareholderStructure.setId(stockCode + stockName + shareholderStructure.getWeekOfYear());
		return shareholderStructure;
	}

	@PostConstruct
//	@Scheduled(cron = "0 0 8 ? * SAT")
	public void updateStockInfo() throws InterruptedException {
		ChromeDriverUtils.grepStockInfo(windowsActive ? windowsChromeDriverPath : linuxChromeDriverPath, listedCompanyUrl, overTheCounterUrl);
	}
}
