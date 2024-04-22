package com.bigstock.sharedComponent.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bigstock.sharedComponent.entity.StockDayPrice;

import jakarta.persistence.Column;

public interface StockDayPriceRepository extends JpaRepository<StockDayPrice, StockDayPrice.StockDayPriceId> {

	List<StockDayPrice> findByStockCode(String stockCode);

	@Query("select t from StockDayPrice t where t.stockCode = :stockCode and t.startOfWeekDate = :startOfWeekDate and t.endOfWeekDate =:endOfWeekDate order by t.tradingDay asc")
	List<StockDayPrice> findThisWeekStockDayPrices(@Param("stockCode") String stockCode,
			@Param("startOfWeekDate") Date startOfWeekDate, @Param("endOfWeekDate") Date endOfWeekDate);
	
}
