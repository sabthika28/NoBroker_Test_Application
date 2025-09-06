package stepdefinitions;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;
import pages.ProfilePage;

import java.util.List;

public class ProfileStepDef {

    ProfilePage profilePage;

    @When("the user opens their profile from the username dropdown")
    public void the_user_opens_profile_from_dropdown() {
        profilePage = new ProfilePage(Hooks.driver, Hooks.extTest);
        profilePage.openProfileFromMenu();
    }

    @When("the user navigates through each left-side profile heading")
    public void the_user_navigates_through_each_left_side_profile_heading() {
        if (profilePage == null) profilePage = new ProfilePage(Hooks.driver, Hooks.extTest);
        List<String> items = profilePage.getLeftMenuItemsText();
        Hooks.extTest.info("Profile left nav items: " + items);
        for (String it : items) {
            boolean ok = profilePage.navigateToLeftMenuItem(it);
            Hooks.extTest.info("Navigate to '" + it + "' => " + ok);
        }
    }

    @Then("all left-side profile sections should be reachable")
    public void all_left_side_profile_sections_should_be_reachable() {
        if (profilePage == null) profilePage = new ProfilePage(Hooks.driver, Hooks.extTest);
        List<String> items = profilePage.getLeftMenuItemsText();
        boolean allOk = true;
        for (String it : items) {
            boolean ok = profilePage.navigateToLeftMenuItem(it);
            if (!ok) allOk = false;
        }
        Assert.assertTrue(allOk, "One or more left-side profile sections failed to load.");
    }
}
