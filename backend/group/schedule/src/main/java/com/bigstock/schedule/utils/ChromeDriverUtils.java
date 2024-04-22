package com.bigstock.schedule.utils;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.IsoFields;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.bigstock.sharedComponent.entity.StockInfo;
import com.google.common.collect.Maps;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChromeDriverUtils {

	private static final Map<Integer, String> SHAREHOLDER_STRUCTURE_COLUMN_NAME = new HashMap<>();
	
	private static final Map<Integer, String> STOCK_DAY_PRICE_COLUMN_NAME = new HashMap<>();
	
	private static final List<String> TWSE_TYPE_LIST = Arrays.asList("01", "02", "03", "04", "05", "06", "07", "21",
			"22", "08", "09", "10", "11", "12", "13", "24", "25", "26", "27", "28", "29", "30", "31", "14", "15", "16",
			"17", "18", "9299", "23", "19", "20");

	static {
		initializeColumnNames();
	}

	public static List<StockInfo> grepStockInfo(String chromeDriverPath, String overTheCounterUrl)
			throws InterruptedException {
		ChromeDriverService service = new ChromeDriverService.Builder()
				.usingDriverExecutable(new File(chromeDriverPath)).usingAnyFreePort().build();
		List<StockInfo> stockInfos = Lists.newArrayList();
		WebDriver driver = new ChromeDriver(service);
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
		try {
			driver.get(overTheCounterUrl);

			Thread.sleep(4000);
			WebElement siiSelectElement = wait
					.until(ExpectedConditions.presenceOfElementLocated((By.cssSelector("tbody select[name='TYPEK']"))));

			// 使用 Select 类初始化
			Select siiTypekSelect = new Select(siiSelectElement);

			// 通过 value 属性设置选项值为 "otc"
			siiTypekSelect.selectByValue("sii");
			Thread.sleep(2000);
			WebElement siiCcodeSelectElement = wait
					.until(ExpectedConditions.presenceOfElementLocated((By.name("code"))));

			// 使用 Select 类初始化
			Select siiCodeSelect = new Select(siiCcodeSelectElement);

			// 通过可见文本选择空白选项
			siiCodeSelect.selectByVisibleText("");

			WebElement siiSearchButton = wait.until(
					ExpectedConditions.presenceOfElementLocated((By.cssSelector("div.search input[type='button']"))));
			siiSearchButton.click();

//			// 点击按钮
//			searchButton.click();
			wait.until(ExpectedConditions
					.presenceOfElementLocated((By.xpath("//th[@class='tblHead' and contains(text(), '產業類別')]"))));
			JavascriptExecutor siijs = (JavascriptExecutor) driver;
			siijs.executeScript("window.scrollTo(0, document.body.scrollHeight);");
			List<WebElement> siiEvenAndOldRows = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
					By.xpath("//tr[contains(@class, 'even') or contains(@class, 'odd')]")));
			for (WebElement webElement : siiEvenAndOldRows) {
				List<WebElement> cells = webElement.findElements(By.tagName("td"));
				StockInfo stockInfo = new StockInfo();
				stockInfo.setStockCode(cells.get(0).getText().trim());
				stockInfo.setStockName(cells.get(1).getText().trim());
				stockInfo.setStockType("1");
				stockInfos.add(stockInfo);
			}

			driver.get(overTheCounterUrl);
			Thread.sleep(4000);
			WebElement otcSelectElement = wait
					.until(ExpectedConditions.presenceOfElementLocated((By.cssSelector("tbody select[name='TYPEK']"))));

			// 使用 Select 类初始化
			Select otcTypekSelect = new Select(otcSelectElement);

			// 通过 value 属性设置选项值为 "otc"
			otcTypekSelect.selectByValue("otc");
			Thread.sleep(2000);
			WebElement otcCcodeSelectElement = wait
					.until(ExpectedConditions.presenceOfElementLocated((By.name("code"))));

			// 使用 Select 类初始化
			Select otcCodeSelect = new Select(otcCcodeSelectElement);

			// 通过可见文本选择空白选项
			otcCodeSelect.selectByVisibleText("");

			WebElement otcSearchButton = wait.until(
					ExpectedConditions.presenceOfElementLocated((By.cssSelector("div.search input[type='button']"))));
			otcSearchButton.click();

//			// 点击按钮
//			searchButton.click();
			wait.until(ExpectedConditions
					.presenceOfElementLocated((By.xpath("//th[@class='tblHead' and contains(text(), '產業類別')]"))));
			JavascriptExecutor js = (JavascriptExecutor) driver;
			js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
			List<WebElement> evenAndOldRows = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
					By.xpath("//tr[contains(@class, 'even') or contains(@class, 'odd')]")));
			for (WebElement webElement : evenAndOldRows) {
				List<WebElement> cells = webElement.findElements(By.tagName("td"));
				StockInfo stockInfo = new StockInfo();
				stockInfo.setStockCode(cells.get(0).getText().trim());
				stockInfo.setStockName(cells.get(1).getText().trim());
				stockInfo.setStockType("0");
				stockInfos.add(stockInfo);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			driver.quit();
		}
		return stockInfos;
	}

	public static List<Map<Integer, String>> graspShareholderStructure(String chromeDriverPath, String tdccQryStockUrl,
			String stockCode, String latestCountDateStr) throws InterruptedException {

		List<Map<Integer, String>> weekInfo = new ArrayList<>();

		ChromeDriverService service = new ChromeDriverService.Builder()
				.usingDriverExecutable(new File(chromeDriverPath)).usingAnyFreePort().build();

		WebDriver driver = new ChromeDriver(service);
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
		try {
			driver.get(tdccQryStockUrl);
			Select dates = new Select(wait.until(ExpectedConditions.presenceOfElementLocated(By.id("scaDate"))));
			List<String> tdccSelectoptions = dates.getOptions().stream().map(WebElement::getText).toList();
			DateTimeFormatter lastestDateformatter = DateTimeFormatter.ofPattern("yyyyMMdd");

			LocalDate latestCountDate = LocalDate.parse(latestCountDateStr, lastestDateformatter);
			for (String tdccSelectoption : tdccSelectoptions) {
				LocalDate selectDate = LocalDate.parse(tdccSelectoption, lastestDateformatter);

				if (selectDate.compareTo(latestCountDate) <= 0) {
					break;
				}

				driver.get("https://www.tdcc.com.tw/portal/zh/smWeb/qryStock");
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("scaDate")));
				Select innerDates = new Select(driver.findElement(By.id("scaDate")));
				Optional<WebElement> tdccSelectoptionOp = innerDates.getOptions().stream()
						.filter(webElement -> webElement.getText().contains(tdccSelectoption)).findFirst();
				tdccSelectoptionOp.get().click();
				LocalDate tdccSelectoptionLd = LocalDate.from(lastestDateformatter.parse(tdccSelectoption));
				int month = tdccSelectoptionLd.getMonthValue();
				WebElement stockInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("StockNo")));
				stockInput.clear();
				stockInput.sendKeys(stockCode);
				WebElement searchButton = wait.until(ExpectedConditions
						.presenceOfElementLocated(By.xpath("//input[@type='submit' and @value='查詢']")));
				searchButton.click();
				WebElement tbodyElement = wait.until(ExpectedConditions
						.visibilityOfElementLocated(By.cssSelector(".table-frame.securities-overview.m-t-20 tbody")));

				boolean isTextPresent = driver.getPageSource().contains("查無此資料");
				if (isTextPresent && !weekInfo.isEmpty()) {
					break;
				}
				int grabSize = 16;
				Map<Integer, String> week = new HashMap<>();
				List<WebElement> rows = tbodyElement.findElements(By.tagName("tr"));

				for (int index = 0; index < grabSize; index++) {
					WebElement row = rows.get(index);
					List<WebElement> cells = row.findElements(By.tagName("td"));
					week.put(index + 6, cells.get(3).getText());
				}

				Thread.sleep(400);
				driver.get("https://stock.wearn.com/cdata.asp");
				Thread.sleep(400);

				Select yearSelect = new Select(
						wait.until(ExpectedConditions.presenceOfElementLocated(By.name("year"))));
				LocalDate rocDate = tdccSelectoptionLd.minusYears(1911);
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyy/MM/dd");
				String rocDateString = rocDate.format(formatter);
				yearSelect.selectByValue(rocDateString.split("/")[0]);
				WebElement selectElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("month")));
				Select monthSelect = new Select(selectElement);
				monthSelect.selectByValue(StringUtils.leftPad(String.valueOf(month), 2, "0"));
				Thread.sleep(200);
				WebElement stockNoInput = wait.until(ExpectedConditions
						.presenceOfElementLocated(By.xpath("//input[@name='kind' and @maxlength='6' and @size='8']")));
				stockNoInput.sendKeys(stockCode);
				Thread.sleep(1000);
				WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(
						By.xpath("//input[@type='SUBMIT' and @value='搜尋' and contains(@class, 'id1')]")));
				element.click();

				WebElement tableElement = wait
						.until(ExpectedConditions.presenceOfElementLocated((By.cssSelector("table.mobile_img"))));
				WebElement pricetbodyElement = tableElement.findElement(By.xpath("./tbody"));
				Thread.sleep(500);
				List<WebElement> twseRows = pricetbodyElement.findElements(By.tagName("tr"));

				if (twseRows.isEmpty()) {
					continue;
				}

				List<WebElement> elements = driver
						.findElements(By.xpath("//td[@colspan='6' and contains(text(),'對不起您輸入的日期資料有錯誤喔。')]"));
				if (!elements.isEmpty()) {
					break;
				}

				Map<String, String> dateAndPrice = new HashMap<>();

				for (int index = 2; index < twseRows.size(); index++) {
					List<WebElement> cells = twseRows.get(index).findElements(By.tagName("td"));
					LocalDate date = LocalDate.parse(cells.get(0).getText(), formatter);
					dateAndPrice.put(date.format(formatter), cells.get(4).getText().trim());
				}

				LocalDate closestDate = null;
				long closestDuration = Long.MAX_VALUE;

				for (String dateString : dateAndPrice.keySet()) {
				       // 指定日期字符串格式
			        DateTimeFormatter dateStringformatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");


			        // 将民国日期转换为西元日期
			        String[] parts = dateString.split("/");
			        int year = Integer.parseInt(parts[0]) + 1911; // 民国转换为西元
			        String standardDateString = year + "/" + parts[1] + "/" + parts[2];

			        // 解析标准日期字符串为 LocalDate 对象
			        LocalDate date = LocalDate.parse(standardDateString, dateStringformatter);
					long duration = Math.abs(selectDate.until(date).getDays());

					if (duration < closestDuration) {
						closestDuration = duration;
						closestDate = date;
					}
				}

				String monthStr = StringUtils.leftPad(String.valueOf(tdccSelectoptionLd.getMonthValue()), 2, "0");
				String dateStr = StringUtils.leftPad(String.valueOf(tdccSelectoptionLd.getDayOfMonth()), 2, "0");
				int weekOfYear = closestDate.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
				String weekOfYearString = StringUtils.leftPad(String.valueOf(weekOfYear), 2, "0");
				int year = closestDate.getYear();
				DateTimeFormatter acformatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
				week.put(1, monthStr + "/" + dateStr);
				week.put(2, dateAndPrice.get(closestDate.format(acformatter)));
				week.put(0, year + "W" + weekOfYearString);

				SHAREHOLDER_STRUCTURE_COLUMN_NAME.entrySet().stream().forEach(entry -> {
					if (!week.containsKey(entry.getKey())) {
						week.put(entry.getKey(), "-");
					}
				});

				weekInfo.add(week);
				Thread.sleep(500);
			}
		} catch(Exception e) {
			log.error(e.getMessage(),e);
		}finally {
		
			driver.quit();
		}

		return weekInfo;
	}

	
	/**
	 * 抓上市上櫃的當日收盤、開盤、最高、最低價格
	 * @param chromeDriverPath
	 * @param stockTWSEPriceUrl
	 * @param stockTPEXPriceUrl
	 * @return
	 * @throws RestClientException
	 * @throws URISyntaxException
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static List<Map<String, String>> graspStockPrice(String chromeDriverPath, String stockTWSEPriceUrl,
			String stockTPEXPriceUrl, List<String> allDataBaseStockCode) throws RestClientException, URISyntaxException {
		List<Map<String, String>> stockCodePriceWithEveryWeekInfo = Lists.newArrayList();
		Date oridate = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(oridate);
		for(int minusday = 0 ; minusday < 1; minusday++) {
//			calendar.add(Calendar.DAY_OF_MONTH, -1);
			Date date = calendar.getTime();
			// 将java.util.Date转换为java.time.LocalDate
			LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			String ACDateString = new SimpleDateFormat("yyyyMMdd").format(date);
			for(String type : TWSE_TYPE_LIST) {
				
				MultiValueMap<String, Object> multipartMap = new LinkedMultiValueMap<>();
				multipartMap.add("response", "json");
				multipartMap.add("date", ACDateString);
				multipartMap.add("type", type);
				HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(multipartMap);
				
				RestTemplate teseRestTeplate = new RestTemplate();
				ResponseEntity<Map> responseEntity = teseRestTeplate.exchange(new URI(stockTWSEPriceUrl), HttpMethod.POST, request, Map.class);
				Map<String, Object> result = responseEntity.getBody();
				if (result.get("data1")!=null) {
					List<List<String>> thisDateStockCodePriceInfo = (List<List<String>>) result.get("data1");
					List<Map<String, String>> TWSEList = thisDateStockCodePriceInfo.stream().map(list -> {
						Map<String, String> stockPriceInfo = Maps.newHashMap();
						stockPriceInfo.put("stock_code", list.get(0));
						stockPriceInfo.put("trading_day", ACDateString);
						stockPriceInfo.put("opening_price", list.get(5));
						stockPriceInfo.put("closing_price", list.get(8));
						stockPriceInfo.put("high_price", list.get(6));
						stockPriceInfo.put("low_price", list.get(7));
						return stockPriceInfo;
					}).toList();
					stockCodePriceWithEveryWeekInfo.addAll(TWSEList);
				}
			}
			
			
			
			int yearAD = localDate.getYear();
			int month = localDate.getMonthValue();
			int day = localDate.getDayOfMonth();
			
			// 西元年份转换为民国年份（民国年份 = 西元年份 - 1911）
			int yearROC = yearAD - 1911;
			
			// 如果月份大于 9，则不补零，否则补零
			String formattedMonth = month > 9 ? String.valueOf(month) : String.format("%02d", month);
			String formattedDay = day > 9 ? String.valueOf(day) : String.format("%02d", day);
			String currentstockTPEXPriceUrl = stockTPEXPriceUrl.replace("{dateString}",
					String.join("/", String.valueOf(yearROC), formattedMonth, formattedDay));
			RestTemplate TPEXRestTeplate = new RestTemplate();
			ResponseEntity<Map> TPEXResponseEntity = TPEXRestTeplate
					.exchange(new URI(currentstockTPEXPriceUrl), HttpMethod.POST, null, Map.class);
			Map<String, Object> TPEXresult = TPEXResponseEntity.getBody();
			if (!((List) TPEXresult.get("aaData")).isEmpty()) {
				List<List<String>> thisDateStockCodePriceInfo = (List<List<String>>) TPEXresult.get("aaData");
				List<Map<String, String>> TPEXList = thisDateStockCodePriceInfo.stream().filter(list -> allDataBaseStockCode.contains(list.get(0))).map(list -> {
					Map<String, String> stockPriceInfo = Maps.newHashMap();
					stockPriceInfo.put("stock_code", list.get(0));
					stockPriceInfo.put("trading_day", ACDateString);
					stockPriceInfo.put("opening_price", list.get(4));
					stockPriceInfo.put("closing_price", list.get(2));
					stockPriceInfo.put("high_price", list.get(5));
					stockPriceInfo.put("low_price", list.get(6));
					return stockPriceInfo;
				}).toList();
				stockCodePriceWithEveryWeekInfo.addAll(TPEXList);
			}
			
		}
		return stockCodePriceWithEveryWeekInfo;
	}
	
	private static void initializeColumnNames() {
		SHAREHOLDER_STRUCTURE_COLUMN_NAME.put(0, "周別");
		SHAREHOLDER_STRUCTURE_COLUMN_NAME.put(1, "統計日期");
		SHAREHOLDER_STRUCTURE_COLUMN_NAME.put(2, "收盤");
		SHAREHOLDER_STRUCTURE_COLUMN_NAME.put(3, "漲跌(元)");
		SHAREHOLDER_STRUCTURE_COLUMN_NAME.put(4, "漲跌(%)");
		SHAREHOLDER_STRUCTURE_COLUMN_NAME.put(5, "集保庫存(萬張)");
		SHAREHOLDER_STRUCTURE_COLUMN_NAME.put(6, "<1張");
		SHAREHOLDER_STRUCTURE_COLUMN_NAME.put(7, "≧1張≦5張");
		SHAREHOLDER_STRUCTURE_COLUMN_NAME.put(8, ">5張≦10張");
		SHAREHOLDER_STRUCTURE_COLUMN_NAME.put(9, ">10張≦15張");
		SHAREHOLDER_STRUCTURE_COLUMN_NAME.put(10, ">15張≦20張");
		SHAREHOLDER_STRUCTURE_COLUMN_NAME.put(11, ">20張≦30張");
		SHAREHOLDER_STRUCTURE_COLUMN_NAME.put(12, ">30張≦40張");
		SHAREHOLDER_STRUCTURE_COLUMN_NAME.put(13, ">40張≦50張");
		SHAREHOLDER_STRUCTURE_COLUMN_NAME.put(14, ">50張≦100張");
		SHAREHOLDER_STRUCTURE_COLUMN_NAME.put(15, ">100張≦200張");
		SHAREHOLDER_STRUCTURE_COLUMN_NAME.put(16, ">200張≦400張");
		SHAREHOLDER_STRUCTURE_COLUMN_NAME.put(17, ">400張≦600張");
		SHAREHOLDER_STRUCTURE_COLUMN_NAME.put(18, ">600張≦800張");
		SHAREHOLDER_STRUCTURE_COLUMN_NAME.put(19, ">800張≦1千張");
		SHAREHOLDER_STRUCTURE_COLUMN_NAME.put(20, ">1千張");
		SHAREHOLDER_STRUCTURE_COLUMN_NAME.put(21, "總計");
		
		
		STOCK_DAY_PRICE_COLUMN_NAME.put(0, "stock_code");
		STOCK_DAY_PRICE_COLUMN_NAME.put(1, "trading_day");
		STOCK_DAY_PRICE_COLUMN_NAME.put(2, "opening_price");
		STOCK_DAY_PRICE_COLUMN_NAME.put(3, "closing_price");
		STOCK_DAY_PRICE_COLUMN_NAME.put(4, "high_price");
		STOCK_DAY_PRICE_COLUMN_NAME.put(5, "low_price");
		

		
	}
}
