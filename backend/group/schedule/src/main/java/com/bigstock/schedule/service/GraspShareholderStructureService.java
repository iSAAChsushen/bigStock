package com.bigstock.schedule.service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.bigstock.schedule.utils.ChromeDriverUtils;
import com.bigstock.sharedComponents.entity.ShareholderStructure;
import com.bigstock.sharedComponents.service.ShareholderStructureService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class GraspShareholderStructureService {

	@Value("schedule.chromeDriverPath")
	private String chromeDriverPath;
	@Value("schedule.tdccQryStockUrl")
	private String tdccQryStockUrl;
	
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
		List<Map<Integer, String>> weekInfos = ChromeDriverUtils.graspStockInfo(chromeDriverPath, tdccQryStockUrl,
				stockCode, latestCountDateStr);
		if (CollectionUtils.isNotEmpty(weekInfos)) {
			List<ShareholderStructure> shareholderStructures = weekInfos.stream().map(weekInfo -> {
				return createShareholderStructure(weekInfo, stockCode, stockName);
			}).toList();
			refres(stockCode, shareholderStructures);
		}
	}
	

	private void refres(String stockCode, List<ShareholderStructure> shareholderStructures) {
		shareholderStructureService.insert(shareholderStructures);
		// 使用Redisson客户端获取列表
        RList<List<ShareholderStructure>> weekInfoList = redissonClient.getList(stockCode);
        // 移除最旧的周数据
        if (weekInfoList.size() >= 26) {
            weekInfoList.remove(0); // 移除列表第一个元素，即最旧的数据
        }
        // 插入最新周数据到列表末尾
        weekInfoList.add(shareholderStructures);
        weekInfoList.expire(Duration.ofHours(24)); 
	}
	
	private ShareholderStructure createShareholderStructure(Map<Integer, String> weekInfo, String stockCode,
			String stockName) {
		ShareholderStructure shareholderStructure = new ShareholderStructure();

		weekInfo.forEach((key, value) -> {
			switch (key) {
			case 0 -> shareholderStructure.setWeekOfYear(value);
			case 1 -> shareholderStructure.setCountDate(value);
			case 2 -> shareholderStructure.setClosingPrice(new BigDecimal(value));
			case 3 -> shareholderStructure.setPriceChange(value);
			case 4 -> shareholderStructure.setPriceChangePercent(value);
			case 5 -> shareholderStructure.setTdccStock(new BigDecimal(value));
			case 6 -> shareholderStructure.setLessThanOneBoardLot(new BigDecimal(value));
			case 7 -> shareholderStructure.setBetweenOneAndFiveBoardLot(new BigDecimal(value));
			case 8 -> shareholderStructure.setBetweenFiveAndTenBoardLot(new BigDecimal(value));
			case 9 -> shareholderStructure.setBetweenTenAndFifteenBoardLot(new BigDecimal(value));
			case 10 -> shareholderStructure.setBetweenFifteenAndTwentyBoardLot(new BigDecimal(value));
			case 11 -> shareholderStructure.setBetweenTwentyAndThirtyBoardLot(new BigDecimal(value));
			case 12 -> shareholderStructure.setBetweenThirtyAndFortyBoardLot(new BigDecimal(value));
			case 13 -> shareholderStructure.setBetweenFortyAndFiftyBoardLot(new BigDecimal(value));
			case 14 -> shareholderStructure.setBetweenFiftyAndOneHundredBoardLot(new BigDecimal(value));
			case 15 -> shareholderStructure.setBetweenOneHundredAndTwoHundredBoardLot(new BigDecimal(value));
			case 16 -> shareholderStructure.setBetweenTwoHundredAndFourHundredBoardLot(new BigDecimal(value));
			case 17 -> shareholderStructure.setBetweenFourHundredAndSixHundredBoardLot(new BigDecimal(value));
			case 18 -> shareholderStructure.setBetweenSixHundredAndEightHundredBoardLot(new BigDecimal(value));
			case 19 -> shareholderStructure.setBetweenEightHundredAndOneThousandBoardLot(new BigDecimal(value));
			case 20 -> shareholderStructure.setOverOneThousandBoardLot(new BigDecimal(value));
			}
		});
		shareholderStructure.setStockCode(stockCode);
		shareholderStructure.setStockName(stockName);
		shareholderStructure.setId(stockCode + stockName + shareholderStructure.getWeekOfYear());
		return shareholderStructure;
	}
}
