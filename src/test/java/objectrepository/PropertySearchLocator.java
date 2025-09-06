package objectrepository;

import org.openqa.selenium.By;

public class PropertySearchLocator {
    // City dropdown (homepage)
    public static final By CITY_DROPDOWN = By.xpath("//*[@id=\"searchCity\"]/div/div[1]");

    // Locality input on homepage
    public static final By LOCALITY_INPUT = By.id("listPageSearchLocality");

    // Homepage Search button
    public static final By SEARCH_BUTTON = By.xpath("//*[@id=\"app\"]/div/div/div[2]/div[4]/button");

    // Primary known listing container (may be site-specific)
    public static final By LISTING_CONTAINER = By.className("nb__3XH0x");

    // Fallback selectors for property result cards
    public static final By FALLBACK_CARD_SELECTORS = By.cssSelector(".srpCard, .nb__2sm4L, .card, .listingCard");

    // Multiple suggestion selectors to try (Google Places pac-item, ARIA roles, site-specific, generic)
    public static final By SUGGESTION_PAC_ITEM = By.cssSelector(".pac-item");
    public static final By SUGGESTION_ROLE_OPTION = By.cssSelector("[role='option']");
    public static final By SUGGESTION_LISTBOX_LI = By.cssSelector("[role='listbox'] li");
    public static final By SUGGESTION_SITE_ITEM = By.cssSelector(".rc-list-item, .nb__listItem, .suggestion-item");
    public static final By SUGGESTION_GENERIC_LI = By.xpath("//ul//li");

    // Array of suggestion locators (order matters — more specific first)
    public static final By[] SUGGESTION_LOCATORS = new By[] {
            SUGGESTION_PAC_ITEM,
            SUGGESTION_ROLE_OPTION,
            SUGGESTION_LISTBOX_LI,
            SUGGESTION_SITE_ITEM,
            SUGGESTION_GENERIC_LI
    };

    // Metro popup locators (broad, case-insensitive)
    public static final By SEARCH_ALONG_METRO_POPUP = By.xpath("//*[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'search along metro')]");
    public static final By SEARCH_ALONG_METRO_SKIP_BTN = By.xpath("//button[contains(normalize-space(.),'Skip') or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'skip')]");
    public static final By SEARCH_ALONG_METRO_CLOSE_BTN = By.cssSelector(".modal-close, .close, .close-btn, button[aria-label='Close']");

}
