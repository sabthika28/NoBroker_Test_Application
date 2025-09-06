package stepdefinitions;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;
import org.openqa.selenium.WebDriver;
import pages.PaymentsPage;

public class PaymentsStepDef {
    private final WebDriver driver;
    private final PaymentsPage paymentsPage;

    public PaymentsStepDef() {
        this.driver = Hooks.driver;  // ✅ use the static driver directly
        this.paymentsPage = new PaymentsPage(driver);
    }

    // ✅ Replaced generic click with two specific steps
    @When("the user clicks on Pay Rent")
    public void the_user_clicks_on_pay_rent() {
        paymentsPage.clickPayRent();
    }

    @When("the user clicks on My Payments")
    public void the_user_clicks_on_my_payments() {
        paymentsPage.clickMyPayments();
    }

    // ✅ Replaced generic page assertion with two specific steps
    @Then("the Payments via Credit Card page should be displayed")
    public void the_payments_via_credit_card_page_should_be_displayed() {
        Assert.assertTrue(paymentsPage.isPaymentsPageDisplayed(),
                "Payments via Credit Card page not displayed");
    }

    @Then("the Payments History page should be displayed")
    public void the_payments_history_page_should_be_displayed() {
        Assert.assertTrue(paymentsPage.isPaymentsHistoryPageDisplayed(),
                "Payments History page not displayed");
    }

    @Then("the message {string} should be displayed")
    public void the_message_should_be_displayed(String expectedMsg) {
        if (expectedMsg.toLowerCase().contains("no transactions")) {
            Assert.assertTrue(paymentsPage.isNoTransactionsMessageDisplayed(),
                    "No transactions message not displayed");
        } else {
            Assert.assertTrue(driver.getPageSource().toLowerCase().contains(expectedMsg.toLowerCase()),
                    "Expected message not found in page source");
        }
    }
}
