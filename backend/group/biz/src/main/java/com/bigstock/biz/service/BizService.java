package com.bigstock.biz.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.bigstock.sharedComponent.entity.ShareholderStructure;
import com.bigstock.sharedComponent.service.ShareholderStructureService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BizService {

	private final ShareholderStructureService shareholderStructureService;
	
	
	public List<ShareholderStructure> getStockShareholderStructure(String stockCode){
		return shareholderStructureService.getShareholderStructureByStockCodeDesc(stockCode);
	}
}
