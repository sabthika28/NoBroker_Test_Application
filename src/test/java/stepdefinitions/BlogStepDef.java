package stepdefinitions;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;
import pages.BlogPage;

public class BlogStepDef {

    private BlogPage blogPage() {
        return new BlogPage(Hooks.driver);
    }

    @When("the user clicks on \"Blog\" in the menu")
    public void the_user_clicks_on_blog_in_the_menu() {
        blogPage().clickBlogFromMenu();
    }

    @Then("the Blog page should be displayed")
    public void the_blog_page_should_be_displayed() {
        Assert.assertTrue(blogPage().isBlogPageDisplayed(), "Blog page not displayed");
    }

    @When("the user clicks on \"Post Free Property Ad\" on Blog page")
    public void the_user_clicks_on_post_free_property_ad_on_blog_page() {
        blogPage().clickPostFreePropertyAd();
    }

    @Then("the Post Free Property Ad page should be displayed")
    public void the_post_free_property_ad_form_page_should_be_displayed() {
        Assert.assertTrue(blogPage().isPostAdPageDisplayed(), "Post Free Property Ad page not displayed");
    }

    @When("the user returns to home from Blog")
    public void the_user_returns_to_home_from_blog() {
        blogPage().returnHome();   // ✅ method name matches BlogPage
    }

    @Then("the home page should be displayed")
    public void the_home_page_should_be_displayed() {
        Assert.assertTrue(blogPage().isHomePageDisplayed(), "Home page not displayed after returning from Blog");
    }
}
