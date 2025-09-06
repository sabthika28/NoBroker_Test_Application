package pages;

import java.time.Duration;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import objectrepository.ChatLocator;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.ClickHelper;

public class ChatPage {
    private final WebDriver driver;
    private final WebDriverWait wait;
    private final ExtentTest extTest;

    public ChatPage(WebDriver driver, ExtentTest extTest) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        this.extTest = extTest;
    }

    private void quietClick(By locator, String name) {
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(locator));
            WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));

            // scroll to center to reduce click interception
            try {
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center', inline:'center'});", el);
            } catch (Exception ignored) {}

            boolean clicked = ClickHelper.safeClick(driver, el, null);
            if (clicked) {
                extTest.log(Status.PASS, "Clicked: " + name);
                return;
            }

            // last-resort JS click (if safeClick didn't succeed)
            try {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
                extTest.log(Status.PASS, "Clicked via JS (fallback): " + name);
                return;
            } catch (Exception jsEx) {
                // Do not log the intermediate exceptions as WARNING; only mark failure
                throw new RuntimeException("Final click fallback failed for " + name, jsEx);
            }
        } catch (TimeoutException te) {
            // Do not spam Extent with caught timeout stack traces here; caller can decide.
            throw te;
        } catch (Exception ex) {
            // Wrap and rethrow so caller can log final result
            throw new RuntimeException("Could not click " + name + ": " + ex.getMessage(), ex);
        }
    }

    // Try to open profile dropdown (icon -> username alt). Best-effort.
    private void openProfileDropdown() {
        WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(6));

        // Try icon first
        try {
            quietClick(ChatLocator.PROFILE_ICON, "Profile icon");
            shortWait.until(d -> d.findElements(ChatLocator.MY_CHATS_LINK).size() > 0 || d.findElements(ChatLocator.MY_CHATS_LINK_FALLBACK).size() > 0);
            extTest.log(Status.INFO, "Profile dropdown opened using icon.");
            return;
        } catch (Exception e) {
            extTest.log(Status.INFO, "Profile icon open attempt did not immediately show dropdown: " + e.getMessage());
        }

        // Try username alt
        try {
            quietClick(ChatLocator.PROFILE_USERNAME_ALT, "Profile username alt");
            shortWait.until(d -> d.findElements(ChatLocator.MY_CHATS_LINK).size() > 0 || d.findElements(ChatLocator.MY_CHATS_LINK_FALLBACK).size() > 0);
            extTest.log(Status.INFO, "Profile dropdown opened using username alt.");
            return;
        } catch (Exception e) {
            extTest.log(Status.INFO, "Profile username alt attempt did not immediately show dropdown: " + e.getMessage());
            // don't fail now — proceed to try clicking links directly
        }
    }

    /**
     * Clicks the My Chats link and waits for the chat UI to be visible.
     * Throws RuntimeException on failure (so step fails neatly).
     */
    public void openMyChats() {
        String beforeUrl = driver.getCurrentUrl();
        try {
            // 1) open dropdown (best-effort)
            openProfileDropdown();

            // 2) click My Chats (exact first, fallback second)
            boolean clicked = false;
            try {
                quietClick(ChatLocator.MY_CHATS_LINK, "My Chats (exact)");
                clicked = true;
            } catch (Exception e) {
                extTest.log(Status.INFO, "Exact My Chats click did not succeed immediately: " + e.getMessage());
                try {
                    quietClick(ChatLocator.MY_CHATS_LINK_FALLBACK, "My Chats (fallback)");
                    clicked = true;
                } catch (Exception e2) {
                    extTest.log(Status.INFO, "Fallback My Chats click also did not succeed immediately: " + e2.getMessage());
                }
            }

            if (!clicked) {
                // Try a last attempt: search for a visible anchor containing 'My Chats' text
                try {
                    WebElement el = driver.findElement(By.xpath("//a[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'my chats')]"));
                    boolean ok = ClickHelper.safeClick(driver, el, null);
                    if (ok) extTest.log(Status.INFO, "Clicked My Chats via text-search fallback.");
                    clicked = ok;
                } catch (Exception ignored) {}
            }

            // 3) wait for indicators (elements OR url change). Use a mid-sized wait.
            WebDriverWait midWait = new WebDriverWait(driver, Duration.ofSeconds(20));
            boolean found = false;

            try {
                found = midWait.until(d -> {
                    // a) heading
                    try {
                        if (d.findElements(ChatLocator.MY_CHATS_HEADING).size() > 0 &&
                            d.findElement(ChatLocator.MY_CHATS_HEADING).isDisplayed()) {
                            return true;
                        }
                    } catch (Exception ignore) { }

                    // b) chat list container
                    try {
                        if (d.findElements(ChatLocator.CHAT_LIST).size() > 0 &&
                            d.findElement(ChatLocator.CHAT_LIST).isDisplayed()) {
                            return true;
                        }
                    } catch (Exception ignore) { }

                    // c) chat item elements
                    try {
                        if (d.findElements(ChatLocator.CHAT_ITEM).size() > 0 &&
                            d.findElement(ChatLocator.CHAT_ITEM).isDisplayed()) {
                            return true;
                        }
                    } catch (Exception ignore) { }

                    // d) chat fallback texts
                    try {
                        if (d.findElements(ChatLocator.CHAT_ITEM_TEXT_FALLBACK).size() > 0 &&
                            d.findElement(ChatLocator.CHAT_ITEM_TEXT_FALLBACK).isDisplayed()) {
                            return true;
                        }
                    } catch (Exception ignore) { }

                    // e) URL fragment changed to a chat-like path
                    try {
                        String after = d.getCurrentUrl();
                        if (after != null && !after.equals(beforeUrl)) {
                            String low = after.toLowerCase();
                            for (String frag : ChatLocator.CHAT_URL_FRAGMENTS) {
                                if (low.contains(frag)) {
                                    return true;
                                }
                            }
                        }
                    } catch (Exception ignore) { }

                    return false;
                });
            } catch (Exception waitEx) {
                // mid-wait couldn't detect page. We'll do final checks below instead of logging as WARNING.
                extTest.log(Status.INFO, "Mid-wait did not detect chat page quickly: " + waitEx.getMessage());
            }

            // 4) final quick fallback: short sleep + re-check
            if (!found) {
                try { Thread.sleep(700); } catch (InterruptedException ignored) {}
                if (isMyChatsPageDisplayed()) {
                    extTest.log(Status.PASS, "Detected My Chats in final quick check.");
                    return;
                }

                // last check: URL fragment explicitly
                String afterUrl = driver.getCurrentUrl();
                if (afterUrl != null && !afterUrl.equals(beforeUrl)) {
                    String low = afterUrl.toLowerCase();
                    for (String frag : ChatLocator.CHAT_URL_FRAGMENTS) {
                        if (low.contains(frag)) {
                            extTest.log(Status.PASS, "Detected My Chats via URL fragment (final): " + frag);
                            return;
                        }
                    }
                }

                // nothing worked -> fail
                extTest.log(Status.FAIL, "Could not detect My Chats page after clicking link.");
                throw new TimeoutException("Could not detect My Chats page after clicking link.");
            }

            extTest.log(Status.PASS, "My Chats page detected.");
            return;
        } catch (Exception ex) {
            extTest.log(Status.FAIL, "Could not open My Chats: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    /**
     * Boolean check used by stepdef.
     */
    public boolean isMyChatsPageDisplayed() {
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(6));
            try {
                shortWait.until(ExpectedConditions.visibilityOfElementLocated(ChatLocator.MY_CHATS_HEADING));
                return true;
            } catch (Exception ignored) {}

            try {
                shortWait.until(ExpectedConditions.visibilityOfElementLocated(ChatLocator.CHAT_LIST));
                return true;
            } catch (Exception ignored) {}

            try {
                shortWait.until(ExpectedConditions.visibilityOfElementLocated(ChatLocator.CHAT_ITEM));
                return true;
            } catch (Exception ignored) {}

            try {
                shortWait.until(ExpectedConditions.visibilityOfElementLocated(ChatLocator.CHAT_ITEM_TEXT_FALLBACK));
                return true;
            } catch (Exception ignored) {}

            String url = driver.getCurrentUrl();
            if (url != null) {
                String low = url.toLowerCase();
                for (String frag : ChatLocator.CHAT_URL_FRAGMENTS) {
                    if (low.contains(frag)) return true;
                }
            }
            return false;
        } catch (Exception e) {
            extTest.log(Status.INFO, "isMyChatsPageDisplayed: " + e.getMessage());
            return false;
        }
    }
}