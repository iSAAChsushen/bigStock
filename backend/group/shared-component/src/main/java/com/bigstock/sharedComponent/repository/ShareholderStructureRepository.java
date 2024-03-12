package com.bigstock.sharedComponent.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bigstock.sharedComponent.entity.ShareholderStructure;

public interface ShareholderStructureRepository extends JpaRepository<ShareholderStructure, String> {

	@Query(value = "select s from ShareholderStructure s where s.stockCode = :stockCode order by s.countDate desc")
	public List<ShareholderStructure> getShareholderStructureByStockCodeDesc(@Param("stockCode") String stockCode);

	@Query(value = "select DISTINCT  s.stockCode from ShareholderStructure s")
	public List<String> getAllShareholderStructureStockCode();
}
