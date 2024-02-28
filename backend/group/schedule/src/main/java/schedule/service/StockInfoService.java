package schedule.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import repository.StockInfoRepository;

@Service
@RequiredArgsConstructor
public class StockInfoService {
	private final StockInfoRepository stockInfoRepository;

	public List<String> getAllStockCode() {
		return stockInfoRepository.getAllStockCode();
	}
}
