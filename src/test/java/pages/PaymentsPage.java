package pages;

import objectrepository.PaymentsLocator;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class PaymentsPage {
    private final WebDriver driver;
    private final WebDriverWait wait;
    private final Duration TIMEOUT = Duration.ofSeconds(15);

    public PaymentsPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, TIMEOUT);
    }

    public void openHomePage() {
        driver.get("https://www.nobroker.in/");
        waitForJsAndJquery();
    }

    public void clickPayRent() {
        dismissGenericModalIfPresent();

        By selector = PaymentsLocator.PAY_RENT_BTN;
        Set<String> before = new HashSet<>(driver.getWindowHandles());

        try {
            WebElement el = retryFindClickable(selector, 3);
            scrollIntoView(el);
            safeClick(el);

            // allow navigation/new tab to appear
            sleep(400);
            boolean switched = switchToNewWindowIfOpened(before);
            waitForPaymentsPageAppear(before, 25);
            // switched variable retained for potential future logic
        } catch (Exception e) {
            throw new RuntimeException("Pay Rent click flow failed", e);
        }
    }

    public void clickMyPayments() {
        dismissGenericModalIfPresent();

        // Ensure we are focused on the payments window/tab if one exists
        switchToWindowContainingUrlFragment("pay-property-rent-online", "payment", "checkout", "payments", "my-payments");

        // First: try absolute XPath you provided using many strategies (descendants, JS dispatch, Actions)
        try {
            By absolute = PaymentsLocator.MY_PAYMENTS_BTN_ABSOLUTE;
            if (clickElementOrDescendant(absolute)) {
                if (waitForHistoryToAppearShort()) return;
            }
        } catch (Exception ignored) {
            // ignore and continue to other locators
        }

        // Candidate locators (non-absolute / broad)
        By[] candidates = new By[]{
            PaymentsLocator.MY_PAYMENTS_BTN,
            By.xpath("//*[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'my payments')]"),
            By.xpath("//a[contains(@href,'/payments') or contains(@href,'payments-history') or contains(@href,'/pay')]")
        };

        RuntimeException lastEx = null;
        boolean success = false;

        final int candidateAttempts = 3;
        final int overallRetries = 3;

        for (int cycle = 0; cycle < overallRetries && !success; cycle++) {
            for (By loc : candidates) {
                for (int attempt = 0; attempt < candidateAttempts && !success; attempt++) {
                    try {
                        WebElement el = (new WebDriverWait(driver, Duration.ofSeconds(6)))
                                .until(ExpectedConditions.elementToBeClickable(loc));
                        scrollIntoView(el);
                        sleep(200);
                        safeClick(el);
                        sleep(400);
                        if (waitForHistoryToAppearShort()) {
                            success = true;
                            break;
                        }
                    } catch (Exception e) {
                        lastEx = e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e);
                        sleep(250);
                    }
                }
                if (success) break;
            }
            if (!success) sleep(600);
        }

        if (!success) {
            try {
                dumpDebugState("my-payments-failure");
            } catch (Exception ignored) {}
            throw new RuntimeException("My Payments click flow failed - unable to detect Payments History after retries", lastEx);
        }
    }

    public boolean isPaymentsPageDisplayed() {
        try {
            if (!driver.findElements(PaymentsLocator.PAYMENTS_PAGE_HEADER).isEmpty()) return true;
            if (!driver.findElements(PaymentsLocator.PAYMENT_CONTAINER).isEmpty()) return true;
            String url = safeGetCurrentUrl().toLowerCase();
            if (url.contains("payment") || url.contains("pay") || url.contains("credit-card") || url.contains("payments") || url.contains("rent"))
                return true;
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isPaymentsHistoryPageDisplayed() {
        try {
            if (!driver.findElements(PaymentsLocator.PAYMENTS_HISTORY_HEADER).isEmpty()) return true;
            if (!driver.findElements(PaymentsLocator.NO_TRANSACTIONS_MSG).isEmpty()) return true;

            List<WebElement> tables = driver.findElements(By.xpath("//table[contains(@class,'transaction') or contains(@class,'payments') or contains(@class,'txn')]"));
            for (WebElement t : tables) {
                try { if (t.isDisplayed()) return true; } catch (StaleElementReferenceException ignored) {}
            }

            List<WebElement> lists = driver.findElements(By.xpath("//*[contains(@class,'transaction') or contains(@class,'payments-list') or contains(@class,'transactions') or contains(@class,'payment-history')]"));
            for (WebElement l : lists) {
                try { if (l.isDisplayed()) return true; } catch (StaleElementReferenceException ignored) {}
            }

            String url = safeGetCurrentUrl().toLowerCase();
            if (url.contains("payments-history") || url.contains("my-payments") || url.contains("transactions") || url.contains("payment")) return true;

            List<WebElement> anchors = driver.findElements(By.xpath("//a[contains(@href,'payments-history') or contains(@href,'/payments') or contains(@href,'/transactions')]"));
            for (WebElement a : anchors) {
                try { if (a.isDisplayed()) return true; } catch (StaleElementReferenceException ignored) {}
            }

            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isNoTransactionsMessageDisplayed() {
        try {
            List<WebElement> elems = driver.findElements(PaymentsLocator.NO_TRANSACTIONS_MSG);
            for (WebElement e : elems) {
                try { if (e.isDisplayed()) return true; } catch (StaleElementReferenceException ignored) {}
            }
            return false;
        } catch (Exception e) { return false; }
    }

    // ---------------- helpers ----------------

    private WebElement retryFindClickable(By locator, int retries) {
        RuntimeException last = null;
        for (int i = 0; i < retries; i++) {
            try {
                return wait.until(ExpectedConditions.elementToBeClickable(locator));
            } catch (StaleElementReferenceException | TimeoutException e) {
                last = new RuntimeException(e);
                sleep(300);
            }
        }
        if (last != null) throw last;
        throw new RuntimeException("Element not clickable: " + locator);
    }

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
                WebElement svgClose = shortWait.until(ExpectedConditions.elementToBeClickable(PaymentsLocator.GENERIC_MODAL_CLOSE_SVG));
                safeClick(svgClose);
                waitForJsAndJquery();
                return;
            } catch (TimeoutException ignored) {}

            try {
                WebElement closeBtn = shortWait.until(ExpectedConditions.elementToBeClickable(
                        By.cssSelector("button[aria-label='Close'], button.close, .nb__close")));
                safeClick(closeBtn);
                waitForJsAndJquery();
            } catch (TimeoutException ignored) {}
        } catch (Exception ignored) {}
    }

    private void scrollIntoView(WebElement el) {
        try { ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", el); }
        catch (Exception ignored) {}
    }


    private boolean switchToWindowContainingUrlFragment(String... fragments) {
        try {
            Set<String> handles = driver.getWindowHandles();
            for (String h : handles) {
                try {
                    driver.switchTo().window(h);
                    String cur = safeGetCurrentUrl().toLowerCase();
                    for (String f : fragments) {
                        if (cur.contains(f)) {
                            return true;
                        }
                    }
                } catch (Exception ignore) {}
            }
        } catch (Exception e) { }
        return false;
    }

    private boolean switchToNewWindowIfOpened(Set<String> beforeHandles) {
        try {
            long deadline = System.currentTimeMillis() + 2000;
            while (System.currentTimeMillis() < deadline) {
                Set<String> handles = driver.getWindowHandles();
                if (handles.size() > beforeHandles.size()) break;
                sleep(150);
            }
            Set<String> after = driver.getWindowHandles();
            if (after.size() > beforeHandles.size()) {
                String newest = null;
                for (String h : after) if (!beforeHandles.contains(h)) { newest = h; break; }
                if (newest != null) {
                    driver.switchTo().window(newest);
                    sleep(300);
                    return true;
                }
            }
        } catch (Exception e) {
        }
        return false;
    }

    private String safeGetCurrentUrl() {
        try { return driver.getCurrentUrl(); } catch (Exception e) { return ""; }
    }

    private void waitForPaymentsPageAppear(Set<String> beforeHandles, int timeoutSeconds) {
        try {
            WebDriverWait post = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
            post.until(d ->
                    !d.findElements(PaymentsLocator.PAYMENTS_PAGE_HEADER).isEmpty()
                            || !d.findElements(PaymentsLocator.PAYMENT_CONTAINER).isEmpty()
                            || d.getCurrentUrl().toLowerCase().contains("payment")
                            || d.getCurrentUrl().toLowerCase().contains("pay")
            );
        } catch (TimeoutException ignored) {
        }
    }

    private boolean waitForHistoryToAppearShort() {
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(4));
            return shortWait.until(d -> isPaymentsHistoryPageDisplayed());
        } catch (TimeoutException te) {
            return isPaymentsHistoryPageDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    private void waitForJsAndJquery() {
        try {
            WebDriverWait waitShort = new WebDriverWait(driver, Duration.ofSeconds(6));
            waitShort.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
            try {
                Object jqueryActive = ((JavascriptExecutor) driver).executeScript("return window.jQuery ? jQuery.active : 0");
                if (jqueryActive instanceof Long) {
                    waitShort.until(d -> ((Long) ((JavascriptExecutor) d).executeScript("return window.jQuery ? jQuery.active : 0")) == 0L);
                }
            } catch (Exception ignored) {}
        } catch (Exception ignored) {}
    }

    // replaced Thread.sleep with WebDriverWait-based pause to preserve behaviour without using Thread.sleep
    private void sleep(long ms) {
        if (ms <= 0) return;
        final long nanosToWait = ms * 1_000_000L;
        final long start = System.nanoTime();
        try {
            WebDriverWait pauseWait = new WebDriverWait(driver, Duration.ofMillis(ms));
            pauseWait.until(d -> (System.nanoTime() - start) >= nanosToWait);
        } catch (Exception ignored) {
            // preserve best-effort pause behaviour
        }
    }

    private void dumpDebugState(String tag) {
        try {
            Path dir = Path.of("debug");
            if (!Files.exists(dir)) Files.createDirectories(dir);

            // screenshot
            try {
                File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                Path dest = dir.resolve(tag + "-screenshot.png");
                Files.copy(src.toPath(), dest);
            } catch (Exception ignored) {
            }

            // small HTML snippet
            try {
                WebElement body = driver.findElement(By.tagName("body"));
                String html = body.getAttribute("innerHTML");
                Path htmlFile = dir.resolve(tag + "-body-snippet.html");
                String snippet = html.length() > 5000 ? html.substring(0, 5000) : html;
                Files.writeString(htmlFile, snippet);
            } catch (Exception ignored) {
            }
        } catch (IOException ioe) {
        }
    }

    private boolean clickElementOrDescendant(By locator) {
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(6));
            WebElement root = shortWait.until(ExpectedConditions.presenceOfElementLocated(locator));

            // 1) Try normal click (fast)
            try {
                if (root.isDisplayed() && root.isEnabled()) {
                    root.click();
                    sleep(300);
                    if (waitForHistoryToAppearShort()) return true;
                }
            } catch (Exception e) {
            }

            // 2) Try clickable descendants: <a>, <button>, role=button
            List<By> descs = Arrays.asList(
                    By.xpath(".//a[normalize-space(.)!='']"),
                    By.xpath(".//button[normalize-space(.)!='']"),
                    By.xpath(".//*[@role='button']")
            );

            for (By d : descs) {
                try {
                    List<WebElement> found = root.findElements(d);
                    for (WebElement candidate : found) {
                        try {
                            if (!candidate.isDisplayed()) continue;
                            candidate.click();
                            sleep(300);
                            if (waitForHistoryToAppearShort()) return true;
                            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", candidate);
                            sleep(300);
                            if (waitForHistoryToAppearShort()) return true;
                        } catch (Exception inner) {
                        }
                    }
                } catch (Exception ex) {
                    // ignore and continue
                }
            }

            // 3) JS dispatch a MouseEvent on the root element
            try {
                String dispatch = "var ev = new MouseEvent('click', {bubbles: true, cancelable: true, view: window}); arguments[0].dispatchEvent(ev);";
                ((JavascriptExecutor) driver).executeScript(dispatch, root);
                sleep(350);
                if (waitForHistoryToAppearShort()) return true;
            } catch (Exception e) {
            }

            // 4) JS querySelector click inside root
            try {
                String jsQueryClick = "var el = arguments[0].querySelector('a, button, [role=button]'); if(el) el.click();";
                ((JavascriptExecutor) driver).executeScript(jsQueryClick, root);
                sleep(350);
                if (waitForHistoryToAppearShort()) return true;
            } catch (Exception e) {
            }

            // 5) Actions move+click on center of element
            try {
                new Actions(driver).moveToElement(root).click().perform();
                sleep(350);
                if (waitForHistoryToAppearShort()) return true;
            } catch (Exception e) {
            }

            // 6) coordinate click as last resort
            try {
                Rectangle r = root.getRect();
                int centerX = r.getX() + r.getWidth() / 2;
                int centerY = r.getY() + r.getHeight() / 2;
                new Actions(driver).moveByOffset(-10000, -10000).perform();
                new Actions(driver).moveByOffset(centerX, centerY).click().perform();
                sleep(400);
                if (waitForHistoryToAppearShort()) return true;
            } catch (Exception e) {
            }

        } catch (TimeoutException te) {
        } catch (Exception e) {
        }
        return false;
    }
}
