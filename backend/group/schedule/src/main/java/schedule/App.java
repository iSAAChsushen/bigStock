package schedule;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class App {
	public static void main(String[] args) {
		System.setProperty("webdriver.chrome.driver", "D:/sts/chromedriver-win64/chromedriver.exe");
		SpringApplication app = new SpringApplication(App.class);
		app.run(args);
	}

}
