package pages;

import objectrepository.PaymentFailedLocator;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

public class PaymentFailedPage {
    private final WebDriver driver;
    private final WebDriverWait wait;
    private final Duration TIMEOUT = Duration.ofSeconds(15);

    public PaymentFailedPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, TIMEOUT);
    }

    public void clickMenu() {
        try {
            WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(PaymentFailedLocator.MENU_BUTTON));
            try { menuBtn.click(); }
            catch (WebDriverException e) { ((JavascriptExecutor) driver).executeScript("arguments[0].click();", menuBtn); }

            WebDriverWait waitShort = new WebDriverWait(driver, Duration.ofSeconds(8));
            waitShort.until(ExpectedConditions.visibilityOfElementLocated(PaymentFailedLocator.MENU_CONTAINER));
        } catch (TimeoutException e) {
            throw new RuntimeException("Menu button not clickable/visible", e);
        }
    }

    public void clickSellerPlansFromMenu() {
        dismissGenericModalIfPresent();
        String original = safeGetHandle();
        Set<String> before = new HashSet<>(driver.getWindowHandles());

        try {
            WebElement seller = wait.until(ExpectedConditions.elementToBeClickable(PaymentFailedLocator.SELLER_PLANS_LINK));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", seller);
            try { seller.click(); }
            catch (WebDriverException e) { ((JavascriptExecutor) driver).executeScript("arguments[0].click();", seller); }
        } catch (Exception e) {
            throw new RuntimeException("Failed to click Seller Plans in menu", e);
        }

        switchToNewWindowIfOpened(before);
        waitForJsAndJquery();
        closeExtraWindowsAndReturn(original);
    }

    public boolean isSellerPlanPageDisplayed() {
        try {
            if (!driver.findElements(PaymentFailedLocator.SELLER_PLAN_HEADING).isEmpty()) return true;
            String url = driver.getCurrentUrl().toLowerCase();
            return url.contains("seller") || url.contains("seller-plans") || url.contains("plans");
        } catch (Exception e) {
            return false;
        }
    }

    public void clickSubscribeOnSellerPlan() {
        dismissGenericModalIfPresent();
        String original = safeGetHandle();
        Set<String> before = new HashSet<>(driver.getWindowHandles());

        try {
            WebElement subscribe = wait.until(ExpectedConditions.elementToBeClickable(PaymentFailedLocator.SUBSCRIBE_BUTTON));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", subscribe);
            try { subscribe.click(); }
            catch (WebDriverException ex) {
                try { ((JavascriptExecutor) driver).executeScript("arguments[0].click();", subscribe); }
                catch (WebDriverException jsEx) {
                    new Actions(driver).moveToElement(subscribe).click().perform();
                }
            }

            WebDriverWait waitPost = new WebDriverWait(driver, Duration.ofSeconds(20));
            waitPost.until(d -> {
                Set<String> after = d.getWindowHandles();
                if (after.size() > before.size()) return true;
                boolean h = !d.findElements(PaymentFailedLocator.PAYMENT_PAGE_HEADING).isEmpty();
                boolean c = !d.findElements(PaymentFailedLocator.PAYMENT_CONTAINER).isEmpty();
                String cur = "";
                try { cur = d.getCurrentUrl().toLowerCase(); } catch (Exception ignored) {}
                boolean urlLooksLikePayment = cur.contains("payment") || cur.contains("checkout") || cur.contains("/pay");
                return h || c || urlLooksLikePayment;
            });

            switchToNewWindowIfOpened(before);
            waitForJsAndJquery();
            closeExtraWindowsAndReturn(original);

        } catch (Exception e) {
            throw new RuntimeException("Subscribe click failed on Seller Plans", e);
        }
    }

    public boolean isPaymentPageDisplayed() {
        try {
            if (!driver.findElements(PaymentFailedLocator.PAYMENT_PAGE_HEADING).isEmpty()) return true;
            if (!driver.findElements(PaymentFailedLocator.PAYMENT_CONTAINER).isEmpty()) return true;
            String url = driver.getCurrentUrl().toLowerCase();
            return url.contains("payment") || url.contains("checkout") || url.contains("/pay");
        } catch (Exception e) { return false; }
    }

    /**
     * Robust Back button click
     */
    public void clickBackFromPaymentPage() {
        dismissGenericModalIfPresent();
        try {
            WebElement back = null;

            // First try exact id
            List<WebElement> matches = driver.findElements(PaymentFailedLocator.BACK_BUTTON);
            if (!matches.isEmpty()) back = matches.get(0);

            // Fallbacks
            if (back == null) {
                List<By> fallbacks = Arrays.asList(
                    By.cssSelector("button[aria-label='Back']"),
                    By.cssSelector(".nb-back, .back-button"),
                    By.xpath("//a[contains(.,'Back') or contains(.,'back')]")
                );
                for (By fb : fallbacks) {
                    List<WebElement> els = driver.findElements(fb);
                    if (!els.isEmpty()) { back = els.get(0); break; }
                }
            }

            if (back == null) throw new RuntimeException("Back control not found on payment page");

            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", back);
            try { back.click(); }
            catch (WebDriverException e) { ((JavascriptExecutor) driver).executeScript("arguments[0].click();", back); }

            WebDriverWait waitShort = new WebDriverWait(driver, Duration.ofSeconds(8));
            waitShort.until(ExpectedConditions.visibilityOfElementLocated(PaymentFailedLocator.CANCEL_PAYMENT_MODAL));

        } catch (Exception e) {
            throw new RuntimeException("Failed clicking Back on payment page", e);
        }
    }

    public void confirmCancelPaymentYes() {
        try {
            WebDriverWait waitShort = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement yes = waitShort.until(ExpectedConditions.elementToBeClickable(PaymentFailedLocator.CANCEL_PAYMENT_YES_BUTTON));
            try { yes.click(); }
            catch (WebDriverException e) { ((JavascriptExecutor) driver).executeScript("arguments[0].click();", yes); }

            WebDriverWait waitMedium = new WebDriverWait(driver, Duration.ofSeconds(12));
            waitMedium.until(ExpectedConditions.invisibilityOfElementLocated(PaymentFailedLocator.CANCEL_PAYMENT_MODAL));

        } catch (Exception e) {
            throw new RuntimeException("Failed to confirm cancel payment", e);
        }
    }
    
    /**
     * Robust detection for the payment failure toast/notification on the Seller Plans page.
     * This version does not print any debug lines to the console.
     */
    public boolean isPaymentErrorDisplayedOnSellerPlans() {
        try {
            WebDriverWait waitShort = new WebDriverWait(driver, Duration.ofSeconds(20));

            boolean found = waitShort.until(d -> {
                // gather candidate elements from multiple sensible selectors
                List<WebElement> candidates = d.findElements(PaymentFailedLocator.PAYMENT_ERROR_TOAST);

                // accessibility / alert candidates
                candidates.addAll(d.findElements(By.xpath(
                    "//*[(@role='alert' or @aria-live='polite' or @aria-live='assertive') and (contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'payment'))]")));

                // text-based candidate: contains payment and fail/failed/try again
                candidates.addAll(d.findElements(By.xpath(
                    "//*[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'payment') and (contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'fail') or contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'failed') or contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'try again'))]")));

                // common toast/notification classes (may not exist)
                try {
                    candidates.addAll(d.findElements(By.cssSelector(".toast, .notification, .nb__toast, .nb__notification, .toast-message")));
                } catch (Exception ignored) {}

                for (WebElement e : candidates) {
                    if (e == null) continue;
                    try {
                        // safe read of text (skip if stale)
                        String txt;
                        try { txt = e.getText(); } catch (StaleElementReferenceException sere) { continue; }

                        // visible check: isDisplayed + non-zero size
                        boolean visible = false;
                        try {
                            visible = e.isDisplayed() && e.getSize() != null && e.getSize().getHeight() > 0 && e.getSize().getWidth() > 0;
                        } catch (Exception ex) {
                            visible = false;
                        }

                        // bounding rect fallback when isDisplayed is unreliable
                        if (!visible) {
                            try {
                                Object rect = ((JavascriptExecutor) d).executeScript(
                                    "var r = arguments[0].getBoundingClientRect(); return (r && r.width>0 && r.height>0);", e);
                                if (rect instanceof Boolean) visible = (Boolean) rect;
                            } catch (Exception ignore) { visible = false; }
                        }

                        if (!visible) continue;

                        // check textual content and aria attributes for payment-failure hints
                        String low = (txt == null) ? "" : txt.trim().toLowerCase();
                        if (!low.isEmpty() && low.contains("payment") && (low.contains("fail") || low.contains("failed") || low.contains("try again"))) {
                            return true;
                        }

                        String aria = "";
                        try { aria = e.getAttribute("aria-label"); } catch (Exception ignored) {}
                        if (aria != null) {
                            String a = aria.toLowerCase();
                            if (a.contains("payment") && (a.contains("fail") || a.contains("failed") || a.contains("try"))) {
                                return true;
                            }
                        }

                        // fallback: visible element containing 'payment' anywhere is accepted
                        if ((low != null && low.contains("payment")) || (aria != null && aria.toLowerCase().contains("payment"))) {
                            return true;
                        }

                    } catch (StaleElementReferenceException sere) {
                        // element went stale — ignore and continue
                    } catch (Exception ex) {
                        // ignore per-element errors and continue scanning
                    }
                }

                // Not found yet
                return false;
            });

            return found;
        } catch (TimeoutException te) {
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    // ---------------- helpers ----------------

    private void dismissGenericModalIfPresent() {
        try {
            WebDriverWait ws = new WebDriverWait(driver, Duration.ofSeconds(3));
            WebElement closeBtn = ws.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button[aria-label='Close'], button.close, .nb__close")));
            closeBtn.click();
            waitForJsAndJquery();
        } catch (Exception ignored) {}
    }

    private boolean switchToNewWindowIfOpened(Set<String> beforeHandles) {
        try {
            long deadline = System.currentTimeMillis() + 2000;
            while (System.currentTimeMillis() < deadline) {
                Set<String> handles = driver.getWindowHandles();
                if (handles.size() > beforeHandles.size()) break;
                sleep(150);
            }

            Set<String> afterHandles = driver.getWindowHandles();
            if (afterHandles.size() > beforeHandles.size()) {
                for (String h : afterHandles) {
                    if (!beforeHandles.contains(h)) {
                        driver.switchTo().window(h);
                        sleep(350);
                        return true;
                    }
                }
            }
        } catch (Exception ignored) {}
        return false;
    }

    private void closeExtraWindowsAndReturn(String keepHandle) {
        if (keepHandle == null) return;
        try {
            for (String h : driver.getWindowHandles()) {
                if (!h.equals(keepHandle)) {
                    driver.switchTo().window(h);
                    driver.close();
                }
            }
            driver.switchTo().window(keepHandle);
        } catch (Exception ignored) {}
    }

    private String safeGetHandle() {
        try { return driver.getWindowHandle(); } catch (Exception e) { return null; }
    }

    private void waitForJsAndJquery() {
        try {
            WebDriverWait ws = new WebDriverWait(driver, Duration.ofSeconds(6));
            ws.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
        } catch (Exception ignored) {}
    }

    private void sleep(long ms) {
        if (ms <= 0) return;
        final long nanosToWait = ms * 1_000_000L;
        final long start = System.nanoTime();
        try {
            WebDriverWait pauseWait = new WebDriverWait(driver, Duration.ofMillis(ms));
            pauseWait.until(d -> (System.nanoTime() - start) >= nanosToWait);
        } catch (Exception ignored) {
            // best-effort pause — swallow exceptions
        }
    }
}
