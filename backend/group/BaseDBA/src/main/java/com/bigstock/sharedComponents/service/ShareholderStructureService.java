package com.bigstock.sharedComponents.service;

import java.util.List;
import java.util.Optional;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.bigstock.sharedComponents.entity.ShareholderStructure;
import com.bigstock.sharedComponents.repository.ShareholderStructureRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ShareholderStructureService {
	private final ShareholderStructureRepository shareholderStructureRepository;

	public List<ShareholderStructure> getAll() {
		return shareholderStructureRepository.findAll();
	}

	public Optional<ShareholderStructure> getById(String id) {
		return shareholderStructureRepository.findById(id);
	}

	public ShareholderStructure insert(ShareholderStructure shareholderStructure) {
		return shareholderStructureRepository.save(shareholderStructure);
	}

	public List<ShareholderStructure> insert(List<ShareholderStructure> shareholderStructures) {
		return shareholderStructureRepository.saveAll(shareholderStructures);
	}

	public void delete(String id) {
		shareholderStructureRepository.deleteById(id);
	}

	public void delete(ShareholderStructure shareholderStructure) {
		shareholderStructureRepository.delete(shareholderStructure);
	}

	@Cacheable(value = "stockShareholderStructure", key = "#stockCode")
	public List<ShareholderStructure> getShareholderStructureByStockCodeDesc(String stockCode) {
		return shareholderStructureRepository.getShareholderStructureByStockCodeDesc(stockCode);
	}

}
