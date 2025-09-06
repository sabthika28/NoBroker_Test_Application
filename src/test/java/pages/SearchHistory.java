package pages;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import objectrepository.HistoryLocator;

public class SearchHistory {
    private final WebDriver driver;
    private final WebDriverWait wait;

    public SearchHistory(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(12));
    }

    public boolean isHistorySectionVisible() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(HistoryLocator.HISTORY_CONTAINER_TEXT));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Try to get visible history items. If none found on first attempt, refresh and retry once.
     */
    public List<String> getVisibleHistoryItemsText() {
        List<String> texts = collectVisibleItems();
        if (texts.isEmpty()) {
            // try a quick refresh and retry once (some apps render history only after a fresh load)
            try {
                driver.navigate().refresh();
                // small wait for UI after refresh
                sleepMillis(800);
            } catch (Exception ignored) {}
            texts = collectVisibleItems();
        }
        return texts;
    }

    // collect using exact xpath first, then fallbacks
    private List<String> collectVisibleItems() {
        List<String> texts = new ArrayList<>();

        // 1) Try the exact xpath provided earlier
        try {
            List<WebElement> exactEls = driver.findElements(HistoryLocator.HISTORY_ITEM_EXACT);
            for (WebElement e : exactEls) {
                try {
                    if (e.isDisplayed()) {
                        String t = e.getText();
                        if (t != null && !t.trim().isEmpty()) texts.add(t.trim());
                    }
                } catch (Exception ignore) {}
            }
            if (!texts.isEmpty()) return texts;
        } catch (Exception ignored) {}

        // 2) Try fallback locators (first working set is returned)
        for (By sel : HistoryLocator.HISTORY_ITEM_LOCATORS) {
            // skip exact (already tried)
            if (sel.equals(HistoryLocator.HISTORY_ITEM_EXACT)) continue;
            try {
                List<WebElement> els = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(sel));
                if (els != null && !els.isEmpty()) {
                    for (WebElement el : els) {
                        try {
                            if (!el.isDisplayed()) continue;
                            String t = el.getText();
                            if (t != null && !t.trim().isEmpty()) texts.add(t.trim());
                        } catch (Exception inner) { /* ignore */ }
                    }
                }
            } catch (Exception ignored) { /* try next selector */ }
            if (!texts.isEmpty()) break;
        }
        return texts;
    }

    /**
     * Click the history item whose text contains expectedPartialText.
     * Uses the exact XPath first (if visible).
     */
    public boolean clickHistoryItem(String expectedPartialText) {
        String lower = expectedPartialText.toLowerCase();

        // 1) try exact locator first
        try {
            List<WebElement> exactEls = driver.findElements(HistoryLocator.HISTORY_ITEM_EXACT);
            if (exactEls != null && !exactEls.isEmpty()) {
                for (WebElement el : exactEls) {
                    try {
                        if (!el.isDisplayed()) continue;
                        String text = el.getText();
                        if (text != null && text.toLowerCase().contains(lower)) {
                            try { el.click(); } catch (Exception clickEx) { ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el); }
                            return true;
                        }
                    } catch (Exception inner) {}
                }
            }
        } catch (Exception ignored) {}

        // 2) fallbacks
        for (By sel : HistoryLocator.HISTORY_ITEM_LOCATORS) {
            if (sel.equals(HistoryLocator.HISTORY_ITEM_EXACT)) continue;
            try {
                List<WebElement> els = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(sel));
                if (els == null || els.isEmpty()) continue;
                for (WebElement el : els) {
                    try {
                        if (!el.isDisplayed()) continue;
                        String text = el.getText();
                        if (text != null && text.toLowerCase().contains(lower)) {
                            try { el.click(); } catch (Exception clickEx) { ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el); }
                            return true;
                        }
                    } catch (Exception inner) {}
                }
            } catch (Exception ignored) {}
        }

        return false;
    }

    /**
     * Read the value currently present in the locality input on the homepage.
     */
    public String getLocalityInputValue() {
        try {
            WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(HistoryLocator.LOCALITY_INPUT));
            String val = input.getAttribute("value");
            if (val == null) val = input.getText();
            return val == null ? "" : val.trim();
        } catch (Exception e) {
            return "";
        }
    }

    // small helper sleep implemented using WebDriverWait (replaces Thread.sleep)
    private void sleepMillis(long ms) {
        if (ms <= 0) return;
        final long nanosToWait = ms * 1_000_000L;
        final long start = System.nanoTime();
        try {
            WebDriverWait pauseWait = new WebDriverWait(driver, Duration.ofMillis(ms));
            pauseWait.until(drv -> (System.nanoTime() - start) >= nanosToWait);
        } catch (Exception ignored) {
            // ignore — preserve previous best-effort pause behavior
        }
    }
}
