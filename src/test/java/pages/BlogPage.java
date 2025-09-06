package pages;

import objectrepository.BlogLocator;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Set;
import java.util.HashSet;

public class BlogPage {
    private final WebDriver driver;
    private final WebDriverWait wait;
    private final Duration TIMEOUT = Duration.ofSeconds(15);

    public BlogPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, TIMEOUT);
    }

    public void openHomePage() {
        driver.get("https://www.nobroker.in/");
        waitForJsAndJquery();
    }

    public void clickMenu() {
        dismissGenericModalIfPresent();
        try {
            WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(BlogLocator.MENU_BUTTON));
            safeClick(menuBtn);

            WebDriverWait waitShort = new WebDriverWait(driver, Duration.ofSeconds(8));
            try {
                waitShort.until(d ->
                        !d.findElements(BlogLocator.MENU_CONTAINER).isEmpty()
                                || !d.findElements(BlogLocator.BLOG_LINK).isEmpty()
                );
            } catch (TimeoutException ignored) {
                // continue even if menu container didn't appear within 8s
            }
        } catch (TimeoutException e) {
            throw new RuntimeException("Menu button not clickable/visible", e);
        }
    }

    public void clickBlogFromMenu() {
        dismissGenericModalIfPresent();
        Set<String> before = new HashSet<>(driver.getWindowHandles());
        try {
            WebElement blogLink = wait.until(ExpectedConditions.elementToBeClickable(BlogLocator.BLOG_LINK));
            scrollIntoView(blogLink);
            safeClick(blogLink);

            sleep(300);
            switchToNewWindowIfOpened(before);
            waitForBlogPageAppear(20);
        } catch (Exception e) {
            throw new RuntimeException("Failed to click Blog link from menu", e);
        }
    }

    public boolean isBlogPageDisplayed() {
        try {
            if (!driver.findElements(BlogLocator.BLOG_PAGE_HEADING).isEmpty()) return true;
            if (!driver.findElements(BlogLocator.POST_FREE_PROPERTY_AD_BUTTON).isEmpty()) return true;
            String url = safeGetCurrentUrl().toLowerCase();
            if (url.contains("/blog") || url.contains("nobroker.in/blog") || url.contains("the-nobroker-times")) return true;
            return !driver.findElements(By.xpath(
                    "//*[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'blog') and (self::h1 or self::h2 or contains(@class,'heading'))]"
            )).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    public void clickPostFreePropertyAd() {
        dismissGenericModalIfPresent();
        Set<String> before = new HashSet<>(driver.getWindowHandles());
        try {
            // try multiple fallbacks: the specific menu xpath you provided, then the generic locator
            By[] candidates = new By[] {
                    By.xpath("/html/body/nav/div/div[2]/div/a"),
                    BlogLocator.POST_FREE_PROPERTY_AD_BUTTON
            };

            boolean clicked = false;
            Exception lastEx = null;
            for (By sel : candidates) {
                try {
                    WebElement btn = (new WebDriverWait(driver, Duration.ofSeconds(6)))
                            .until(ExpectedConditions.elementToBeClickable(sel));
                    scrollIntoView(btn);
                    safeClick(btn);
                    sleep(300);
                    switchToNewWindowIfOpened(before);
                    // wait for something indicative of the post ad page (URL or form)
                    WebDriverWait waitShort = new WebDriverWait(driver, Duration.ofSeconds(8));
                    try {
                        waitShort.until(d ->
                                !d.findElements(BlogLocator.POST_AD_FORM).isEmpty()
                                        || d.getCurrentUrl().toLowerCase().contains("post")
                                        || d.getCurrentUrl().toLowerCase().contains("post-property")
                        );
                    } catch (TimeoutException ignored) {}
                    clicked = true;
                    break;
                } catch (TimeoutException te) {
                    lastEx = te;
                } catch (Exception e) {
                    lastEx = e;
                }
            }

            if (!clicked) {
                throw lastEx != null ? new RuntimeException("POST button not clickable", lastEx)
                        : new RuntimeException("POST button not clickable (unknown)");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to click Post Free Property Ad", e);
        }
    }

    public boolean isPostAdPageDisplayed() {
        try {
            // 1) Post ad form presence
            if (!driver.findElements(BlogLocator.POST_AD_FORM).isEmpty()) return true;

            // 2) URL check for common patterns
            String url = safeGetCurrentUrl().toLowerCase();
            if (url.contains("post") || url.contains("post-property") || url.contains("/post-your-property") || url.contains("post-your-property")) {
                return true;
            }

            // 3) Look for text/heading that suggests "post property" flow
            if (!driver.findElements(By.xpath(
                    "//*[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'post free property') " +
                            "or contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'post your property') " +
                            "or contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'post property')]")).isEmpty()) {
                return true;
            }

            // 4) fallback: check for common inputs used in property post forms (title, address, price etc.)
            if (!driver.findElements(By.xpath("//input[contains(@name,'title') or contains(@id,'title') or contains(@placeholder,'title')]")).isEmpty())
                return true;
            if (!driver.findElements(By.xpath("//input[contains(@name,'address') or contains(@id,'address') or contains(@placeholder,'address')]")).isEmpty())
                return true;

            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public void returnHome() {
        dismissGenericModalIfPresent();
        try {
            if (!driver.findElements(BlogLocator.SITE_LOGO).isEmpty()) {
                try {
                    WebElement logo = wait.until(ExpectedConditions.elementToBeClickable(BlogLocator.SITE_LOGO));
                    safeClick(logo);
                    waitForJsAndJquery();

                    WebDriverWait waitShort = new WebDriverWait(driver, Duration.ofSeconds(6));
                    try { waitShort.until(d -> !d.findElements(BlogLocator.HOME_PAGE_IDENTIFIER).isEmpty()); } catch (TimeoutException ignored) {}
                    return;
                } catch (Exception ignored) { /* fallback below */ }
            }
        } catch (Exception ignored) {}

        try {
            driver.get("https://www.nobroker.in/");
            waitForJsAndJquery();
        } catch (Exception e) {
            throw new RuntimeException("Failed to return to home page", e);
        }
    }

    // ---------------- helpers ----------------

    private void dismissGenericModalIfPresent() {
        try {
            WebDriverWait waitShort = new WebDriverWait(driver, Duration.ofSeconds(3));
            try {
                WebElement svgClose = waitShort.until(ExpectedConditions.elementToBeClickable(BlogLocator.GENERIC_MODAL_CLOSE_SVG));
                safeClick(svgClose);
                waitForJsAndJquery();
                return;
            } catch (TimeoutException ignored) {}

            try {
                WebElement closeBtn = waitShort.until(ExpectedConditions.elementToBeClickable(
                        By.cssSelector("button[aria-label='Close'], button.close, .nb__close")));
                safeClick(closeBtn);
                waitForJsAndJquery();
            } catch (TimeoutException ignored) {}
        } catch (Exception ignored) {}
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

    private void scrollIntoView(WebElement el) {
        try { ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", el); } catch (Exception ignored) {}
    }


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

    private boolean switchToNewWindowIfOpened(Set<String> beforeHandles) {
        try {
            long deadline = System.currentTimeMillis() + 2000;
            while (System.currentTimeMillis() < deadline) {
                Set<String> handles = driver.getWindowHandles();
                if (handles.size() > beforeHandles.size()) break;
                sleep(100);
            }
            Set<String> after = driver.getWindowHandles();
            if (after.size() > beforeHandles.size()) {
                String newest = null;
                for (String h : after) {
                    if (!beforeHandles.contains(h)) { newest = h; break; }
                }
                if (newest != null) {
                    driver.switchTo().window(newest);
                    sleep(300);
                    return true;
                }
            }
        } catch (Exception ignored) {}
        return false;
    }

    private boolean switchToWindowContainingUrlFragment(String... fragments) {
        try {
            Set<String> handles = driver.getWindowHandles();
            for (String h : handles) {
                try {
                    driver.switchTo().window(h);
                    String cur = safeGetCurrentUrl().toLowerCase();
                    for (String f : fragments) if (cur.contains(f)) return true;
                } catch (Exception ignore) {}
            }
        } catch (Exception ignored) {}
        return false;
    }

    private void waitForBlogPageAppear(int timeoutSec) {
        try {
            WebDriverWait post = new WebDriverWait(driver, Duration.ofSeconds(timeoutSec));
            post.until(d ->
                    !d.findElements(BlogLocator.BLOG_PAGE_HEADING).isEmpty()
                            || !d.findElements(BlogLocator.POST_FREE_PROPERTY_AD_BUTTON).isEmpty()
                            || d.getCurrentUrl().toLowerCase().contains("/blog")
            );
        } catch (TimeoutException ignored) {}
    }

    public boolean isHomePageDisplayed() {
        try {
            // 1) explicit marker element (search input / hero)
            if (!driver.findElements(BlogLocator.HOME_PAGE_IDENTIFIER).isEmpty()) return true;

            // 2) logo present and clickable (common on home)
            try {
                if (!driver.findElements(BlogLocator.SITE_LOGO).isEmpty()) {
                    WebElement logo = driver.findElement(BlogLocator.SITE_LOGO);
                    if (logo.isDisplayed()) return true;
                }
            } catch (Exception ignored) {}

            // 3) URL heuristic: root domain or root path
            String url = safeGetCurrentUrl().toLowerCase();
            if (url.equals("https://www.nobroker.in/") || url.equals("https://nobroker.in/") ||
                url.endsWith("/nobroker.in/") || url.endsWith("/nobroker.in") ||
                url.equals("about:blank") == false && (url.equals("https://www.nobroker.in") || url.equals("https://nobroker.in")))
            {
                // if URL is homepage-ish, consider it displayed
                if (url.equals("https://www.nobroker.in/") || url.equals("https://www.nobroker.in")) return true;
            }
            if (url.endsWith("/") && (url.equals("https://www.nobroker.in/") || url.equals("https://nobroker.in/"))) return true;

            // 4) fallback: look for hero heading text snippet
            if (!driver.findElements(By.xpath("//*[contains(.,\"World's Largest NoBrokerage Property Site\") or contains(.,'Search upto 3 localities')]")).isEmpty())
                return true;

            return false;
        } catch (Exception e) {
            return false;
        }
    }


    private String safeGetCurrentUrl() {
        try { return driver.getCurrentUrl(); } catch (Exception e) { return ""; }
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
}
