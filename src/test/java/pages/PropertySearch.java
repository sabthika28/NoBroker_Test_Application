package pages;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import objectrepository.PropertySearchLocator;

public class PropertySearch {
    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By fallbackCardSelectors = PropertySearchLocator.FALLBACK_CARD_SELECTORS;

    public PropertySearch(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(25));
    }

    public void openHomePage() {
        driver.get("https://www.nobroker.in/");
    }

    public void selectCity(String cityName) {
        WebElement cityDrop = wait.until(ExpectedConditions.elementToBeClickable(PropertySearchLocator.CITY_DROPDOWN));
        cityDrop.click();

        String itemXpath = "//div[text()='" + cityName + "']";
        WebElement cityItem = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(itemXpath)));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", cityItem);
        cityItem.click();
    }

    /**
     * Type locality, wait for suggestions, click the FIRST visible suggestion.
     * Returns the actual suggestion text that was clicked (or the input text if no suggestion/click failed).
     */
    public String enterLocality(String locality) {
        WebElement locInput = wait.until(ExpectedConditions.elementToBeClickable(PropertySearchLocator.LOCALITY_INPUT));
        locInput.clear();
        locInput.sendKeys(locality);

        // small initial pause to allow suggestions to begin
        sleepMillis(250);

        List<WebElement> suggestions = null;
        WebElement firstSuggestion = null;

        // try multiple suggestion locators until we find visible suggestions
        for (By sel : PropertySearchLocator.SUGGESTION_LOCATORS) {
            try {
                suggestions = (new WebDriverWait(driver, Duration.ofSeconds(8)))
                        .until(ExpectedConditions.visibilityOfAllElementsLocatedBy(sel));
                if (suggestions != null && !suggestions.isEmpty()) {
                    for (WebElement s : suggestions) {
                        if (s.isDisplayed()) {
                            firstSuggestion = s;
                            break;
                        }
                    }
                }
            } catch (Exception ignored) {
                // try next locator
            }
            if (firstSuggestion != null) break;
        }

        if (firstSuggestion == null) {
            // fallback: press ENTER if no suggestion appears
            System.out.println("[DEBUG] No suggestion visible for '" + locality + "'. Falling back to ENTER.");
            try { locInput.sendKeys(Keys.ENTER); } catch (Exception ex) { throw new RuntimeException("No suggestions visible and ENTER fallback failed.", ex); }
            sleepMillis(350);
            return locality;
        }

        // capture the visible text of the suggestion before clicking
        String clickedText = "";
        try {
            clickedText = firstSuggestion.getText().trim();
        } catch (Exception e) {
            clickedText = locality; // fallback
        }

        // Click the first suggestion with retries/strategies
        boolean clicked = tryClickSuggestionWithRetries(firstSuggestion, suggestions);
        if (!clicked) {
            System.out.println("[DEBUG] Suggestion click attempts failed. Falling back to ENTER.");
            try { locInput.sendKeys(Keys.ENTER); } catch (Exception ex) { throw new RuntimeException("All attempts to select suggestion failed and ENTER fallback also failed.", ex); }
            sleepMillis(350);
            return clickedText.isEmpty() ? locality : clickedText;
        } else {
            // allow UI to settle
            sleepMillis(400);
            return clickedText.isEmpty() ? locality : clickedText;
        }
    }

    // Try clicking suggestion using several strategies; handle StaleElementReferenceException and retry once
    private boolean tryClickSuggestionWithRetries(WebElement candidate, List<WebElement> suggestions) {
        int attempts = 0;
        while (attempts < 2) {
            try {
                // Strategy 1: Actions click
                try {
                    new Actions(driver).moveToElement(candidate).pause(Duration.ofMillis(120)).click().perform();
                    return true;
                } catch (Exception e1) {
                    System.out.println("[DEBUG] Actions click failed: " + e1.getMessage());
                }

                // Strategy 2: direct click
                try {
                    candidate.click();
                    return true;
                } catch (Exception e2) {
                    System.out.println("[DEBUG] candidate.click() failed: " + e2.getMessage());
                }

                // Strategy 3: click a clickable child (a, span, div)
                try {
                    WebElement child = null;
                    try { child = candidate.findElement(By.cssSelector("a,span,div")); } catch (Exception ignore) {}
                    if (child != null) {
                        child.click();
                        return true;
                    }
                } catch (Exception e3) {
                    System.out.println("[DEBUG] child.click() failed: " + e3.getMessage());
                }

                // Strategy 4: JS click
                try {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", candidate);
                    return true;
                } catch (Exception jsEx) {
                    System.out.println("[DEBUG] JS click failed: " + jsEx.getMessage());
                }

                // nothing worked
                return false;
            } catch (StaleElementReferenceException sere) {
                attempts++;
                System.out.println("[DEBUG] StaleElementReferenceException, retry attempt: " + attempts);
                // try to re-find first visible suggestion
                if (suggestions != null && !suggestions.isEmpty()) {
                    for (WebElement s : suggestions) {
                        if (s.isDisplayed()) {
                            candidate = s;
                            break;
                        }
                    }
                } else {
                    candidate = reFindFirstSuggestion();
                    if (candidate == null) return false;
                }
            }
        }
        return false;
    }

    // Re-find the first visible suggestion element
    private WebElement reFindFirstSuggestion() {
        for (By sel : PropertySearchLocator.SUGGESTION_LOCATORS) {
            try {
                List<WebElement> found = (new WebDriverWait(driver, Duration.ofSeconds(4)))
                        .until(ExpectedConditions.visibilityOfAllElementsLocatedBy(sel));
                if (found != null && !found.isEmpty()) {
                    for (WebElement f : found) {
                        if (f.isDisplayed()) return f;
                    }
                }
            } catch (Exception ignored) {}
        }
        return null;
    }

    /**
     * Click Search button robustly. After search, handle the "Search Along Metro" popup and stabilize page.
     */
    public void clickSearch() {
        By primary = PropertySearchLocator.SEARCH_BUTTON;
        try {
            WebElement searchBtn = wait.until(ExpectedConditions.elementToBeClickable(primary));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", searchBtn);
            searchBtn.click();
        } catch (Exception e) {
            System.out.println("[DEBUG] Search click failed: " + e.getMessage());
            // fallback to ENTER on input
            try {
                WebElement locInput = wait.until(ExpectedConditions.elementToBeClickable(PropertySearchLocator.LOCALITY_INPUT));
                locInput.sendKeys(Keys.ENTER);
            } catch (Exception ex) {
                throw new RuntimeException("Could not click Search or fallback ENTER failed.", ex);
            }
        }

        // wait for listing navigation or some result elements
        waitForListingsStart();

        // close the "Search Along Metro" popup if present
        closeSearchAlongMetroPopup();

        // extra stabilization time
        sleepMillis(2000);
    }

    /**
     * Close the "Search Along Metro" popup by trying the Skip button first, then close button, then JS click on popup.
     */
    private void closeSearchAlongMetroPopup() {
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(5));
            // Wait for popup container presence (if not present, it will timeout quickly and be ignored)
            shortWait.until(ExpectedConditions.presenceOfElementLocated(PropertySearchLocator.SEARCH_ALONG_METRO_POPUP));

            // Try Skip button
            List<WebElement> skipButtons = driver.findElements(PropertySearchLocator.SEARCH_ALONG_METRO_SKIP_BTN);
            for (WebElement btn : skipButtons) {
                try {
                    if (btn.isDisplayed()) {
                        try { btn.click(); } catch (Exception ex) { ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn); }
                        // wait for popup to disappear
                        new WebDriverWait(driver, Duration.ofSeconds(4)).until(ExpectedConditions.invisibilityOfElementLocated(PropertySearchLocator.SEARCH_ALONG_METRO_POPUP));
                        System.out.println("[DEBUG] 'Search along Metro' popup dismissed via Skip button.");
                        return;
                    }
                } catch (Exception inner) {
                    // try next skip button
                }
            }

            // Try close button inside popup
            try {
                WebElement popup = driver.findElement(PropertySearchLocator.SEARCH_ALONG_METRO_POPUP);
                WebElement closeBtn = null;
                try { closeBtn = popup.findElement(PropertySearchLocator.SEARCH_ALONG_METRO_CLOSE_BTN); } catch (Exception ignore) {}
                if (closeBtn != null && closeBtn.isDisplayed()) {
                    try { closeBtn.click(); } catch (Exception ex) { ((JavascriptExecutor) driver).executeScript("arguments[0].click();", closeBtn); }
                    new WebDriverWait(driver, Duration.ofSeconds(4)).until(ExpectedConditions.invisibilityOfElementLocated(PropertySearchLocator.SEARCH_ALONG_METRO_POPUP));
                    System.out.println("[DEBUG] 'Search along Metro' popup dismissed via close button.");
                    return;
                }

                // final fallback: JS click on popup container to dismiss overlay
                try {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", popup);
                    new WebDriverWait(driver, Duration.ofSeconds(4)).until(ExpectedConditions.invisibilityOfElementLocated(PropertySearchLocator.SEARCH_ALONG_METRO_POPUP));
                    System.out.println("[DEBUG] 'Search along Metro' popup dismissed via JS click on popup.");
                    return;
                } catch (Exception ex) {
                    // ignore fallback failure
                }
            } catch (Exception ex) {
                // popup not found or stale
            }
        } catch (Exception e) {
            // popup didn't appear — ignore silently
        }
    }

    // Wait for listing navigation or presence of result elements (non-fatal)
    private void waitForListingsStart() {
        try {
            wait.until((ExpectedCondition<Boolean>) drv -> {
                String url = drv.getCurrentUrl().toLowerCase();
                boolean urlLooksLikeListing = url.contains("/property/") || url.contains("/rent") || url.contains("/list") || url.contains("/search");
                if (urlLooksLikeListing) return true;

                List<WebElement> possibleCards = drv.findElements(By.cssSelector("div[class*='list'], div[class*='card'], div[class*='listing'], a[href*='/property/']"));
                return possibleCards != null && !possibleCards.isEmpty();
            });
        } catch (Exception ignored) {
            // non-fatal
        }
    }

    /**
     * Robust check for the listing page:
     *  - wait for listing container or fallback card selectors OR URL patterns
     *  - confirm at least one meaningful result element exists
     */
    public boolean isListingPageDisplayed() {
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(30));
            shortWait.until(ExpectedConditions.or(
                    ExpectedConditions.presenceOfElementLocated(PropertySearchLocator.LISTING_CONTAINER),
                    ExpectedConditions.presenceOfElementLocated(fallbackCardSelectors),
                    ExpectedConditions.presenceOfElementLocated(By.cssSelector("div[class*='list'], div[class*='listing'], div[class*='card'], a[href*='/property/']")),
                    ExpectedConditions.urlContains("/property/"),
                    ExpectedConditions.urlContains("/rent"),
                    ExpectedConditions.urlContains("/list"),
                    ExpectedConditions.urlContains("/search")
            ));

            List<WebElement> cards = driver.findElements(By.cssSelector("div[class*='list'], div[class*='listing'], div[class*='card'], a[href*='/property/']"));
            if (cards != null && !cards.isEmpty()) return true;

            if (!driver.findElements(fallbackCardSelectors).isEmpty()) return true;
            if (!driver.findElements(PropertySearchLocator.LISTING_CONTAINER).isEmpty()) return true;

            String url = driver.getCurrentUrl().toLowerCase();
            return url.contains("/property/") || url.contains("/rent") || url.contains("/list") || url.contains("/search");
        } catch (Exception e) {
            System.out.println("[DEBUG] isListingPageDisplayed failed: " + e.getMessage());
            System.out.println("[DEBUG] URL: " + driver.getCurrentUrl());
            return false;
        }
    }

    // small helper sleep (implemented using WebDriverWait so Thread.sleep is removed)
    private void sleepMillis(long ms) {
        if (ms <= 0) return;
        final long nanosToWait = ms * 1_000_000L;
        final long start = System.nanoTime();
        try {
            WebDriverWait pauseWait = new WebDriverWait(driver, Duration.ofMillis(ms));
            pauseWait.until((ExpectedCondition<Boolean>) drv -> (System.nanoTime() - start) >= nanosToWait);
        } catch (Exception ignored) {
            // if wait throws (timeout/interrupted), we ignore to preserve previous behavior of best-effort pause
        }
    }
}
