package com.bigstock.biz.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.bigstock.sharedComponents.entity.ShareholderStructure;
import com.bigstock.sharedComponents.service.ShareholderStructureService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BizService {

	private final ShareholderStructureService shareholderStructureService;
	
	
	public List<ShareholderStructure> getStockShareholderStructure(String stockCode){
		return shareholderStructureService.getShareholderStructureByStockCodeDesc(stockCode);
	}
}
