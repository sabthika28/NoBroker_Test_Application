package objectrepository;

import org.openqa.selenium.By;

public class HistoryLocator {

    // Container that often shows "Continue Last Search" or similar text
    public static final By HISTORY_CONTAINER_TEXT = By.xpath(
        "//*[contains(normalize-space(.),'Continue Last Search') or " +
        "contains(normalize-space(.),'Recent Searches') or " +
        "contains(normalize-space(.),'Recent search')]"
    );

    // Exact XPath for one history item (your earlier path)
    public static final By HISTORY_ITEM_EXACT = By.xpath(
        "//*[@id='app']/div/div/div[2]/a/div[1]/div[2]/div[2]/div"
    );

    // Candidate selectors for history items (ordered by preference)
    public static final By SUGGESTION_ROLE_OPTION = By.cssSelector("[role='option']");
    public static final By SUGGESTION_SITE_ITEM = By.cssSelector(
        ".rc-list-item, .nb__listItem, .suggestion-item, .nb__historyItem"
    );
    public static final By SUGGESTION_GENERIC_LI = By.xpath("//ul//li");
    public static final By SUGGESTION_DIV_AFTER_HISTORY_TEXT = By.xpath(
        "//*[contains(normalize-space(.),'Continue Last Search')]/following::div[1]"
    );

    // Ordered array (try exact first, then fallbacks)
    public static final By[] HISTORY_ITEM_LOCATORS = new By[] {
        HISTORY_ITEM_EXACT,
        SUGGESTION_ROLE_OPTION,
        SUGGESTION_SITE_ITEM,
        SUGGESTION_DIV_AFTER_HISTORY_TEXT,
        SUGGESTION_GENERIC_LI
    };

    // Locality input on home page
    public static final By LOCALITY_INPUT = By.id("listPageSearchLocality");
}
