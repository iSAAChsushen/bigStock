package com.bigstock.sharedComponent.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RList;
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

	public List<String> getAllShareholderStructureStockCode() {
		return shareholderStructureRepository.getAllShareholderStructureStockCode();
	}
	
	public List<ShareholderStructure>  getShareholderStructureLastTwoWeeks(String firstWeekOfYear, String secondWeekOfYear,
			String thirdWeekOfYear){
		String key = firstWeekOfYear + secondWeekOfYear + thirdWeekOfYear;
		RMapCache<String, RList<ShareholderStructure>> outerMapCache = redissonClient
				.getMapCache("shareholderStructures-lastTwoWeeks");
		RList<ShareholderStructure> innerListCache = outerMapCache.get(key);
		if (innerListCache != null) {
			innerListCache.expire(Duration.ofDays(10));
			List<ShareholderStructure> ss = innerListCache.readAll();
			Collections.sort(ss, Comparator.comparing(ShareholderStructure::getId).reversed()); 
			return ss;
		} else {
			// 缓存中没有数据，从数据库获取
			List<ShareholderStructure> dbDatas = shareholderStructureRepository
					.getByOverFourHundreLotContinueIncrease(firstWeekOfYear, secondWeekOfYear, thirdWeekOfYear);

			// 构建内部的 Hash 表缓存
			innerListCache = redissonClient.getList(key);
			innerListCache.addAll(dbDatas);
			
			// 将内部的 Hash 表缓存存入外部的 Hash 表缓存
			outerMapCache.put(key, innerListCache, 10, TimeUnit.DAYS);

			return dbDatas;
		}
	}

	public List<ShareholderStructure> getShareholderStructureByStockCodeDesc(String stockCode) {
		String key = stockCode;
		RMapCache<String, RMapCache<String, ShareholderStructure>> outerMapCache = redissonClient
				.getMapCache("shareholderStructures");

		// 尝试从缓存获取数据
		RMapCache<String, ShareholderStructure> innerMapCache = outerMapCache.get(key);
		if (innerMapCache != null && !innerMapCache.readAllValues().isEmpty()) {
			innerMapCache.expire(Duration.ofDays(10));
			List<ShareholderStructure> ss = new ArrayList<>(innerMapCache.readAllValues());
			Collections.sort(ss, Comparator.comparing(ShareholderStructure::getWeekOfYear).reversed()); 
			return ss;
		} else {
			// 缓存中没有数据，从数据库获取
			List<ShareholderStructure> dbDatas = shareholderStructureRepository
					.getShareholderStructureByStockCodeDesc(stockCode);

			// 构建内部的 Hash 表缓存
			innerMapCache = redissonClient.getMapCache(stockCode);
			for (ShareholderStructure dbData : dbDatas) {
				innerMapCache.put(dbData.getWeekOfYear(), dbData, 2, TimeUnit.DAYS);
			}

			// 将内部的 Hash 表缓存存入外部的 Hash 表缓存
			outerMapCache.put(key, innerMapCache, 13, TimeUnit.DAYS);

			return dbDatas;
		}
	}
	
	public String getMaxWeekOfYear() {
		return shareholderStructureRepository.getMaxWeekOfYear();
	}

	public String getMaxWeekOfYearExcludeSpecificDate(List<String> weekOfYears) {
		return shareholderStructureRepository.getMaxWeekOfYearExcludeSpecificDate(weekOfYears);
	}
	
	public List<String> getAreadyFinshGrapsStockCode(String maxWeekOfYear){
		return shareholderStructureRepository.getAreadyFinshGrapsStockCode(maxWeekOfYear);
	}
}
