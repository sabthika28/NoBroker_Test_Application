package stepdefinitions;

import io.cucumber.java.en.*;
import org.testng.Assert;
import pages.LogoutPage;

public class LogoutStepDef {

    private LogoutPage logoutPage() {
        return new LogoutPage(Hooks.driver);
    }

    @When("the user clicks on the top-right \"Profile\" icon")
    public void the_user_clicks_on_the_top_right_icon() {
        logoutPage().clickProfileIcon();
    }

    @When("the user clicks on \"Sign Out\" from the dropdown")
    public void the_user_clicks_on_from_the_dropdown() {
        logoutPage().clickSignOut();
    }

    @Then("the login menu should be displayed")
    public void the_login_menu_should_be_displayed() {
        boolean ok = logoutPage().isLoginPageDisplayed();
        Assert.assertTrue(ok, "Login page/menu was not displayed after signing out");
    }
}
