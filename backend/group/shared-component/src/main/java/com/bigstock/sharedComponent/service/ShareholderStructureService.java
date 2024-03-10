package com.bigstock.sharedComponent.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import com.bigstock.sharedComponent.entity.ShareholderStructure;
import com.bigstock.sharedComponent.repository.ShareholderStructureRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ShareholderStructureService {
	private final ShareholderStructureRepository shareholderStructureRepository;
	private final RedissonClient redissonClient;

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

	public List<ShareholderStructure> getShareholderStructureByStockCodeDesc(String stockCode) {
		 String key = stockCode;
		    RMapCache<String, ShareholderStructure> mapCache = redissonClient.getMapCache("shareholderStructures");
		    
		    // 尝试从缓存获取数据
		    ShareholderStructure cachedData = mapCache.get(key);
		    if (cachedData != null) {
		        // 缓存中有数据，直接返回
		        return Collections.singletonList(cachedData);
		    } else {
		        // 缓存中没有数据，从数据库获取
		        List<ShareholderStructure> dbData = shareholderStructureRepository.getShareholderStructureByStockCodeDesc(stockCode);
		        // 将从数据库获取的数据存入缓存
		        dbData.forEach(ss -> mapCache.put(stockCode + ":" + ss.getWeekOfYear(), ss, 24, TimeUnit.HOURS));
		        return dbData;
		    }
	}

}
