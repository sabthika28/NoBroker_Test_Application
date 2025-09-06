package pages;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import objectrepository.RentalAgreementLocator;

import java.time.Duration;
import java.util.Set;

public class RentalAgreementPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    public RentalAgreementPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    public void openHomePage() {
        driver.get("https://www.nobroker.in/");
    }

    /**
     * Click the top-right menu button and wait until the Rental Agreement link is visible.
     */
    public void clickMenu() {
        try {
            WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(RentalAgreementLocator.MENU_BUTTON));
            try {
                menuBtn.click();
            } catch (WebDriverException e) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", menuBtn);
            }

            WebDriverWait waitShort = new WebDriverWait(driver, Duration.ofSeconds(8));
            try {
                waitShort.until(ExpectedConditions.or(
                        ExpectedConditions.visibilityOfElementLocated(RentalAgreementLocator.MENU_CONTAINER),
                        ExpectedConditions.visibilityOfElementLocated(RentalAgreementLocator.RENTAL_AGREEMENT_LINK),
                        ExpectedConditions.visibilityOfElementLocated(RentalAgreementLocator.RENTAL_AGREEMENT_LINK_FALLBACK)
                ));
            } catch (TimeoutException te) {
                sleepMillis(300);
            }
        } catch (TimeoutException e) {
            throw new RuntimeException("Menu button not clickable/visible - check locator or page load.", e);
        }
    }

    /**
     * Click Rental Agreement in menu.
     */
    public void clickRentalAgreement() {
        dismissGenericModalIfPresent();

        By[] candidates = new By[] {
                RentalAgreementLocator.RENTAL_AGREEMENT_LINK,
                RentalAgreementLocator.RENTAL_AGREEMENT_LINK_FALLBACK
        };

        boolean clicked = false;
        Exception lastEx = null;
        for (By sel : candidates) {
            try {
                WebElement link = wait.until(ExpectedConditions.presenceOfElementLocated(sel));
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", link);
                try {
                    wait.until(ExpectedConditions.elementToBeClickable(sel));
                    try {
                        link.click();
                    } catch (WebDriverException e) {
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", link);
                    }
                } catch (TimeoutException te) {
                    try {
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", link);
                    } catch (Exception jsEx) {
                        throw jsEx;
                    }
                }

                clicked = true;
                break;
            } catch (Exception e) {
                lastEx = e;
            }
        }

        if (!clicked) {
            throw new RuntimeException("Failed to click Rental Agreement link", lastEx);
        }

        switchToNewWindowIfOpened();

        try {
            WebDriverWait pageWait = new WebDriverWait(driver, Duration.ofSeconds(10));
            pageWait.until(ExpectedConditions.or(
                    ExpectedConditions.presenceOfElementLocated(RentalAgreementLocator.RENTAL_AGREEMENT_PAGE),
                    ExpectedConditions.urlContains("rental"),
                    ExpectedConditions.urlContains("agreement")
            ));
        } catch (TimeoutException ignored) {}

        waitForJsAndJquery();
    }

    public boolean isRentalAgreementPageDisplayed() {
        try {
            if (!driver.findElements(RentalAgreementLocator.RENTAL_AGREEMENT_PAGE).isEmpty()) return true;
            String url = driver.getCurrentUrl().toLowerCase();
            if (url.contains("rental") || url.contains("agreement") || url.contains("rent")) return true;
            return !driver.findElements(By.xpath("//*[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'rental agreement')]")).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    // -------------------- Renew --------------------

    public void clickRenewYourAgreement() {
        try {
            dismissGenericModalIfPresent();

            final String beforeUrl = driver.getCurrentUrl();
            WebElement el = wait.until(ExpectedConditions.elementToBeClickable(RentalAgreementLocator.RENEW_YOUR_AGREEMENT));
            try { el.click(); } catch (WebDriverException e) { ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el); }

            // Wait for URL change or expected signals
            WebDriverWait waitShort = new WebDriverWait(driver, Duration.ofSeconds(10));
            try {
                waitShort.until(d -> {
                    String cur = d.getCurrentUrl();
                    if (!cur.equals(beforeUrl)) return true;
                    boolean renewalHeading = !d.findElements(RentalAgreementLocator.RENEWAL_PAGE).isEmpty();
                    boolean modalExact = !d.findElements(RentalAgreementLocator.GENERIC_MODAL_CLOSE_SVG_EXACT).isEmpty();
                    boolean modalGeneric = !d.findElements(RentalAgreementLocator.GENERIC_MODAL_CLOSE_SVG).isEmpty();
                    boolean contractFlow = cur.toLowerCase().contains("/contract") || cur.toLowerCase().contains("leadid") || cur.toLowerCase().contains("nbfr");
                    return renewalHeading || modalExact || modalGeneric || contractFlow;
                });
            } catch (TimeoutException te) {
                try {
                    WebDriverWait waitLong = new WebDriverWait(driver, Duration.ofSeconds(12));
                    waitLong.until(d -> {
                        String cur = d.getCurrentUrl();
                        if (!cur.equals(beforeUrl)) return true;
                        boolean renewalHeading = !d.findElements(RentalAgreementLocator.RENEWAL_PAGE).isEmpty();
                        boolean modalExact = !d.findElements(RentalAgreementLocator.GENERIC_MODAL_CLOSE_SVG_EXACT).isEmpty();
                        boolean modalGeneric = !d.findElements(RentalAgreementLocator.GENERIC_MODAL_CLOSE_SVG).isEmpty();
                        boolean contractFlow = cur.toLowerCase().contains("/contract") || cur.toLowerCase().contains("leadid") || cur.toLowerCase().contains("nbfr");
                        return renewalHeading || modalExact || modalGeneric || contractFlow;
                    });
                } catch (TimeoutException e) {
                    // silent fallback
                }
            }

            // If modal appeared, handle it
            if (!driver.findElements(RentalAgreementLocator.GENERIC_MODAL_CLOSE_SVG_EXACT).isEmpty() ||
                !driver.findElements(RentalAgreementLocator.GENERIC_MODAL_CLOSE_SVG).isEmpty()) {
                handleNoEligibleAgreementModal();
            }

            switchToNewWindowIfOpened();
            waitForJsAndJquery();
        } catch (TimeoutException e) {
            throw new RuntimeException("Renew Your Agreement not clickable/found", e);
        }
    }

    public boolean isRenewalPageDisplayed() {
        try {
            // 1) explicit heading
            if (!driver.findElements(RentalAgreementLocator.RENEWAL_PAGE).isEmpty()) return true;

            // 2) URL heuristics
            String url = driver.getCurrentUrl().toLowerCase();
            if (url.contains("renew") || url.contains("/contract") || url.contains("leadid") || url.contains("nbfr")) {
                return true;
            }

            // 3) container heuristics
            if (!driver.findElements(By.xpath("//*[contains(@id,'contract') or contains(@class,'contract') or contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'renew')]")).isEmpty()) {
                return true;
            }

            return false;
        } catch (Exception e) {
            return false;
        }
    }

    // -------------------- Upload Draft --------------------

    public void clickUploadYourDraft() {
        try {
            dismissGenericModalIfPresent();
            WebElement el = wait.until(ExpectedConditions.presenceOfElementLocated(RentalAgreementLocator.UPLOAD_YOUR_DRAFT));
            try { wait.until(ExpectedConditions.elementToBeClickable(RentalAgreementLocator.UPLOAD_YOUR_DRAFT)).click(); }
            catch (Exception e) { ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el); }

            switchToNewWindowIfOpened();

            try {
                WebDriverWait pageWait = new WebDriverWait(driver, Duration.ofSeconds(8));
                pageWait.until(ExpectedConditions.or(
                    ExpectedConditions.presenceOfElementLocated(RentalAgreementLocator.UPLOAD_PAGE),
                    ExpectedConditions.urlContains("/contract"),
                    ExpectedConditions.urlContains("leadid")
                ));
            } catch (TimeoutException ignored) {}

            waitForJsAndJquery();
        } catch (TimeoutException e) {
            throw new RuntimeException("Upload Draft not found/clickable", e);
        }
    }

    public boolean isUploadPageDisplayed() {
        try {
            if (!driver.findElements(RentalAgreementLocator.UPLOAD_PAGE).isEmpty()) return true;
            String url = driver.getCurrentUrl().toLowerCase();
            if (url.contains("upload") || url.contains("draft") || url.contains("/contract") || url.contains("leadid")) return true;
            boolean found = !driver.findElements(By.xpath("//h1[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'upload') or contains(.,'Upload Draft') or contains(.,'Upload your Draft')]")).isEmpty();
            if (found) return true;
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    // -------------------- E-Stamped --------------------

    public void clickEStampedAgreement() {
        try {
            dismissGenericModalIfPresent();
            WebElement el = wait.until(ExpectedConditions.presenceOfElementLocated(RentalAgreementLocator.E_STAMPED_AGREEMENT));
            try { wait.until(ExpectedConditions.elementToBeClickable(RentalAgreementLocator.E_STAMPED_AGREEMENT)).click(); }
            catch (Exception e) { ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el); }

            switchToNewWindowIfOpened();
            try {
                WebDriverWait pageWait = new WebDriverWait(driver, Duration.ofSeconds(8));
                pageWait.until(ExpectedConditions.or(
                    ExpectedConditions.presenceOfElementLocated(RentalAgreementLocator.E_STAMPED_PAGE),
                    ExpectedConditions.urlContains("/contract"),
                    ExpectedConditions.urlContains("leadid")
                ));
            } catch (TimeoutException ignored) {}
            waitForJsAndJquery();
        } catch (TimeoutException e) {
            throw new RuntimeException("E-Stamped Agreement not found/clickable", e);
        }
    }

    public boolean isEStampedPageDisplayed() {
        try {
            if (!driver.findElements(RentalAgreementLocator.E_STAMPED_PAGE).isEmpty()) return true;
            String url = driver.getCurrentUrl().toLowerCase();
            if (url.contains("e-stamp") || url.contains("estamp") || url.contains("/contract") || url.contains("leadid")) return true;
            boolean found = !driver.findElements(By.xpath("//h1[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'e-stamp') or contains(.,'E-Stamped') or contains(.,'E Stamped')]")).isEmpty();
            if (found) return true;
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    // -------------------- Aadhaar E-Sign --------------------

    public void clickAadhaarEsignAgreement() {
        try {
            dismissGenericModalIfPresent();
            WebElement el = wait.until(ExpectedConditions.presenceOfElementLocated(RentalAgreementLocator.AADHAAR_ESIGN));
            try { wait.until(ExpectedConditions.elementToBeClickable(RentalAgreementLocator.AADHAAR_ESIGN)).click(); }
            catch (Exception e) { ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el); }

            switchToNewWindowIfOpened();
            try {
                WebDriverWait pageWait = new WebDriverWait(driver, Duration.ofSeconds(8));
                pageWait.until(ExpectedConditions.or(
                    ExpectedConditions.presenceOfElementLocated(RentalAgreementLocator.AADHAAR_ESIGN_PAGE),
                    ExpectedConditions.urlContains("/contract"),
                    ExpectedConditions.urlContains("leadid")
                ));
            } catch (TimeoutException ignored) {}
            waitForJsAndJquery();
        } catch (TimeoutException e) {
            throw new RuntimeException("Aadhaar E-Sign not found/clickable", e);
        }
    }

    public boolean isAadhaarEsignPageDisplayed() {
        try {
            if (!driver.findElements(RentalAgreementLocator.AADHAAR_ESIGN_PAGE).isEmpty()) return true;
            String url = driver.getCurrentUrl().toLowerCase();
            if (url.contains("/contract") || url.contains("leadid") || url.contains("nbfr") || url.contains("contract?")) {
                return true;
            }
            if (!driver.findElements(By.xpath("//*[@id='modalContent' or contains(@class,'contract') or contains(@id,'contract')]")).isEmpty()) {
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    // -------------------- Your Ongoing Agreements --------------------

    public void clickYourOngoingAgreements() {
        try {
            dismissGenericModalIfPresent();
            WebElement el = wait.until(ExpectedConditions.presenceOfElementLocated(RentalAgreementLocator.YOUR_ONGOING_AGREEMENTS));
            try { wait.until(ExpectedConditions.elementToBeClickable(RentalAgreementLocator.YOUR_ONGOING_AGREEMENTS)).click(); }
            catch (Exception e) { ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el); }

            switchToNewWindowIfOpened();

            try {
                WebDriverWait pageWait = new WebDriverWait(driver, Duration.ofSeconds(10));
                pageWait.until(ExpectedConditions.or(
                        ExpectedConditions.presenceOfElementLocated(RentalAgreementLocator.YOUR_ONGOING_PAGE),
                        ExpectedConditions.urlContains("/contract"),
                        ExpectedConditions.urlContains("leadid"),
                        ExpectedConditions.urlContains("ongoing")
                ));
            } catch (TimeoutException ignored) {}

            waitForJsAndJquery();
        } catch (TimeoutException e) {
            throw new RuntimeException("Your Ongoing Agreements not found/clickable", e);
        }
    }

    public boolean isOngoingAgreementsPageDisplayed() {
        try {
            if (!driver.findElements(RentalAgreementLocator.YOUR_ONGOING_PAGE).isEmpty()) return true;
            String url = driver.getCurrentUrl().toLowerCase();
            if (url.contains("/contract") || url.contains("leadid") || url.contains("ongoing") || url.contains("your-ongoing")) {
                return true;
            }
            if (!driver.findElements(By.xpath("//*[@id='modalContent' or contains(@class,'contract') or contains(@id,'contract') or contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'ongoing')]")).isEmpty()) {
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    // -------------------- helpers --------------------

    private void dismissGenericModalIfPresent() {
        try {
            WebDriverWait waitShort = new WebDriverWait(driver, Duration.ofSeconds(3));
            try {
                WebElement svgClose = waitShort.until(ExpectedConditions.elementToBeClickable(RentalAgreementLocator.GENERIC_MODAL_CLOSE_SVG_EXACT));
                try { svgClose.click(); } catch (Exception ex) { ((JavascriptExecutor) driver).executeScript("arguments[0].click();", svgClose); }
                waitForJsAndJquery();
                return;
            } catch (TimeoutException ignored) {}

            try {
                WebElement svgClose2 = waitShort.until(ExpectedConditions.elementToBeClickable(RentalAgreementLocator.GENERIC_MODAL_CLOSE_SVG));
                try { svgClose2.click(); } catch (Exception ex) { ((JavascriptExecutor) driver).executeScript("arguments[0].click();", svgClose2); }
                waitForJsAndJquery();
                return;
            } catch (TimeoutException ignored) {}

            try {
                WebElement closeBtn = waitShort.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[aria-label='Close'], button.close, .nb__close")));
                try { closeBtn.click(); } catch (Exception ex) { ((JavascriptExecutor) driver).executeScript("arguments[0].click();", closeBtn); }
                waitForJsAndJquery();
            } catch (TimeoutException ignored) {}
        } catch (Exception ignored) {
        }
    }

    private void handleNoEligibleAgreementModal() {
        try {
            WebDriverWait waitShort = new WebDriverWait(driver, Duration.ofSeconds(6));

            By closeExact = RentalAgreementLocator.GENERIC_MODAL_CLOSE_SVG_EXACT;
            By closeGeneric = RentalAgreementLocator.GENERIC_MODAL_CLOSE_SVG;

            try {
                WebElement modalRoot = waitShort.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id='modalContent']")));
                String modalText = modalRoot.getText().trim();
            } catch (Exception e) {
                // ignore
            }

            try {
                if (!driver.findElements(closeExact).isEmpty()) {
                    WebElement svgClose = waitShort.until(ExpectedConditions.elementToBeClickable(closeExact));
                    try { svgClose.click(); } catch (Exception ex) { ((JavascriptExecutor) driver).executeScript("arguments[0].click();", svgClose); }
                } else if (!driver.findElements(closeGeneric).isEmpty()) {
                    WebElement svgClose2 = waitShort.until(ExpectedConditions.elementToBeClickable(closeGeneric));
                    try { svgClose2.click(); } catch (Exception ex) { ((JavascriptExecutor) driver).executeScript("arguments[0].click();", svgClose2); }
                } else {
                    By fallbackBtn = By.cssSelector("button[aria-label='Close'], button.close, .nb__close");
                    if (!driver.findElements(fallbackBtn).isEmpty()) {
                        WebElement fb = waitShort.until(ExpectedConditions.elementToBeClickable(fallbackBtn));
                        try { fb.click(); } catch (Exception ex) { ((JavascriptExecutor) driver).executeScript("arguments[0].click();", fb); }
                    } else {
                        try { new Actions(driver).sendKeys(Keys.ESCAPE).perform(); } catch (Exception ignored) {}
                    }
                }

                WebDriverWait disappearWait = new WebDriverWait(driver, Duration.ofSeconds(6));
                disappearWait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//*[@id='modalContent']")));
            } catch (TimeoutException te) {
                // ignore
            }
        } catch (Exception e) {
            // ignore
        }
    }

    private void switchToNewWindowIfOpened() {
        try {
            String original = driver.getWindowHandle();
            long deadline = System.currentTimeMillis() + 2000;
            while (System.currentTimeMillis() < deadline) {
                Set<String> handles = driver.getWindowHandles();
                if (handles.size() > 1) break;
                sleepMillis(150);
            }

            Set<String> handles = driver.getWindowHandles();
            if (handles.size() > 1) {
                for (String h : handles) {
                    if (!h.equals(original)) {
                        driver.switchTo().window(h);
                        sleepMillis(300);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            // ignore
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
