package stepdefinitions;

import io.cucumber.java.en.*;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import pages.RentalAgreementPage;

import java.time.Duration;

public class RentalAgreementStepDef {

    private RentalAgreementPage rentalPage() {
        return new RentalAgreementPage(Hooks.driver);
    }

    // Background steps
    @When("the user clicks on the top-right \"Menu\"")
    public void the_user_clicks_on_menu() {
        rentalPage().clickMenu();
    }

    @When("the user clicks on \"Rental Agreement\"")
    public void the_user_clicks_on_rental_agreement() {
        rentalPage().clickRentalAgreement();
    }

    @Then("the Rental Agreement page should be displayed")
    public void rental_agreement_page_should_be_displayed() {
        WebDriverWait wait = new WebDriverWait(Hooks.driver, Duration.ofSeconds(15));
        boolean ok = wait.until(d -> rentalPage().isRentalAgreementPageDisplayed());
        Assert.assertTrue(ok, "Rental Agreement page not displayed");
    }

    // Scenario steps
    @When("the user clicks on \"Renew Your Agreement\"")
    public void the_user_clicks_on_renew() {
        rentalPage().clickRenewYourAgreement();
    }

    @Then("the Renewal page should be displayed")
    public void renewal_page_should_be_displayed() {
        WebDriverWait wait = new WebDriverWait(Hooks.driver, Duration.ofSeconds(20));
        boolean ok = wait.until(d -> rentalPage().isRenewalPageDisplayed());
        Assert.assertTrue(ok, "Renewal page not displayed");
    }

    @When("the user clicks on \"Upload your Draft\"")
    public void the_user_clicks_on_upload() {
        rentalPage().clickUploadYourDraft();
    }

    @Then("the Upload Draft page should be displayed")
    public void upload_page_should_be_displayed() {
        WebDriverWait wait = new WebDriverWait(Hooks.driver, Duration.ofSeconds(15));
        boolean ok = wait.until(d -> rentalPage().isUploadPageDisplayed());
        Assert.assertTrue(ok, "Upload Draft page not displayed");
    }

    @When("the user clicks on \"E-Stamped Agreement\"")
    public void the_user_clicks_on_estamped() {
        rentalPage().clickEStampedAgreement();
    }

    @Then("the E-Stamped Agreement form page should be displayed")
    public void estamp_page_should_be_displayed() {
        WebDriverWait wait = new WebDriverWait(Hooks.driver, Duration.ofSeconds(15));
        boolean ok = wait.until(d -> rentalPage().isEStampedPageDisplayed());
        Assert.assertTrue(ok, "E-Stamped page not displayed");
    }

    @When("the user clicks on \"Paperless Rental Agreement with Aadhaar E-Sign\"")
    public void the_user_clicks_on_aadhaar() {
        rentalPage().clickAadhaarEsignAgreement();
    }

    @Then("the Aadhaar E-Sign Agreement page should be displayed")
    public void aadhaar_page_should_be_displayed() {
        WebDriverWait wait = new WebDriverWait(Hooks.driver, Duration.ofSeconds(15));
        boolean ok = wait.until(d -> rentalPage().isAadhaarEsignPageDisplayed());
        Assert.assertTrue(ok, "Aadhaar E-Sign page not displayed");
    }

    // Your Ongoing Agreements
    @When("the user clicks on \"Your Ongoing Agreements\"")
    public void the_user_clicks_on_your_ongoing_agreements() {
        rentalPage().clickYourOngoingAgreements();
    }

    @Then("the Ongoing Agreements page should be displayed")
    public void the_ongoing_agreements_page_should_be_displayed() {
        WebDriverWait wait = new WebDriverWait(Hooks.driver, Duration.ofSeconds(15));
        boolean ok = wait.until(d -> rentalPage().isOngoingAgreementsPageDisplayed());
        Assert.assertTrue(ok, "Ongoing Agreements page not displayed");
    }
}