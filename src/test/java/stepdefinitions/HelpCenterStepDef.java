package stepdefinitions;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;
import io.cucumber.java.en.When;
import objectrepository.HelpCenterLocator;  // Import locators
import utils.Base;

import java.time.Duration;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class HelpCenterStepDef {

    WebDriver driver;

    public HelpCenterStepDef() {
        this.driver = Hooks.driver; // Assuming you are using Hooks to initialize driver
    }

    @When("User clicks on Help Center button")
    public void user_clicks_on_help_center_button() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement helpCenterBtn = wait.until(ExpectedConditions.elementToBeClickable(
        		HelpCenterLocator.HELP_CENTER_BTN
        ));
        helpCenterBtn.click();
        Base.sleep();
    }

    @And("User selects {string} service")
    public void user_selects_service(String serviceName) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement serviceOption = wait.until(ExpectedConditions.elementToBeClickable(
        		HelpCenterLocator.SERVICE_OPTION
        ));
        serviceOption.click();
        Base.sleep();
    }

    @And("User selects complaint issue {string}")
    public void user_selects_complaint_issue(String issueName) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement issueOption = wait.until(ExpectedConditions.elementToBeClickable(
        		HelpCenterLocator.ISSUE_OPTION
        ));
        issueOption.click();
        Base.sleep();
    }

    @Then("User closes the Help Center panel")
    public void user_closes_the_help_center_panel() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement closeBtn = wait.until(ExpectedConditions.elementToBeClickable(
        		HelpCenterLocator.CLOSE_BTN
        ));
        closeBtn.click();
        Base.sleep();
    }
}

