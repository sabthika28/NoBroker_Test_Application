package stepdefinitions;

import org.openqa.selenium.WebDriver;
import org.testng.Assert;

import com.aventstack.extentreports.ExtentTest;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import pages.LoginPage;
import utils.Base;

public class LoginStepDef {

    WebDriver driver = Hooks.driver;
    ExtentTest extTest = Hooks.extTest;

    LoginPage loginPage;

    @Given("the user is on the login page")
    public void the_user_is_on_the_login_page() {
        String baseUrl = "https://www.nobroker.in/";
        String currentUrl = driver.getCurrentUrl();
        boolean ok = currentUrl != null && currentUrl.startsWith(baseUrl);
        Assert.assertTrue(ok, "Expected URL to start with '" + baseUrl + "' but was: " + currentUrl);
    }

    // scenario--1
    @When("the user enters a invalid phone number")
    public void the_user_enters_a_invalid_phone_number() {
        loginPage = new LoginPage(driver, extTest);
        loginPage.clickLogin();
        loginPage.enterMobileNumber("5763394");
        loginPage.clickContinue();
    }

    @Then("an invalid number message should be shown")
    public void an_invalid_number_message_should_be_shown() {
        boolean actualError = loginPage.getNumErrorMessage();
        Assert.assertTrue(actualError);
    }

    // scenario--2
    @When("the user enters a valid phone number")
    public void the_user_enters_a_valid_phone_number() {
        // defensive init
        if (loginPage == null) loginPage = new LoginPage(driver, extTest);
        loginPage.clickLogin();
        loginPage.enterMobileNumber("7094080503");
        // request OTP
        loginPage.clickContinue();
    }

    @When("the user enters the invalid OTP")
    public void the_user_enters_the_invalid_otp() {
        if (loginPage == null) loginPage = new LoginPage(driver, extTest);
        loginPage.enterOtp("573852");
        loginPage.clickContinue();
    }

    @Then("an invalid otp message should be shown")
    public void an_invalid_otp_message_should_be_shown() {
        boolean actualError = loginPage.getOtpErrorMessage();
        Assert.assertTrue(actualError);
    }

    // OTP timeout steps (added/fixed)
    @When("the user requests an OTP")
    public void the_user_requests_an_otp() {
        if (loginPage == null) loginPage = new LoginPage(driver, extTest);
        // Assumes phone number is already entered; clicking Continue requests OTP
        loginPage.clickContinue();
    }

    @When("waits until the OTP expires")
    public void waits_until_the_otp_expires() {
        if (loginPage == null) loginPage = new LoginPage(driver, extTest);
        // Wait until the UI indicates OTP expiry (or resend enabled)
        loginPage.waitUntilOtpExpires();
    }

    @Then("an otp expired message should be shown")
    public void an_otp_expired_message_should_be_shown() {
        if (loginPage == null) loginPage = new LoginPage(driver, extTest);
        boolean expiredVisible = loginPage.isOtpExpiredMessageDisplayed();
        Assert.assertTrue(expiredVisible, "OTP expired message was not displayed");
    }

    // scenario--3: wait for expiry then resend
    @When("waits until the OTP expires and clicks on resend button")
    public void waits_until_the_otp_expires_and_clicks_on_resend_button() {
        if (loginPage == null) loginPage = new LoginPage(driver, extTest);
        // wait until UI shows OTP expired (or resend becomes available)
        loginPage.waitUntilOtpExpires();
        // then click resend
        loginPage.clickResendOtp();
    }

    @When("the user enters the valid OTP")
    public void the_user_enters_the_valid_otp() {
        if (loginPage == null) loginPage = new LoginPage(driver, extTest);
        loginPage.enterOtpManually(driver);
        Base.sleep();
        loginPage.clickContinue();
        Base.sleep();
    }

    // scenario--4
    @Then("the user should be logged in successfully")
    public void the_user_should_be_logged_in_successfully() {
        if (loginPage == null) loginPage = new LoginPage(driver, extTest);
        boolean actualError = loginPage.loginsuccessful();
        Assert.assertTrue(actualError);
    }
}
