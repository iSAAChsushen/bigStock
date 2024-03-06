package com.bigstock.schedule.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.bigstock.sharedComponents.repository.StockInfoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StockInfoService {
	private final StockInfoRepository stockInfoRepository;

	public List<String> getAllStockCode() {
		return stockInfoRepository.getAllStockCode();
	}
}
