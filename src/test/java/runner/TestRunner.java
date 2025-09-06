package runner;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

@CucumberOptions(
		features = "src/test/resources/features",
		glue = "stepdefinitions",        
		plugin = {
				"pretty",
				"html:target/cucumber-report/html-report.html",
				"json:target/cucumber-report/report.json"
		},
    monochrome = true
)
public class TestRunner extends AbstractTestNGCucumberTests {
    // nothing else needed
}
