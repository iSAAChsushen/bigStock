package com.bigstock.schedule.utils;

import java.io.File;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.IsoFields;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

public class ChromeDriverUtils {

	private static final Map<Integer, String> COLUMN_NAME = new HashMap<>();

	static {
		initializeColumnNames();
	}

	public static List<Map<String, String>> grepStockInfo(String chromeDriverPath, String listedCompanyUrl,
			String overTheCounterUrl) throws InterruptedException {
		ChromeDriverService service = new ChromeDriverService.Builder()
				.usingDriverExecutable(new File(chromeDriverPath)).usingAnyFreePort().build();

		WebDriver driver = new ChromeDriver(service);
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
		try {
			driver.get(listedCompanyUrl);
			WebElement searchButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("search")));
			searchButton.click();
			Thread.sleep(1500);
			WebElement selectElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("select")));

			// 使用 Select 类来操作下拉菜单
			Select select = new Select(selectElement);
			// 选择“全部”选项
			select.selectByValue("-1");
			Thread.sleep(4000);
			// 找到 TBODY 元素
			WebElement tbodyElement = wait
					.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".rwd-table tbody")));

			// 找到 TBODY 下的所有 TR 元素
			List<WebElement> trElements = tbodyElement.findElements(By.tagName("tr"));

			// 遍历每个 TR 元素
			for (WebElement trElement : trElements) {
				// 找到 TR 元素下的所有 TD 元素
				List<WebElement> tdElements = trElement.findElements(By.tagName("td"));

				// 遍历每个 TD 元素并输出其文本内容
				for (WebElement tdElement : tdElements) {
					System.out.print(tdElement.getText() + "\t");
				}
				System.out.println(); // 换行
			}
		} finally {
			driver.quit();
		}
		return null;

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

			for (String tdccSelectoption : tdccSelectoptions) {
				LocalDate selectDate = LocalDate.parse(tdccSelectoption, lastestDateformatter);
				LocalDate latestCountDate = LocalDate.parse(latestCountDateStr, lastestDateformatter);

				if (selectDate.compareTo(latestCountDate) < 0) {
					continue;
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
				int grabSize = 15;
				Map<Integer, String> week = new HashMap<>();
				List<WebElement> rows = tbodyElement.findElements(By.tagName("tr"));

				for (int index = 0; index < grabSize; index++) {
					WebElement row = rows.get(index);
					List<WebElement> cells = row.findElements(By.tagName("td"));
					week.put(index + 6, cells.get(2).getText());
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
					LocalDate date = LocalDate.parse(dateString.replace("/", "-"));
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

				COLUMN_NAME.entrySet().stream().forEach(entry -> {
					if (!week.containsKey(entry.getKey())) {
						week.put(entry.getKey(), "-");
					}
				});

				weekInfo.add(week);
				Thread.sleep(500);
			}
		} finally {
			driver.quit();
		}

		return weekInfo;
	}

	private static void initializeColumnNames() {
		COLUMN_NAME.put(0, "周別");
		COLUMN_NAME.put(1, "統計日期");
		COLUMN_NAME.put(2, "收盤");
		COLUMN_NAME.put(3, "漲跌(元)");
		COLUMN_NAME.put(4, "漲跌(%)");
		COLUMN_NAME.put(5, "集保庫存(萬張)");
		COLUMN_NAME.put(6, "<1張");
		COLUMN_NAME.put(7, "≧1張≦5張");
		COLUMN_NAME.put(8, ">5張≦10張");
		COLUMN_NAME.put(9, ">10張≦15張");
		COLUMN_NAME.put(10, ">15張≦20張");
		COLUMN_NAME.put(11, ">20張≦30張");
		COLUMN_NAME.put(12, ">30張≦40張");
		COLUMN_NAME.put(13, ">40張≦50張");
		COLUMN_NAME.put(14, ">50張≦100張");
		COLUMN_NAME.put(15, ">100張≦200張");
		COLUMN_NAME.put(16, ">200張≦400張");
		COLUMN_NAME.put(17, ">400張≦600張");
		COLUMN_NAME.put(18, ">600張≦800張");
		COLUMN_NAME.put(19, ">800張≦1千張");
		COLUMN_NAME.put(20, ">1千張");
	}
}
