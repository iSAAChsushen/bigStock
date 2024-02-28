package schedule.infra;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class ChromeDriverConfig {
	
	@Value("${custom.chromeDriverPath}")
	private String chromeDriverPath;

	@PostConstruct
	public void init() {
		System.setProperty("webdriver.chrome.driver", chromeDriverPath);
	}
}
