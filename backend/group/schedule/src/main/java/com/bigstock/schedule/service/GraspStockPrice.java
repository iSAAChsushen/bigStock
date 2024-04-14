package com.bigstock.schedule.service;

import java.util.List;
import java.util.Optional;

import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

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
public class GraspStockPrice {
	@Value("${schedule.chromeDriverPath.windows.active}")
	private boolean windowsActive;

	@Value("${schedule.chromeDriverPath.windows.path}")
	private String windowsChromeDriverPath;

	@Value("${schedule.chromeDriverPath.linux.active}")
	private boolean linuxActive;

	@Value("${schedule.chromeDriverPath.linux.path}")
	private String linuxChromeDriverPath;
	
	private final StockInfoService stockInfoService;
	

	@PostConstruct
	// 每周日早上8点触发更新
//	@Scheduled(cron = "0 0 20 ? * *")
	public void updateShareholderStructure() {
		// 先抓DB裡面全部的代號資料
		List<String> allDataBaseStockCode = stockInfoService.getAllStockCode();
		allDataBaseStockCode.stream().forEach(stockCode -> {
			try {
				
				
			} catch (InterruptedException e) {
				log.error(e.getMessage(), e);
			}
		});
	}
}
