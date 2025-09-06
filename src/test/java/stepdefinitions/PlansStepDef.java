package stepdefinitions;

import io.cucumber.java.en.*;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import pages.PlansPage;

import java.time.Duration;

public class PlansStepDef {

    private PlansPage plansPage() {
        return new PlansPage(Hooks.driver);
    }

    // existing explicit plan clicks...
    @When("the user clicks on \"Tenant Plans\"")
    public void the_user_clicks_on_tenant_plans() { plansPage().clickPlanFromMenu("Tenant Plans"); }

    @When("the user clicks on \"Buyer Plans\"")
    public void the_user_clicks_on_buyer_plans() { plansPage().clickPlanFromMenu("Buyer Plans"); }

    @When("the user clicks on \"Owner Plans\"")
    public void the_user_clicks_on_owner_plans() { plansPage().clickPlanFromMenu("Owner Plans"); }

    @When("the user clicks on \"Seller Plans\"")
    public void the_user_clicks_on_seller_plans() { plansPage().clickPlanFromMenu("Seller Plans"); }

    // Plan-specific assertion (keeps your explicit plan-check step)
    @Then("the {string} plan page should be displayed")
    public void the_plan_page_should_be_displayed(String planName) {
        WebDriverWait wait = new WebDriverWait(Hooks.driver, Duration.ofSeconds(15));
        boolean ok = wait.until(d -> plansPage().isPlanPageDisplayed(planName));
        Assert.assertTrue(ok, planName + " plan page not displayed");
    }

    /**
     * Dispatcher: keep backward compatibility with existing features that say:
     *   Then the "Tenant Plans" page should be displayed
     *
     * If the pageName looks like a Plans page (contains 'plan' or 'plans'), delegate to plan assertion.
     * Otherwise, instruct the test author to use a specific step (keeps behavior explicit).
     */
    @Then("the {string} page should be displayed")
    public void the_page_should_be_displayed(String pageName) {
        String lower = pageName == null ? "" : pageName.toLowerCase();
        if (lower.contains("plan")) {
            // delegate to the plan-specific assertion
            the_plan_page_should_be_displayed(pageName);
            return;
        }

        // Not a plan page — keep behavior explicit to avoid accidental false positives.
        // Informative failure to guide the test author to use page-specific step defs.
        throw new RuntimeException("No generic page-check exists for \"" + pageName
            + "\". Either add a page-specific step definition or change the feature to a supported step (e.g. 'the \""
            + pageName + "\" plan page should be displayed' if it's a plan).");
    }

    @When("the user clicks on \"Subscribe\" in {string} page")
    public void the_user_clicks_subscribe_in_plan_page(String planName) {
        plansPage().clickSubscribeOnPlanPage();
    }

    @Then("the payment page should be displayed")
    public void payment_page_should_be_displayed() {
        WebDriverWait wait = new WebDriverWait(Hooks.driver, Duration.ofSeconds(20));
        boolean ok = wait.until(d -> plansPage().isPaymentPageDisplayed());
        Assert.assertTrue(ok, "Payment page not displayed");
    }
}
