package com.bigstock.biz.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bigstock.sharedComponent.entity.ShareholderStructure;
import com.bigstock.sharedComponent.service.ShareholderStructureService;
import com.google.common.collect.Lists;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class InitShareholderStructureService {
	
	@Autowired
	ShareholderStructureService shareholderStructureService;

	@PostConstruct
	public void initShareholderStructure() throws IOException {
		// 解压缩zip文件

		// 读取Excel文件
		File excelFile = new File(
				"D:\\bigStock\\backend\\group\\biz\\src\\main\\resources\\init\\ShareholderStructureInfo\\ShareholderStructureInfo\\upperStock");
		if (excelFile != null) {
				processExcelFile(excelFile);
		}
		log.info("現在處理到的資料完成");
	}



	private void processExcelFile(File excelFile) {
		List<String> allStockCode = shareholderStructureService.getAllShareholderStructureStockCode();
		Lists.newArrayList( excelFile.listFiles()).parallelStream().forEach(excels ->{
			String stockCode = "";
			String stockName ="";
			try (FileInputStream fis = new FileInputStream(excels); 
					Workbook workbook = new HSSFWorkbook(fis)) {
				Sheet sheet = workbook.getSheetAt(0); // Assuming first sheet
				Pattern pattern = Pattern.compile("-\\d+");
				String name = excels.getName();
				Matcher matcher = pattern.matcher(name);
				matcher.find();
				 stockCode =  matcher.group().substring(1).trim();
				  int dashIndex = excels.getName().lastIndexOf("-");
				  if(allStockCode.contains(stockCode)) {
					  return;
				  }
				 stockName = excels.getName().substring(0, dashIndex).trim();
				
				int rowIndex = 0;
				for (Row row : sheet) {
					if (rowIndex == 0) {
						// 跳过第一行，作为列名
						rowIndex++;
						continue;
					}

					ShareholderStructure shareholderStructure = new ShareholderStructure();
					for (int index = 0; index < row.getLastCellNum(); index++) {
						Cell cell = row.getCell(index);
						if (cell != null) {
							String cellValue = cell.getStringCellValue(); // 假设单元格的值是字符串类型
							cellValue = cellValue.equals("-")? "0" : cellValue;
							cellValue = StringUtils.isBlank(cellValue)? "0" : cellValue;
							switch (index) {
							case 0 -> shareholderStructure.setWeekOfYear(cellValue);
							case 1 -> shareholderStructure.setCountDate(cellValue);
							case 2 -> shareholderStructure.setClosingPrice(cellValue);
							case 3 -> shareholderStructure.setPriceChange(cellValue);
							case 4 -> shareholderStructure.setPriceChangePercent(cellValue);
							case 5 -> shareholderStructure.setTdccStock(cellValue);
							case 6 -> shareholderStructure.setLessThanOneBoardLot(cellValue);
							case 7 -> shareholderStructure.setBetweenOneAndFiveBoardLot(cellValue);
							case 8 -> shareholderStructure.setBetweenFiveAndTenBoardLot(cellValue);
							case 9 -> shareholderStructure.setBetweenTenAndFifteenBoardLot(cellValue);
							case 10 -> shareholderStructure.setBetweenFifteenAndTwentyBoardLot(cellValue);
							case 11 -> shareholderStructure.setBetweenTwentyAndThirtyBoardLot(cellValue);
							case 12 -> shareholderStructure.setBetweenThirtyAndFortyBoardLot(cellValue);
							case 13 -> shareholderStructure.setBetweenFortyAndFiftyBoardLot(cellValue);
							case 14 -> shareholderStructure.setBetweenFiftyAndOneHundredBoardLot(cellValue);
							case 15 ->
								shareholderStructure.setBetweenOneHundredAndTwoHundredBoardLot(cellValue);
							case 16 ->
								shareholderStructure.setBetweenTwoHundredAndFourHundredBoardLot(cellValue);
							case 17 ->
								shareholderStructure.setBetweenFourHundredAndSixHundredBoardLot(cellValue);
							case 18 ->
								shareholderStructure.setBetweenSixHundredAndEightHundredBoardLot(cellValue);
							case 19 -> shareholderStructure
									.setBetweenEightHundredAndOneThousandBoardLot(cellValue);
							case 20 -> shareholderStructure.setOverOneThousandBoardLot(cellValue);
							}
						}
					}
					rowIndex++;
					shareholderStructure.setStockCode(stockCode);
					shareholderStructure.setStockName(stockName);
					shareholderStructure.setId(stockCode+"-"+shareholderStructure.getWeekOfYear());
					shareholderStructureService.insert(shareholderStructure);
				}

				// Create and save Shareholder entity using property1, property2, and property3
				// shareHolderRepository.save(new Shareholder(property1, property2, property3));
			} catch (IOException e) {
				log.error(stockName + "-" + stockCode + " 檔案格式錯誤");
			}
		});
	
	}
}
