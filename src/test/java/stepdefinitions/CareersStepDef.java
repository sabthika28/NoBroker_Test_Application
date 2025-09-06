package stepdefinitions;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;
import pages.CareersPage;

public class CareersStepDef {

    CareersPage careersPage;

    @When("the user clicks on \"Careers\"")
    public void the_user_clicks_on_careers() {
        careersPage = new CareersPage(Hooks.driver, Hooks.extTest);
        careersPage.openMenu();    // open the top-right menu
        careersPage.clickCareers(); // click the Careers link
    }

    @Then("the Career page should be displayed")
    public void career_page_should_be_displayed() {
        boolean displayed = careersPage.isCareersPageDisplayed();
        Assert.assertTrue(displayed, "Careers page was not displayed.");
    }

    @When("the user scrolls down to view the Careers content")
    public void the_user_scrolls_down_to_view_the_careers_content() {
        if (careersPage == null) careersPage = new CareersPage(Hooks.driver, Hooks.extTest);
        careersPage.scrollToHowDidWeStart();
    }

    @Then("the How did we Start section should be visible")
    public void the_how_did_we_start_section_should_be_visible() {
        boolean visible = careersPage.isHowDidWeStartVisible();
        Assert.assertTrue(visible, "'How did we Start' section was not visible after scrolling.");
    }
}
