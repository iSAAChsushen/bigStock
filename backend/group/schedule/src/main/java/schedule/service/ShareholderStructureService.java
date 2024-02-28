package schedule.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import entity.ShareholderStructure;
import lombok.RequiredArgsConstructor;
import repository.ShareholderStructureRepository;

@Service
@RequiredArgsConstructor
public class ShareholderStructureService {
	private final ShareholderStructureRepository shareholderStructureRepository;
	
	public List<ShareholderStructure> getAll(){
		return shareholderStructureRepository.findAll();
	}
	
	public Optional<ShareholderStructure> getById(String id){
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
	
	public List<ShareholderStructure> getShareholderStructureByStockCodeDesc(String stockCode){
		return shareholderStructureRepository.getShareholderStructureByStockCodeDesc(stockCode);
	}
	
}
