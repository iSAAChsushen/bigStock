package com.bigstock.sharedComponent.service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import com.bigstock.sharedComponent.entity.SecuritiesFirmsDayOperate;
import com.bigstock.sharedComponent.entity.StockDayPrice;
import com.bigstock.sharedComponent.repository.StockDayPriceRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StockDayPriceService {
	private final  StockDayPriceRepository stockDayPriceRepository;
	private final RedissonClient redissonClient;
	
	public StockDayPrice save(StockDayPrice stockDayPrice) {
		return stockDayPriceRepository.save(stockDayPrice);
	}
	
	public void deleteByIds(List<StockDayPrice.StockDayPriceId> ids) {
		stockDayPriceRepository.deleteAllByIdInBatch(ids);
	};
	
	public Optional<StockDayPrice> findById(StockDayPrice.StockDayPriceId id){
		return stockDayPriceRepository.findById(id);
	}
	 
	public List<StockDayPrice> findByStockCode(String stockCode){
		return stockDayPriceRepository.findByStockCode(stockCode);
	}
	
	public List<StockDayPrice> findByStockAndDateRange(String stockCode, Date startDate, Date endDate) {
		return stockDayPriceRepository.findByStockAndDateRange(stockCode, startDate, endDate);
	}
}
