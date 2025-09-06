package stepdefinitions;

import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import org.testng.Assert;
import pages.PropertySearch;

public class SearchStepDef {
    private PropertySearch propertySearch;

    // Lazy getter: always create page object with the current Hooks.driver
    private PropertySearch propertySearch() {
        if (propertySearch == null) {
            propertySearch = new PropertySearch(Hooks.driver);
        }
        return propertySearch;
    }

    @When("the user selects {string} from the city dropdown")
    public void user_selects_city(String city) {
        propertySearch().selectCity(city);
    }

    @When("the user enters {string} as the locality")
    public void user_enters_locality(String locality) {
        propertySearch().enterLocality(locality);
    }

    @When("the user clicks on the Search button")
    public void user_clicks_search() {
        propertySearch().clickSearch();
    }

    @Then("the property listing page should be displayed")
    public void listings_page_should_be_displayed() {
        boolean displayed = propertySearch().isListingPageDisplayed();
        Assert.assertTrue(displayed, "Listing page was not displayed");
    }
}