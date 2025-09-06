package pages;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import objectrepository.ProfileLocator;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class ProfilePage {
    private final WebDriver driver;
    private final WebDriverWait wait;
    private final ExtentTest extTest;

    // small helper waits with explicit durations so we don't repeatedly create them
    private final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(15);
    private final Duration SHORT_TIMEOUT = Duration.ofSeconds(6);

    public ProfilePage(WebDriver driver, ExtentTest extTest) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, DEFAULT_TIMEOUT);
        this.extTest = extTest;
    }

    private void safeClick(By locator, String name) {
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, SHORT_TIMEOUT);
            shortWait.until(ExpectedConditions.presenceOfElementLocated(locator));
            try {
                wait.until(ExpectedConditions.elementToBeClickable(locator));
            } catch (Exception ignored) {
                // we'll still try clicking even if elementToBeClickable timed out (it may be visible but overlapped)
            }

            WebElement el = driver.findElement(locator);
            try {
                el.click();
                extTest.log(Status.PASS, "Clicked: " + name);
            } catch (WebDriverException e) {
                // fallback to JS click when normal click fails
                try {
                    ((JavascriptExecutor) driver)
                            .executeScript("arguments[0].scrollIntoView(true); arguments[0].click();", el);
                    extTest.log(Status.PASS, "Clicked via JS fallback: " + name);
                } catch (Exception jsEx) {
                    extTest.log(Status.WARNING, "JS fallback click also failed for " + name + ": " + jsEx.getMessage());
                    throw jsEx;
                }
            }
        } catch (Exception e) {
            extTest.log(Status.FAIL, "Failed to click " + name + ": " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Open profile from top menu / avatar. Uses a final local `beforeUrl` (safe for lambdas).
     * Adds small retry + scroll behavior if left nav is missing.
     */
    public void openProfileFromMenu() {
        // capture current URL into a final variable (allowed in lambda)
        String beforeUrlTemp = "";
        try {
            beforeUrlTemp = driver.getCurrentUrl();
        } catch (Exception ignored) {
        }
        final String beforeUrl = beforeUrlTemp; // now effectively-final for lambdas

        boolean dropdownOpened = false;

        // 1) Try primary profile icon
        try {
            safeClick(ProfileLocator.PROFILE_ICON, "Profile icon");
            dropdownOpened = true;
        } catch (Exception e) {
            extTest.log(Status.WARNING, "Primary profile icon click failed: " + e.getMessage());
            // 2) Try alternate username element if profile icon fails
            try {
                safeClick(ProfileLocator.PROFILE_USERNAME_ALT, "Profile username alt");
                dropdownOpened = true;
            } catch (Exception altEx) {
                extTest.log(Status.WARNING, "Alternate profile opener failed: " + altEx.getMessage());
            }
        }

        // 3) Click Profile link inside dropdown (if dropdown opened)
        if (dropdownOpened) {
            try {
                WebDriverWait shortWait = new WebDriverWait(driver, SHORT_TIMEOUT);
                shortWait.until(ExpectedConditions.presenceOfElementLocated(ProfileLocator.PROFILE_LINK));
                safeClick(ProfileLocator.PROFILE_LINK, "Profile link (dropdown)");
            } catch (Exception e) {
                extTest.log(Status.INFO, "Profile link not clickable from dropdown; trying direct profile link click. reason: " + e.getMessage());
                try {
                    safeClick(ProfileLocator.PROFILE_LINK, "Profile link (direct attempt)");
                } catch (Exception finalLinkEx) {
                    extTest.log(Status.WARNING, "Could not click Profile link after opening dropdown: " + finalLinkEx.getMessage());
                }
            }
        } else {
            // fallback: try direct click of the profile link (if visible somewhere)
            try {
                safeClick(ProfileLocator.PROFILE_LINK, "Profile link (direct fallback)");
            } catch (Exception e) {
                extTest.log(Status.FAIL, "Unable to open profile - no dropdown and direct link click failed: " + e.getMessage());
                throw new RuntimeException("Unable to open profile page", e);
            }
        }

        // Load detection: try several heuristics but also retry a couple times with small scrolls if nothing found.
        try {
            boolean loaded = wait.until(d -> {
                try {
                    if (!d.findElements(ProfileLocator.LEFT_NAV_ITEMS).isEmpty()) return true;
                    if (!d.findElements(ProfileLocator.LEFT_NAV_ITEMS_ALT).isEmpty()) return true;
                    if (!d.findElements(ProfileLocator.MAIN_HEADING).isEmpty()) return true;
                    if (ProfileLocator.PROFILE_PAGE_CONTAINER != null && !d.findElements(ProfileLocator.PROFILE_PAGE_CONTAINER).isEmpty())
                        return true;
                    String cur = d.getCurrentUrl();
                    if (cur != null && !cur.equals(beforeUrl)) {
                        String lower = cur.toLowerCase();
                        if (lower.contains("profile") || lower.contains("account") || lower.contains("user")) return true;
                    }
                } catch (Exception ignored) {
                }
                return false;
            });

            if (loaded) {
                extTest.log(Status.PASS, "Profile page loaded successfully.");
                return;
            }
        } catch (TimeoutException te) {
            extTest.log(Status.INFO, "Initial profile-load heuristics failed; attempting gentle retries/scrolls.");
        }

        // gentle retry: scroll a bit, wait, re-check nav items / headings
        try {
            for (int i = 0; i < 2; i++) {
                try {
                    ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, 200);");
                } catch (Exception ignored) {}
                // replaced Thread.sleep(400) with sleepMillis(400)
                try { sleepMillis(400); } catch (Exception ignored) {}
                try {
                    if (!driver.findElements(ProfileLocator.LEFT_NAV_ITEMS).isEmpty() ||
                        !driver.findElements(ProfileLocator.LEFT_NAV_ITEMS_ALT).isEmpty() ||
                        !driver.findElements(ProfileLocator.MAIN_HEADING).isEmpty()) {
                        extTest.log(Status.PASS, "Profile page detected after retry/scroll attempt " + (i+1));
                        return;
                    }
                } catch (Exception ignored) {}
            }
        } catch (Exception exRetry) {
            extTest.log(Status.INFO, "Retry/scroll attempts had an exception: " + exRetry.getMessage());
        }

        // final fallback: capture debug artifacts and try an extended scan to salvage nav items
        extTest.log(Status.WARNING, "Profile page heuristics failed; capturing debug artifacts and performing extended nav scan.");
        captureDebugArtifacts("profile-load-failure");
        List<String> scanned = extendedScanForNavItems();
        if (!scanned.isEmpty()) {
            extTest.log(Status.INFO, "Extended scan discovered left-menu-like items: " + scanned);
            return;
        }

        // nothing worked -> fail loudly (keeps original behavior)
        String currentUrl = "";
        try {
            currentUrl = driver.getCurrentUrl();
        } catch (Exception ignored) {}
        extTest.log(Status.FAIL, "Profile page did not load. Current URL: " + currentUrl);
        throw new TimeoutException("Profile page load heuristics did not pass (and extended scan failed).");
    }

    /**
     * Collect left navigation items text. Tries multiple strategies; if none found captures debug artifacts.
     */
    public List<String> getLeftMenuItemsText() {
        Set<String> items = new HashSet<>();

        // 1) Quick immediate read (no wait) - fast path for already-rendered pages
        try {
            List<WebElement> immediate = driver.findElements(ProfileLocator.LEFT_NAV_ITEMS);
            if (immediate.isEmpty()) immediate = driver.findElements(ProfileLocator.LEFT_NAV_ITEMS_ALT);
            if (!immediate.isEmpty()) {
                for (WebElement e : immediate) {
                    String txt = safeGetText(e);
                    if (!txt.isEmpty()) items.add(txt);
                }
                extTest.log(Status.INFO, "Left nav items (immediate): " + items);
                return new ArrayList<>(items);
            }
        } catch (Exception ignoreImmediate) {
            // continue to next strategy
        }

        // 2) Presence wait for nav items or main heading (short wait)
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, SHORT_TIMEOUT);
            shortWait.until(d -> {
                boolean foundNav = !d.findElements(ProfileLocator.LEFT_NAV_ITEMS).isEmpty()
                        || !d.findElements(ProfileLocator.LEFT_NAV_ITEMS_ALT).isEmpty();
                boolean foundHeading = !d.findElements(ProfileLocator.MAIN_HEADING).isEmpty();
                return foundNav || foundHeading;
            });

            List<WebElement> els = driver.findElements(ProfileLocator.LEFT_NAV_ITEMS);
            if (els.isEmpty()) els = driver.findElements(ProfileLocator.LEFT_NAV_ITEMS_ALT);
            for (WebElement e : els) {
                String txt = safeGetText(e);
                if (!txt.isEmpty()) items.add(txt);
            }
            if (!items.isEmpty()) {
                extTest.log(Status.INFO, "Left nav items (short-wait): " + items);
                return new ArrayList<>(items);
            }
        } catch (Exception shortEx) {
            extTest.log(Status.INFO, "Short presence wait did not produce left-nav items: " + shortEx.getMessage());
        }

        // 3) Anchor / nav scan (extended): role='navigation', aria-label, sidebar/menu classes, buttons
        List<String> scanned = extendedScanForNavItems();
        if (!scanned.isEmpty()) {
            extTest.log(Status.INFO, "Left nav items (extended scan): " + scanned);
            return scanned;
        }

        // 4) Nothing found — capture debug artifacts for investigation
        extTest.log(Status.WARNING, "No left-nav items or main heading found on profile page. Capturing debug artifacts.");
        captureDebugArtifacts("left-nav-missing");
        return new ArrayList<>(items);
    }

    // Extended scan that tries several xpath patterns to find nav-like labels
    private List<String> extendedScanForNavItems() {
        Set<String> items = new HashSet<>();
        try {
            String[] xpaths = new String[] {
                "//*[@role='navigation']//a[normalize-space()]",
                "//*[contains(@aria-label,'nav')]//a[normalize-space()]",
                "//nav//a[normalize-space()]",
                "//aside//a[normalize-space()]",
                "//div[contains(translate(@class,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sidebar')]//a[normalize-space()]",
                "//div[contains(translate(@class,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'menu')]//a[normalize-space()]",
                "//button[contains(@class,'nav') or contains(@aria-label,'nav') or contains(@data-test,'nav')]",
                "//a[contains(@href,'/profile') or contains(., 'Profile') or contains(., 'Account') or contains(., 'Settings')]"
            };

            for (String xp : xpaths) {
                try {
                    List<WebElement> els = driver.findElements(By.xpath(xp));
                    for (WebElement e : els) {
                        String txt = safeGetText(e);
                        if (txt.isEmpty()) {
                            // try alt attributes if link has aria-label
                            try {
                                String aria = e.getAttribute("aria-label");
                                if (aria != null && !aria.trim().isEmpty()) txt = aria.trim();
                            } catch (Exception ignored) {}
                        }
                        if (!txt.isEmpty() && looksLikeNavText(txt)) items.add(txt);
                    }
                } catch (Exception xpEx) {
                    // ignore single-xpath failures and continue
                }
            }

            // also consider links with hrefs that contain 'profile' or 'account'
            try {
                List<WebElement> profileLinks = driver.findElements(By.xpath("//a[contains(translate(@href,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'profile') or contains(translate(@href,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'account')]"));
                for (WebElement e : profileLinks) {
                    String txt = safeGetText(e);
                    if (txt.isEmpty()) {
                        String aria = e.getAttribute("aria-label");
                        if (aria != null && !aria.trim().isEmpty()) txt = aria.trim();
                    }
                    if (!txt.isEmpty() && looksLikeNavText(txt)) items.add(txt);
                }
            } catch (Exception ignored) {}
        } catch (Exception e) {
            extTest.log(Status.INFO, "extendedScanForNavItems encountered exception: " + e.getMessage());
        }
        return new ArrayList<>(items);
    }

    // Helper to safely get trimmed text from an element
    private String safeGetText(WebElement e) {
        try {
            String txt = e.getText();
            if (txt == null) txt = "";
            txt = txt.trim();
            if (!txt.isEmpty()) return txt;
        } catch (Exception ignored) {}
        try {
            String alt = e.getAttribute("innerText");
            if (alt != null) return alt.trim();
        } catch (Exception ignored) {}
        try {
            String aria = e.getAttribute("aria-label");
            if (aria != null) return aria.trim();
        } catch (Exception ignored) {}
        return "";
    }

    // Heuristic to filter only meaningful nav labels
    private boolean looksLikeNavText(String txt) {
        if (txt == null) return false;
        txt = txt.trim();
        if (txt.length() < 2) return false;
        String lower = txt.toLowerCase();
        // filter out common noisy short labels (customize to your app)
        if (lower.matches("^\\d+$")) return false;
        if (lower.contains("©") || lower.contains("privacy") || lower.contains("terms") || lower.contains("cookie")) return false;
        // ignore very long text that is probably a paragraph
        if (txt.length() > 120) return false;
        return true;
    }

    /**
     * Capture screenshot and page source to ./test-artifacts/ with timestamped filenames.
     * Logs file paths (does not throw).
     */
    private void captureDebugArtifacts(String tag) {
        try {
            String ts = new SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(new Date());
            Path base = Paths.get("test-artifacts");
            if (!Files.exists(base)) Files.createDirectories(base);

            // screenshot
            try {
                if (driver instanceof TakesScreenshot) {
                    File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                    Path dest = base.resolve(tag + "_" + ts + ".png");
                    Files.copy(src.toPath(), dest);
                    extTest.log(Status.INFO, "Saved screenshot: " + dest.toAbsolutePath().toString());
                } else {
                    extTest.log(Status.INFO, "Driver does not support screenshots.");
                }
            } catch (IOException ioe) {
                extTest.log(Status.WARNING, "Failed to save screenshot: " + ioe.getMessage());
            } catch (WebDriverException wde) {
                extTest.log(Status.WARNING, "WebDriver screenshot error: " + wde.getMessage());
            }

            // page source
            try {
                String pageSource = driver.getPageSource();
                Path srcFile = base.resolve(tag + "_" + ts + ".html");
                Files.writeString(srcFile, pageSource);
                extTest.log(Status.INFO, "Saved page source: " + srcFile.toAbsolutePath().toString());
            } catch (IOException ioe) {
                extTest.log(Status.WARNING, "Failed to save page source: " + ioe.getMessage());
            } catch (WebDriverException wde) {
                extTest.log(Status.WARNING, "WebDriver pageSource error: " + wde.getMessage());
            }
        } catch (Exception e) {
            extTest.log(Status.INFO, "captureDebugArtifacts encountered exception: " + e.getMessage());
        }
    }

    /**
     * Click a left-menu item by visible text. Tries exact, contains, then a fallback scan.
     * Returns true when the target section appears (by heading or main heading), false otherwise.
     */
    public boolean navigateToLeftMenuItem(String itemText) {
        try {
            String escaped = itemText.replace("\"", "\\\"");
            By itemLocatorExact = By.xpath(String.format("//a[normalize-space() = \"%s\"]", escaped));
            By itemLocatorContains = By.xpath(String.format("//a[contains(normalize-space(.), \"%s\")]", escaped));

            WebElement el = null;
            try {
                WebDriverWait shortWait = new WebDriverWait(driver, SHORT_TIMEOUT);
                shortWait.until(ExpectedConditions.presenceOfElementLocated(itemLocatorExact));
                el = driver.findElement(itemLocatorExact);
            } catch (Exception exExact) {
                try {
                    WebDriverWait shortWait = new WebDriverWait(driver, SHORT_TIMEOUT);
                    shortWait.until(ExpectedConditions.presenceOfElementLocated(itemLocatorContains));
                    el = driver.findElement(itemLocatorContains);
                } catch (Exception exContains) {
                    // fallback: scan left nav items for a close match
                    List<String> available = getLeftMenuItemsText();
                    for (String candidate : available) {
                        if (candidate.equalsIgnoreCase(itemText) || candidate.toLowerCase().contains(itemText.toLowerCase())) {
                            // try to click element by its visible text
                            try {
                                el = driver.findElement(By.xpath(String.format("//a[normalize-space() = \"%s\"]", candidate.replace("\"", "\\\""))));
                                break;
                            } catch (Exception ignored) {}
                        }
                    }
                }
            }

            if (el == null) {
                extTest.log(Status.WARNING, "Could not locate left-menu item element for '" + itemText + "'");
                // optionally capture debug artifacts to help diagnose missing item
                captureDebugArtifacts("nav-item-missing-" + sanitizeForFileName(itemText));
                return false;
            }

            try {
                el.click();
            } catch (WebDriverException clickEx) {
                try {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true); arguments[0].click();", el);
                } catch (Exception jsEx) {
                    extTest.log(Status.WARNING, "Could not click left-menu item '" + itemText + "': " + jsEx.getMessage());
                    captureDebugArtifacts("nav-item-click-failed-" + sanitizeForFileName(itemText));
                    return false;
                }
            }

            // Wait for expected heading for this item OR a main heading presence
            By heading = ProfileLocator.headingFor(itemText);
            try {
                WebDriverWait shortWait = new WebDriverWait(driver, SHORT_TIMEOUT);
                shortWait.until(ExpectedConditions.or(
                        ExpectedConditions.visibilityOfElementLocated(heading),
                        ExpectedConditions.visibilityOfElementLocated(ProfileLocator.MAIN_HEADING)
                ));
                extTest.log(Status.PASS, "Section visible after clicking '" + itemText + "'");
                return true;
            } catch (Exception waitEx) {
                extTest.log(Status.WARNING, "No explicit heading found for '" + itemText + "' after click: " + waitEx.getMessage());
                return false;
            }
        } catch (Exception e) {
            extTest.log(Status.FAIL, "Failed to navigate to left menu item '" + itemText + "': " + e.getMessage());
            return false;
        }
    }

    // small helper to produce file-safe tag
    private String sanitizeForFileName(String s) {
        if (s == null) return "null";
        return s.replaceAll("[^a-zA-Z0-9-_\\.]", "_");
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
