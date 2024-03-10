package com.bigstock.biz.ctroller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bigstock.sharedComponent.entity.ShareholderStructure;
import com.bigstock.sharedComponent.service.ShareholderStructureService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("Biz")
public class BizController {

	private final ShareholderStructureService shareholderStructureService;

	@GetMapping("stockShareholderStructure/{stockCode}")
	public ResponseEntity<List<ShareholderStructure>> getStockShareholderStructure(
			@PathVariable("stockCode") String stockCode) {
		return ResponseEntity.ok(shareholderStructureService.getShareholderStructureByStockCodeDesc(stockCode));
	}
}
