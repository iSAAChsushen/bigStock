package com.bigstock.sharedComponent.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bigstock.sharedComponent.entity.StockDayPrice;

public interface StockDayPriceRepository extends JpaRepository<StockDayPrice, StockDayPrice.StockDayPriceId> {

	Optional<StockDayPrice> findById(StockDayPrice id);

	List<StockDayPrice> findByStockCode(String stockCode);
	
	List<StockDayPrice> findByStockAndDateRange(String stockCode, Date startDate, Date endDate);
}
