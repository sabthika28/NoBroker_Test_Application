package stepdefinitions;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;

import io.cucumber.java.*;
import utils.Base;
import org.openqa.selenium.WebDriver;

public class Hooks extends Base {
	
	static ExtentSparkReporter spark;
    static ExtentReports extReports;
    public static ExtentTest extTest;
    public static WebDriver driver; // accessible for step definitions

    @BeforeAll
    public static void beforeAll() {
        spark = new ExtentSparkReporter("report/ExtentReport.html");
        extReports = new ExtentReports();
        extReports.attachReporter(spark);
    }

    @AfterAll
    public static void afterAll() {
        if (driver != null) {
            driver.quit(); // quit once after all scenarios
        }
        extReports.flush();
    }

    @Before
    public void setup(Scenario scenario) {
        if (driver == null) {  // only launch once
            launchBrowser();
            driver = Base.driver;
        }
        extTest = extReports.createTest(scenario.getName());
        extTest.info("Scenario started: " + scenario.getName());
    }

    @After
    public void tearDown(Scenario scenario) {
        if (scenario.isFailed()) {
            extTest.fail("Scenario FAILED: " + scenario.getName());
        } else {
            extTest.pass("Scenario PASSED: " + scenario.getName());
        }
        Base.sleep();
        // ❌ do not quit driver here
    }
    
}
