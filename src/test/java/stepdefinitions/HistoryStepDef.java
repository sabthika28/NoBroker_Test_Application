package stepdefinitions;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import org.testng.Assert;
import org.openqa.selenium.WebDriver;

import pages.PropertySearch;
import pages.SearchHistory;
import utils.Base; // optional: if you use Base.sleep(); remove if not present

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class HistoryStepDef {

    private WebDriver driver = Hooks.driver;
    private PropertySearch propertySearch = new PropertySearch(driver);
    private SearchHistory searchHistory = new SearchHistory(driver);

    // store the actual suggestion text clicked in enterLocality
    private String selectedSuggestionText;

    // NOTE: Do not define the Given "the user is on the NoBroker home page" here
    // to avoid duplicate step definitions. That Given is in SearchStepDef.

    @When("the user performs a search for {string}")
    public void user_performs_search(String locality) {
        // ensure on home page
        propertySearch.openHomePage();

        // select city if needed (adjust or remove if not required)
        propertySearch.selectCity("Chennai");

        // enterLocality now returns the actual suggestion text clicked (or typed text as fallback)
        selectedSuggestionText = propertySearch.enterLocality(locality);

        // click search page
        propertySearch.clickSearch();
    }

    @When("the user returns to the home page")
    public void user_returns_home() {
        // go to home page explicitly and refresh to ensure history UI is rendered
        propertySearch.openHomePage();
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        driver.navigate().refresh();
        try { Thread.sleep(800); } catch (InterruptedException ignored) {}
    }

    @Then("the previous search {string} should appear in search history")
    public void previous_search_should_appear(String expected) {
        searchHistory = new SearchHistory(driver);

        // quick presence check for history section
        boolean visible = searchHistory.isHistorySectionVisible();
        Assert.assertTrue(visible, "Search history section was not visible on the homepage.");

        // get visible items (SearchHistory retries/refresh internally if needed)
        List<String> items = searchHistory.getVisibleHistoryItemsText();

        // debug output to console to help debugging
        System.out.println("[DEBUG] selectedSuggestionText: '" + selectedSuggestionText + "'");
        System.out.println("[DEBUG] expected (from feature): '" + expected + "'");
        System.out.println("[DEBUG] history items collected: " + items);
        for (String it : items) {
            System.out.println("[DEBUG]   ITEM RAW: '" + it + "' (len=" + it.length() + ")");
        }

        Assert.assertFalse(items.isEmpty(), "No history items found. Items: " + items);

        // Decide which text to match: prefer actual clicked suggestion if available
        String toMatch = (selectedSuggestionText != null && !selectedSuggestionText.isEmpty()) ? selectedSuggestionText : expected;

        boolean matched = false;
        for (String item : items) {
            if (tokenMatch(item, toMatch) || tokenMatch(toMatch, item)) {
                matched = true;
                break;
            }
        }

        Assert.assertTrue(matched, "Expected history item not found. Items: " + items + " ; Looking for: '" + toMatch + "'");
    }

    // Normalize string into alphanumeric tokens (lowercase), removing punctuation and collapsing spaces
    private List<String> tokens(String s) {
        if (s == null) return Collections.emptyList();
        // remove all non-alphanumeric characters except spaces, then collapse spaces
        String cleaned = s.replaceAll("[^\\p{Alnum}\\s]", " ").replaceAll("\\s+", " ").trim().toLowerCase();
        if (cleaned.isEmpty()) return Collections.emptyList();
        return Arrays.asList(cleaned.split(" "));
    }

    // Return true if every token in expected exists in candidate (order-agnostic)
    private boolean tokenMatch(String candidate, String expected) {
        List<String> ct = tokens(candidate);
        List<String> et = tokens(expected);
        if (et.isEmpty()) return false;
        for (String t : et) {
            if (!ct.contains(t)) return false;
        }
        return true;
    }
}
