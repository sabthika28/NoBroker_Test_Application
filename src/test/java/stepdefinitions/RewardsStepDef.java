package stepdefinitions;

import io.cucumber.java.en.*;
import org.testng.Assert;
import pages.RewardsPage;

public class RewardsStepDef {

    private RewardsPage rewardsPage() { return new RewardsPage(Hooks.driver); }

    @When("the user clicks on the {string} icon")
    public void the_user_clicks_on_the_icon(String iconName) {
        if (!"Rewards".equalsIgnoreCase(iconName)) {
            throw new IllegalArgumentException("Unsupported icon in this step: " + iconName);
        }
        rewardsPage().clickRewardIcon();
    }

    @Then("the Rewards page should be displayed")
    public void the_rewards_page_should_be_displayed() {
        boolean ok = rewardsPage().isRewardsPageDisplayed();
        Assert.assertTrue(ok, "Rewards page not displayed");
    }

    @Then("the user scrolls through the Rewards page")
    public void the_user_scrolls_through_the_rewards_page() {
        rewardsPage().scrollThroughRewards();
    }
}
