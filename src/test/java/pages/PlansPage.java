package pages;

import objectrepository.PlansLocator;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PlansPage {
    private final WebDriver driver;
    private final WebDriverWait wait;
    private final Duration TIMEOUT = Duration.ofSeconds(15);

    public PlansPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, TIMEOUT);
    }

    public void openHomePage() {
        driver.get("https://www.nobroker.in/");
    }

    public void clickMenu() {
        try {
            WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(PlansLocator.MENU_BUTTON));
            try { menuBtn.click(); }
            catch (WebDriverException e) { ((JavascriptExecutor) driver).executeScript("arguments[0].click();", menuBtn); }

            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(8));
            try {
                shortWait.until(ExpectedConditions.or(
                        ExpectedConditions.visibilityOfElementLocated(PlansLocator.MENU_CONTAINER),
                        ExpectedConditions.visibilityOfElementLocated(PlansLocator.TENANT_PLANS_LINK)
                ));
            } catch (TimeoutException te) {
                // replaced Thread.sleep(250) with sleepMillis
                sleepMillis(250);
            }
        } catch (TimeoutException e) {
            throw new RuntimeException("Menu button not clickable/visible - check locator or page load.", e);
        }
    }

    public void clickPlanFromMenu(String planName) {
        dismissGenericModalIfPresent();

        String originalHandle = safeGetHandle();
        Set<String> beforeHandles = new HashSet<>(driver.getWindowHandles());

        By[] candidates = new By[]{
                PlansLocator.planLinkByName(planName),
                PlansLocator.TENANT_PLANS_LINK,
                PlansLocator.BUYER_PLANS_LINK,
                PlansLocator.OWNER_PLANS_LINK,
                PlansLocator.SELLER_PLANS_LINK
        };

        boolean clicked = false;
        Exception lastEx = null;
        for (By sel : candidates) {
            try {
                if (driver.findElements(sel).isEmpty()) continue;
                WebElement el = wait.until(ExpectedConditions.elementToBeClickable(sel));
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", el);
                try { el.click(); }
                catch (WebDriverException e) { ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el); }
                clicked = true;
                break;
            } catch (Exception e) {
                lastEx = e;
            }
        }

        if (!clicked) throw new RuntimeException("Failed to click plan '" + planName + "' in menu", lastEx);

        switchToNewWindowIfOpened(beforeHandles);
        waitForJsAndJquery();
        closeExtraWindowsAndReturn(originalHandle);
    }

    public boolean isPlanPageDisplayed(String planName) {
        try {
            if (!driver.findElements(PlansLocator.planPageHeading(planName)).isEmpty()) return true;
            String url = driver.getCurrentUrl().toLowerCase();
            if (url.contains(planName.toLowerCase().replaceAll("\\s+","-")) || url.contains("plans") || url.contains("plans/")) return true;
            return !driver.findElements(By.xpath(
                    "//*[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'), '" 
                            + planName.toLowerCase() + "') and (self::h1 or self::h2 or contains(@class,'heading'))]"
            )).isEmpty();
        } catch (Exception e) { return false; }
    }

    public void clickSubscribeOnPlanPage() {
        dismissGenericModalIfPresent();

        String originalHandle = safeGetHandle();
        Set<String> beforeHandles = new HashSet<>(driver.getWindowHandles());

        try {
            By subSelector = PlansLocator.subscribeButtonForPlan("");

            List<WebElement> candidates = driver.findElements(subSelector);
            WebElement chosen = null;
            for (WebElement c : candidates) {
                try {
                    if (c.isDisplayed() && c.isEnabled()) {
                        chosen = c;
                        break;
                    }
                } catch (StaleElementReferenceException sere) {}
            }

            if (chosen == null) {
                List<WebElement> broad = driver.findElements(By.xpath(
                        "//button[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'subscribe') or contains(.,'Subscribe')]"));
                for (WebElement b : broad) {
                    try {
                        if (b.isDisplayed() && b.isEnabled()) { chosen = b; break; }
                    } catch (StaleElementReferenceException ignored) {}
                }
            }

            if (chosen == null) {
                throw new RuntimeException("Subscribe button not found (no visible/enabled candidate)");
            }

            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", chosen);

            try {
                wait.until(ExpectedConditions.elementToBeClickable(chosen));
                try { chosen.click(); }
                catch (WebDriverException clickEx) {
                    try { ((JavascriptExecutor) driver).executeScript("arguments[0].click();", chosen); }
                    catch (WebDriverException jsEx) {
                        try { new Actions(driver).moveToElement(chosen).click().perform(); }
                        catch (Exception actEx) { throw jsEx; }
                    }
                }
            } catch (TimeoutException te) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", chosen);
            }

            WebDriverWait postClickWait = new WebDriverWait(driver, Duration.ofSeconds(20));
            try {
                postClickWait.until(d -> {
                    Set<String> after = d.getWindowHandles();
                    if (after.size() > beforeHandles.size()) return true;
                    boolean paymentHeading = !d.findElements(PlansLocator.PAYMENT_PAGE_HEADING).isEmpty();
                    boolean paymentContainer = !d.findElements(PlansLocator.PAYMENT_CONTAINER).isEmpty();
                    String cur = "";
                    try { cur = d.getCurrentUrl().toLowerCase(); } catch (Exception ignored) {}
                    boolean urlLooksLikePayment = cur.contains("payment") || cur.contains("checkout") || cur.contains("/pay");
                    return paymentHeading || paymentContainer || urlLooksLikePayment;
                });
            } catch (TimeoutException ignore) {}

            switchToNewWindowIfOpened(beforeHandles);
            waitForJsAndJquery();
            closeExtraWindowsAndReturn(originalHandle);

        } catch (Exception e) {
            throw new RuntimeException("Subscribe click flow failed: " + e.getMessage(), e);
        }
    }

    public boolean isPaymentPageDisplayed() {
        try {
            if (!driver.findElements(PlansLocator.PAYMENT_PAGE_HEADING).isEmpty()) return true;
            if (!driver.findElements(PlansLocator.PAYMENT_CONTAINER).isEmpty()) return true;
            String url = driver.getCurrentUrl().toLowerCase();
            if (url.contains("payment") || url.contains("checkout") || url.contains("/pay")) return true;
            return false;
        } catch (Exception e) { return false; }
    }

    // ---------------- helpers ----------------

    private void dismissGenericModalIfPresent() {
        try {
            WebDriverWait waitShort = new WebDriverWait(driver, Duration.ofSeconds(3));
            try {
                WebElement svgClose = waitShort.until(ExpectedConditions.elementToBeClickable(PlansLocator.GENERIC_MODAL_CLOSE_SVG));
                try { svgClose.click(); } catch (Exception ex) { ((JavascriptExecutor) driver).executeScript("arguments[0].click();", svgClose); }
                waitForJsAndJquery();
                return;
            } catch (TimeoutException ignored) {}

            try {
                WebElement closeBtn = waitShort.until(ExpectedConditions.elementToBeClickable(
                        By.cssSelector("button[aria-label='Close'], button.close, .nb__close")));
                try { closeBtn.click(); } catch (Exception ex) { ((JavascriptExecutor) driver).executeScript("arguments[0].click();", closeBtn); }
                waitForJsAndJquery();
            } catch (TimeoutException ignored) {}
        } catch (Exception ignored) {}
    }

    private boolean switchToNewWindowIfOpened(Set<String> beforeHandles) {
        try {
            long deadline = System.currentTimeMillis() + 2000;
            while (System.currentTimeMillis() < deadline) {
                Set<String> handles = driver.getWindowHandles();
                if (handles.size() > beforeHandles.size()) break;
                // replaced Thread.sleep(150) with sleepMillis
                sleepMillis(150);
            }

            Set<String> afterHandles = driver.getWindowHandles();
            if (afterHandles.size() > beforeHandles.size()) {
                String newest = null;
                for (String h : afterHandles) {
                    if (!beforeHandles.contains(h)) { newest = h; break; }
                }
                if (newest != null) {
                    driver.switchTo().window(newest);
                    // replaced Thread.sleep(350) with sleepMillis
                    sleepMillis(350);
                    return true;
                }
            }
        } catch (Exception ignored) {}
        return false;
    }

    private void closeExtraWindowsAndReturn(String keepHandle) {
        if (keepHandle == null) return;
        try {
            Set<String> handles = driver.getWindowHandles();
            for (String h : handles) {
                if (!h.equals(keepHandle)) {
                    try {
                        driver.switchTo().window(h);
                        driver.close();
                    } catch (Exception ignored) {}
                }
            }
            try { driver.switchTo().window(keepHandle); } catch (Exception ignored) {}
        } catch (Exception ignored) {}
    }

    private String safeGetHandle() {
        try { return driver.getWindowHandle(); } catch (Exception e) { return null; }
    }

    private void waitForJsAndJquery() {
        try {
            WebDriverWait waitShort = new WebDriverWait(driver, Duration.ofSeconds(6));
            waitShort.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
            try {
                Object jqueryActive = ((JavascriptExecutor) driver).executeScript("return window.jQuery ? jQuery.active : 0");
                if (jqueryActive instanceof Long) {
                    waitShort.until(d -> ((Long) ((JavascriptExecutor) d).executeScript(
                            "return window.jQuery ? jQuery.active : 0")) == 0L);
                }
            } catch (Exception ignored) {}
        } catch (Exception ignored) {}
    }

    // small helper sleep (replaces Thread.sleep) implemented using WebDriverWait
    private void sleepMillis(long ms) {
        if (ms <= 0) return;
        final long nanosToWait = ms * 1_000_000L;
        final long start = System.nanoTime();
        try {
            WebDriverWait pauseWait = new WebDriverWait(driver, Duration.ofMillis(ms));
            pauseWait.until(d -> (System.nanoTime() - start) >= nanosToWait);
        } catch (Exception ignored) {
            // ignore — preserve previous best-effort pause behavior
        }
    }
}
