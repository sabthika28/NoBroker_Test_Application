package pages;

import objectrepository.RewardsLocator;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Set;
import java.util.HashSet;

public class RewardsPage {
    private final WebDriver driver;
    private final WebDriverWait wait;
    private static final Duration TIMEOUT = Duration.ofSeconds(15);

    public RewardsPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, TIMEOUT);
    }

    public void clickRewardIcon() {
        dismissGenericModalIfPresent();
        Set<String> before = new HashSet<>(driver.getWindowHandles());
        try {
            WebElement icon = wait.until(ExpectedConditions.presenceOfElementLocated(RewardsLocator.REWARD_ICON));
            // try normal clickable wait
            try {
                wait.until(ExpectedConditions.elementToBeClickable(RewardsLocator.REWARD_ICON)).click();
            } catch (Exception e) {
                // fallback JS click or Actions
                try {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", icon);
                } catch (Exception jsEx) {
                    new Actions(driver).moveToElement(icon).click().perform();
                }
            }
            // allow potential navigation or fragment change
            sleep(300);
            switchToNewWindowIfOpened(before);
            waitForRewardsPageAppear(10);
        } catch (Exception e) {
            throw new RuntimeException("Failed to click Rewards icon", e);
        }
    }

    public boolean isRewardsPageDisplayed() {
        try {
            // 1) element markers
            if (!driver.findElements(RewardsLocator.REWARDS_PAGE_HEADER).isEmpty()) return true;
            if (!driver.findElements(RewardsLocator.REWARDS_CONTAINER).isEmpty()) return true;

            // 2) url heuristics
            String url = safeGetCurrentUrl().toLowerCase();
            if (url.contains("reward") || url.contains("rewards")) return true;

            // 3) fallback: page contains the word rewards somewhere in visible DOM
            try {
                Boolean found = (Boolean) ((JavascriptExecutor) driver).executeScript(
                        "return !!document.body && document.body.innerText.toLowerCase().indexOf('reward') !== -1;");
                if (Boolean.TRUE.equals(found)) return true;
            } catch (Exception ignored) {}

            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public void scrollThroughRewards() {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            // get full height
            long lastHeight = (long) ((Number) js.executeScript("return Math.max(document.body.scrollHeight, document.documentElement.scrollHeight);")).longValue();
            long viewport = ((Number) js.executeScript("return window.innerHeight || document.documentElement.clientHeight;")).longValue();

            long scrolled = 0;
            // scroll in steps until we reach bottom or up to 40 iterations to avoid infinite loop
            for (int i = 0; i < 40 && scrolled < lastHeight; i++) {
                scrolled += viewport - 50; // overlap a bit
                if (scrolled > lastHeight) scrolled = lastHeight;
                js.executeScript("window.scrollTo({top: arguments[0], behavior: 'smooth'});", scrolled);
                sleep(600); // allow rendering
                // recalc height in case of lazy-loaded content
                long newHeight = ((Number) js.executeScript("return Math.max(document.body.scrollHeight, document.documentElement.scrollHeight);")).longValue();
                if (newHeight > lastHeight) {
                    lastHeight = newHeight;
                }
            }
            // final ensure bottom
            js.executeScript("window.scrollTo({top: document.body.scrollHeight, behavior: 'instant'});");
            sleep(400);
        } catch (Exception e) {
            throw new RuntimeException("Failed while scrolling rewards page", e);
        }
    }

    // ---------------- helpers ----------------

    private void dismissGenericModalIfPresent() {
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(2));
            try {
                WebElement svgClose = shortWait.until(ExpectedConditions.elementToBeClickable(RewardsLocator.GENERIC_MODAL_CLOSE_SVG));
                try { svgClose.click(); } catch (Exception ex) { ((JavascriptExecutor) driver).executeScript("arguments[0].click();", svgClose); }
                sleep(200);
            } catch (Exception ignored) {}
        } catch (Exception ignored) {}
    }

    private boolean switchToNewWindowIfOpened(Set<String> beforeHandles) {
        try {
            long deadline = System.currentTimeMillis() + 1500;
            while (System.currentTimeMillis() < deadline) {
                Set<String> handles = driver.getWindowHandles();
                if (handles.size() > beforeHandles.size()) break;
                sleep(100);
            }
            Set<String> after = driver.getWindowHandles();
            if (after.size() > beforeHandles.size()) {
                String newest = null;
                for (String h : after) if (!beforeHandles.contains(h)) { newest = h; break; }
                if (newest != null) {
                    driver.switchTo().window(newest);
                    sleep(200);
                    return true;
                }
            }
        } catch (Exception ignored) {}
        return false;
    }

    private String safeGetCurrentUrl() {
        try { return driver.getCurrentUrl(); } catch (Exception e) { return ""; }
    }

    private void waitForRewardsPageAppear(int timeoutSec) {
        try {
            WebDriverWait post = new WebDriverWait(driver, Duration.ofSeconds(timeoutSec));
            post.until(d -> {
                try {
                    if (!d.findElements(RewardsLocator.REWARDS_PAGE_HEADER).isEmpty()) return true;
                    if (!d.findElements(RewardsLocator.REWARDS_CONTAINER).isEmpty()) return true;
                    String cur = "";
                    try { cur = d.getCurrentUrl().toLowerCase(); } catch (Exception ignored) {}
                    return cur.contains("reward") || cur.contains("rewards");
                } catch (Exception e) { return false; }
            });
        } catch (TimeoutException ignored) {}
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
}
