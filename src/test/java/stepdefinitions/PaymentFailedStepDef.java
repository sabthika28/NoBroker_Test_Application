package stepdefinitions;

import io.cucumber.java.en.*;
import pages.PaymentFailedPage;
import stepdefinitions.Hooks; // ✅ corrected import, your Hooks is usually in hooks package
import objectrepository.PaymentFailedLocator;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import java.time.Duration;

public class PaymentFailedStepDef {

    private PaymentFailedPage paymentPage() {
        return new PaymentFailedPage(Hooks.driver);
    }

    @When("the user clicks on the top-right Menu")
    public void the_user_clicks_on_top_right_menu() {
        paymentPage().clickMenu();
    }

    @When("the user clicks on Seller Plans")
    public void the_user_clicks_on_seller_plans() {
        paymentPage().clickSellerPlansFromMenu();
    }

    @Then("the Seller Plans page should be displayed")
    public void the_seller_plans_page_should_be_displayed() {
        WebDriverWait wait = new WebDriverWait(Hooks.driver, Duration.ofSeconds(15));
        boolean ok = wait.until(d -> paymentPage().isSellerPlanPageDisplayed());
        Assert.assertTrue(ok, "Seller Plans page not displayed");
    }

    @When("the user clicks on Subscribe on Seller Plans page")
    public void the_user_clicks_subscribe_on_seller_plans_page() {
        paymentPage().clickSubscribeOnSellerPlan();
    }

    // ✅ Renamed step to avoid duplicate with PlansStepDef
    @Then("the Seller Plans payment page should be displayed")
    public void the_seller_plans_payment_page_should_be_displayed() {
        WebDriverWait wait = new WebDriverWait(Hooks.driver, Duration.ofSeconds(20));
        boolean ok = wait.until(d -> paymentPage().isPaymentPageDisplayed());
        Assert.assertTrue(ok, "Seller Plans payment page not displayed");
    }

    @When("the user clicks on Back from payment page")
    public void the_user_clicks_on_back_from_payment_page() {
        paymentPage().clickBackFromPaymentPage();
    }

    @Then("the Cancel Payment option should be displayed")
    public void the_cancel_payment_option_should_be_displayed() {
        WebDriverWait wait = new WebDriverWait(Hooks.driver, Duration.ofSeconds(10));
        boolean visible = wait.until(d -> !d.findElements(PaymentFailedLocator.CANCEL_PAYMENT_MODAL).isEmpty());
        Assert.assertTrue(visible, "Cancel Payment modal not displayed");
    }

    @When("the user clicks on Yes to cancel payment")
    public void the_user_clicks_on_yes_to_cancel_payment() {
        paymentPage().confirmCancelPaymentYes();
    }

    @Then("the payment failed message should be displayed on Seller Plans page")
    public void the_payment_failed_message_should_be_displayed_on_seller_plans_page() {
        WebDriverWait wait = new WebDriverWait(Hooks.driver, Duration.ofSeconds(12));
        boolean ok = wait.until(d -> paymentPage().isPaymentErrorDisplayedOnSellerPlans());
        Assert.assertTrue(ok, "Payment failure message not displayed on Seller Plans page");
    }
}
