package com.bigstock.sharedComponent.service;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RList;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import com.bigstock.sharedComponent.entity.ShareholderStructure;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RedissonCacheService {

	private final RedissonClient redissonClient;
	public static final String SHAREHOLDER_STRUCTURE_RMAP_CACHE = "shareholderStructureRmapCache";
	public static final String SHAREHOLDER_STRUCTURE_RLIST = "shareholderStructureRList";
	public static final String LAST_TWO_WEEKS_INCREASE_RMAP_CACHE = "lastTwoWeeksIncreaseRmapCache";
	public static final String LAST_TWO_WEEKS_INCREASE_RLIST = "lastTwoWeeksIncreaseRList";

	public void saveShareholderStructureNormalRecord(String stockCode,
			List<ShareholderStructure> shareholderStructure) {
		saveRecord(SHAREHOLDER_STRUCTURE_RMAP_CACHE, SHAREHOLDER_STRUCTURE_RLIST, stockCode, shareholderStructure);
	}

	public List<ShareholderStructure> getShareholderStructureNormalRecord(String stockCode) {
		return getRecord(SHAREHOLDER_STRUCTURE_RMAP_CACHE, SHAREHOLDER_STRUCTURE_RLIST, stockCode);
	}

	public void saveShareholderStructureLastTwoWeeksIncreaseRecord(String key,
			List<ShareholderStructure> shareholderStructure) {
		saveRecord(LAST_TWO_WEEKS_INCREASE_RMAP_CACHE, LAST_TWO_WEEKS_INCREASE_RLIST, key, shareholderStructure);
	}

	public List<ShareholderStructure> getShareholderStructureLastTwoWeeksIncreaseRecord(String stockCode) {
		return getRecord(LAST_TWO_WEEKS_INCREASE_RMAP_CACHE, LAST_TWO_WEEKS_INCREASE_RLIST, stockCode);
	}

	private void saveRecord(String mapCacheName, String listNamePrefix, String stockCode,
			List<ShareholderStructure> shareholderStructure) {
		RMapCache<String, RList<ShareholderStructure>> outerMapCache = redissonClient.getMapCache(mapCacheName);

		// 获取或创建新的 RList
		RList<ShareholderStructure> shareholderStructureRlist = outerMapCache.get(stockCode);
		if (shareholderStructureRlist == null) {
			shareholderStructureRlist = redissonClient.getList(listNamePrefix + "_" + stockCode);
			// 将新的 RList 放入 outerMapCache 并设置过期时间
			outerMapCache.put(stockCode, shareholderStructureRlist, 24, TimeUnit.HOURS);
		}

		// 添加新的数据到 RList
		shareholderStructureRlist.addAll(shareholderStructure);

		// 更新 outerMapCache 的过期时间
		outerMapCache.put(stockCode, shareholderStructureRlist, 24, TimeUnit.HOURS);
	}

	private List<ShareholderStructure> getRecord(String mapCacheName, String listNamePrefix, String key) {
		RMapCache<String, RList<ShareholderStructure>> outerMapCache = redissonClient.getMapCache(mapCacheName);
		RList<ShareholderStructure> shareholderStructureRlist = outerMapCache.get(key);
		if (shareholderStructureRlist == null) {
			shareholderStructureRlist = redissonClient.getList(listNamePrefix + "_" + key);
		}
		outerMapCache.put(key, shareholderStructureRlist, 24, TimeUnit.HOURS);
		return shareholderStructureRlist.readAll();
	}
}
