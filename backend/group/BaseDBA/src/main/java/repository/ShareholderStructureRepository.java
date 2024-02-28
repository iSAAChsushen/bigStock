package repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import entity.ShareholderStructure;

public interface ShareholderStructureRepository extends JpaRepository<ShareholderStructure, String> {

	@Query(value = "select s from ShareholderStructure s where s.stockCode = :stockCode order by s.countDate desc")
	public List<ShareholderStructure> getShareholderStructureByStockCodeDesc(@Param("stockCode") String stockCode);

}
