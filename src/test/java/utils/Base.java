package utils;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;

import io.github.bonigarcia.wdm.WebDriverManager;

public class Base {
	
    static final int TIME = 2000;
    public static WebDriver driver;

    public void launchBrowser() {
        Properties prop = PropertyReader.readProperties();

        String browser = prop.getProperty("Browser", "chrome").toLowerCase();

        if (browser.equals("chrome")) {
            // Setup ChromeDriver automatically
            WebDriverManager.chromedriver().setup();

            ChromeOptions chromeOptions = new ChromeOptions();
            Map<String, Object> chromePrefs = new HashMap<>();
            chromePrefs.put("credentials_enable_service", false);
            chromePrefs.put("profile.password_manager_leak_detection", false);
            chromeOptions.setExperimentalOption("prefs", chromePrefs);

            driver = new ChromeDriver(chromeOptions);
            driver.manage().window().maximize();
        } 
        else if (browser.equals("firefox")) {
            // Setup GeckoDriver automatically
            WebDriverManager.firefoxdriver().setup();
            driver = new FirefoxDriver();
            driver.manage().window().maximize();
        }

        // Add implicit wait for stability
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));

        // Navigate to URL from properties
        driver.get(prop.getProperty("URL"));
    }

    public static void sleep() {
        try {
            Thread.sleep(TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
