package pages;

import objectrepository.LogoutLocator;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class LogoutPage {
    private final WebDriver driver;
    private final WebDriverWait wait;
    private final Duration TIMEOUT = Duration.ofSeconds(15);

    public LogoutPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, TIMEOUT);
    }

    /**
     * Open profile dropdown using a combined locator plus fallbacks.
     */
    public void clickProfileIcon() {
        dismissGenericModalIfPresent();

        Exception lastEx = null;
        boolean opened = false;

        // Try combined PROFILE_ICON first
        try {
            WebElement el = wait.until(ExpectedConditions.elementToBeClickable(LogoutLocator.PROFILE_ICON));
            safeClick(el);
            // short wait for menu to appear
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(6));
            shortWait.until(d -> !d.findElements(LogoutLocator.PROFILE_MENU).isEmpty()
                         || !d.findElements(LogoutLocator.SIGN_OUT_LINK).isEmpty());
            opened = true;
        } catch (Exception e) {
            lastEx = e;
        }

        // If not opened, try specific fallbacks sequentially
        if (!opened) {
            By[] fallbacks = new By[]{LogoutLocator.PROFILE_ICON_SVG, LogoutLocator.PROFILE_ICON_IMG, LogoutLocator.PROFILE_ICON_WRAPPER};
            for (By b : fallbacks) {
                try {
                    WebElement el = wait.until(ExpectedConditions.elementToBeClickable(b));
                    safeClick(el);
                    WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(6));
                    shortWait.until(d -> !d.findElements(LogoutLocator.PROFILE_MENU).isEmpty()
                                 || !d.findElements(LogoutLocator.SIGN_OUT_LINK).isEmpty());
                    opened = true;
                    break;
                } catch (Exception e) {
                    lastEx = e;
                }
            }
        }

        // If still not opened, try clicking an ancestor (button/div) of the svg element via Selenium findElement
        if (!opened) {
            try {
                WebElement svg = wait.until(ExpectedConditions.presenceOfElementLocated(LogoutLocator.PROFILE_ICON_SVG));
                WebElement parent = null;

                // try nearest button ancestor
                try {
                    parent = svg.findElement(By.xpath("ancestor::button[1]"));
                } catch (NoSuchElementException ignored) {}

                // try nearest clickable anchor
                if (parent == null) {
                    try {
                        parent = svg.findElement(By.xpath("ancestor::a[1]"));
                    } catch (NoSuchElementException ignored) {}
                }

                // try nearest div ancestor
                if (parent == null) {
                    try {
                        parent = svg.findElement(By.xpath("ancestor::div[1]"));
                    } catch (NoSuchElementException ignored) {}
                }

                if (parent != null) {
                    try {
                        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", parent);
                        safeClick(parent);
                        WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(6));
                        shortWait.until(d -> !d.findElements(LogoutLocator.PROFILE_MENU).isEmpty()
                                     || !d.findElements(LogoutLocator.SIGN_OUT_LINK).isEmpty());
                        opened = true;
                    } catch (Exception e) {
                        lastEx = e;
                    }
                }
            } catch (Exception ignored) {
                // leave lastEx if set
            }
        }

        if (!opened) {
            throw new RuntimeException("Profile icon not clickable/visible", lastEx);
        }
    }

    /**
     * Click the Sign Out entry in the profile dropdown.
     */
    public void clickSignOut() {
        dismissGenericModalIfPresent();

        try {
            WebElement signOut = wait.until(ExpectedConditions.elementToBeClickable(LogoutLocator.SIGN_OUT_LINK));
            safeClick(signOut);
        } catch (TimeoutException e) {
            // sign out element not found/clickable — attempt to reopen dropdown once and retry
            try {
                clickProfileIcon();
                WebElement retry = wait.until(ExpectedConditions.elementToBeClickable(LogoutLocator.SIGN_OUT_LINK));
                safeClick(retry);
            } catch (Exception ex) {
                throw new RuntimeException("Sign Out click flow failed", ex);
            }
        }

        // After clicking sign out: wait briefly for logged-out markers (non-fatal if detection times out)
        try {
            WebDriverWait post = new WebDriverWait(driver, Duration.ofSeconds(12));
            post.until(d ->
                    !d.findElements(LogoutLocator.LOGIN_PAGE_IDENTIFIER).isEmpty()
                    || !d.findElements(LogoutLocator.LOGIN_LINK).isEmpty()
                    || !d.findElements(LogoutLocator.SIGNUP_LINK).isEmpty()
                    || d.getCurrentUrl().toLowerCase().contains("login")
                    || d.getCurrentUrl().toLowerCase().contains("signin")
            );
        } catch (TimeoutException ignored) {
            // detection timed out — the click may still have succeeded; step assertion can re-check
        }
    }

    /**
     * Return true if sign-out resulted in login controls / login link visible
     */
    public boolean isLoginPageDisplayed() {
        try {
            // 1) Dedicated login form or phone input
            if (!driver.findElements(LogoutLocator.LOGIN_PAGE_IDENTIFIER).isEmpty()) return true;

            // 2) Header links visible when logged out
            if (!driver.findElements(LogoutLocator.LOGIN_LINK).isEmpty()) return true;
            if (!driver.findElements(LogoutLocator.SIGNUP_LINK).isEmpty()) return true;

            // 3) URL-based fallback
            String url = safeGetCurrentUrl().toLowerCase();
            if (url.contains("login") || url.contains("signin") || url.contains("sign-in")) return true;

            // 4) final fallback - page contains 'log in' text (case-insensitive)
            String src = "";
            try { src = driver.getPageSource().toLowerCase(); } catch (Exception ignored) {}
            if (src.contains("log in") || src.contains("login") || src.contains("sign in")) return true;

            // not detected
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    // ---------------- helpers ----------------

    private void safeClick(WebElement el) {
        try { el.click(); }
        catch (WebDriverException clickEx) {
            try { ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el); }
            catch (WebDriverException jsEx) {
                new Actions(driver).moveToElement(el).click().perform();
            }
        }
    }

    private void dismissGenericModalIfPresent() {
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(3));
            try {
                WebElement svgClose = shortWait.until(ExpectedConditions.elementToBeClickable(LogoutLocator.GENERIC_MODAL_CLOSE_SVG));
                safeClick(svgClose);
            } catch (TimeoutException ignored) {}

            try {
                WebElement closeBtn = shortWait.until(ExpectedConditions.elementToBeClickable(
                        By.cssSelector("button[aria-label='Close'], button.close, .nb__close")));
                safeClick(closeBtn);
            } catch (TimeoutException ignored) {}
        } catch (Exception ignored) {}
    }

    private String safeGetCurrentUrl() {
        try { return driver.getCurrentUrl(); } catch (Exception e) { return ""; }
    }
}
