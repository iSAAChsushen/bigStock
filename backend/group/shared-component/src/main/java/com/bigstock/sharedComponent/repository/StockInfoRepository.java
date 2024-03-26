package com.bigstock.sharedComponent.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.bigstock.sharedComponent.entity.StockInfo;

public interface StockInfoRepository extends JpaRepository<StockInfo, String> {
	@Query(value =" select t.stockCode from StockInfo t")
	List<String> getAllStockCode();
}