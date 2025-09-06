package pages;

import java.time.Duration;
import java.util.List;

import objectrepository.CareersLocator;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;

/**
 * CareersPage - robust navigation + scrolling for Careers smoke test.
 */
public class CareersPage {
    private final WebDriver driver;
    private final WebDriverWait wait;
    private final ExtentTest extTest;

    public CareersPage(WebDriver driver, ExtentTest extTest) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        this.extTest = extTest;
    }

    // Robust click helper (normal -> Actions -> JS)
    private void robustClick(By locator, String name) {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(locator));
            WebElement el = driver.findElement(locator);
            try {
                el.click();
                extTest.log(Status.PASS, "Clicked: " + name);
                return;
            } catch (WebDriverException e1) {
                try {
                    new Actions(driver).moveToElement(el).click().perform();
                    extTest.log(Status.PASS, "Clicked via Actions: " + name);
                    return;
                } catch (Exception e2) {
                    try {
                        JavascriptExecutor js = (JavascriptExecutor) driver;
                        js.executeScript("arguments[0].scrollIntoView(true); arguments[0].click();", el);
                        extTest.log(Status.PASS, "Clicked via JS: " + name);
                        return;
                    } catch (Exception jsEx) {
                        extTest.log(Status.FAIL, "JS click failed for " + name + ": " + jsEx.getMessage());
                        throw jsEx;
                    }
                }
            }
        } catch (Exception ex) {
            extTest.log(Status.FAIL, "Failed to click " + name + ": " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    // Open the top-right menu and wait for the menu container to be visible
    public void openMenu() {
        try {
            robustClick(CareersLocator.MENU_BUTTON, "Top-right Menu");
        } catch (Exception e) {
            // robustClick will have logged a FAIL if it truly failed; continue best-effort
            extTest.log(Status.INFO, "openMenu() robustClick threw: " + e.getMessage());
        }

        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(CareersLocator.MENU_CONTAINER));
            extTest.log(Status.INFO, "Menu container is visible.");
        } catch (Exception e) {
            // still proceed because some pages render menu differently — log info
            extTest.log(Status.INFO, "Menu container did not become visible within timeout: " + e.getMessage());
        }
    }

    /**
     * Click Careers link.
     * Strategy:
     * 1) Ensure menu is open (best-effort)
     * 2) Try primary locator but tolerate elementToBeClickable failures by doing a presence+JS fallback
     * 3) Try exact text search scoped to menu
     * 4) Final fallback: contains('career') locator via robustClick
     */
    public void clickCareers() {
        // Ensure menu is open (best-effort)
        try {
            openMenu();
        } catch (Exception e) {
            extTest.log(Status.INFO, "openMenu() raised an exception but continuing: " + e.getMessage());
        }

        // 1) Try primary locator but be tolerant: if elementToBeClickable fails, try visibility + JS click
        try {
            try {
                // try clickable normally
                wait.until(ExpectedConditions.elementToBeClickable(CareersLocator.CAREERS_LINK));
                WebElement primary = driver.findElement(CareersLocator.CAREERS_LINK);
                try {
                    primary.click();
                    extTest.log(Status.INFO, "Clicked primary careers link.");
                } catch (Exception clickEx) {
                    // fallback to Actions then JS (mirror robustClick behavior without failing the primary attempt)
                    try {
                        new Actions(driver).moveToElement(primary).click().perform();
                        extTest.log(Status.INFO, "Clicked primary careers link via Actions.");
                    } catch (Exception actionEx) {
                        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true); arguments[0].click();", primary);
                        extTest.log(Status.INFO, "Clicked primary careers link via JS.");
                    }
                }
                // wait for careers page heading
                wait.until(ExpectedConditions.visibilityOfElementLocated(CareersLocator.CAREERS_PAGE));
                return;
            } catch (Exception eClickable) {
                // elementToBeClickable timed out or was not clickable — attempt presence/visibility + JS click BEFORE marking primary as failed
                extTest.log(Status.INFO, "Primary clickable wait failed; attempting gentle JS/presence fallback: " + eClickable.getMessage());
                try {
                    WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(6));
                    WebElement candidate = shortWait.until(d -> {
                        List<WebElement> els = d.findElements(CareersLocator.CAREERS_LINK);
                        return (els != null && !els.isEmpty()) ? els.get(0) : null;
                    });

                    if (candidate != null) {
                        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", candidate);
                        try { candidate.click(); extTest.log(Status.INFO, "Clicked primary careers link (presence fallback)."); }
                        catch (Exception c2) {
                            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", candidate);
                            extTest.log(Status.INFO, "Clicked primary careers link via JS (presence fallback).");
                        }
                        wait.until(ExpectedConditions.visibilityOfElementLocated(CareersLocator.CAREERS_PAGE));
                        return;
                    }
                } catch (Exception presenceEx) {
                    extTest.log(Status.INFO, "Primary presence/JS fallback did not find/click element: " + presenceEx.getMessage());
                    // fall through to the existing exact-text attempt and fallback below
                }
            }
        } catch (Exception primaryEx) {
            extTest.log(Status.INFO, "Primary careers link failed (final): " + primaryEx.getMessage());
        }

        // 2) Try to locate exact menu-scoped link text inside menu container (less brittle than absolute index)
        try {
            WebElement menuContainer = null;
            try {
                menuContainer = wait.until(ExpectedConditions.visibilityOfElementLocated(CareersLocator.MENU_CONTAINER));
            } catch (Exception e) {
                extTest.log(Status.INFO, "Menu container not visible when searching for careers link: " + e.getMessage());
            }

            By exactCareersInMenu;
            if (menuContainer != null) {
                exactCareersInMenu = By.xpath("//*[@id='main-menu']//a[normalize-space(text())='Careers' or translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')='careers']");
            } else {
                exactCareersInMenu = By.xpath("//a[normalize-space(text())='Careers' or translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')='careers']");
            }

            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(8));
            List<WebElement> candidates = shortWait.until(d -> {
                List<WebElement> els = d.findElements(exactCareersInMenu);
                return (els != null && !els.isEmpty()) ? els : null;
            });

            if (candidates != null && !candidates.isEmpty()) {
                WebElement el = candidates.get(0);
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({behavior:'smooth', block:'center'});", el);
                try {
                    el.click();
                    extTest.log(Status.PASS, "Clicked Careers link (exact text) inside menu.");
                } catch (Exception eClick) {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
                    extTest.log(Status.PASS, "Clicked Careers link via JS (exact text).");
                }
                wait.until(ExpectedConditions.visibilityOfElementLocated(CareersLocator.CAREERS_PAGE));
                return;
            }
        } catch (Exception exactEx) {
            extTest.log(Status.INFO, "Exact-text careers link attempt failed: " + exactEx.getMessage());
        }

        // 3) Final fallback: contains('career') locator
        try {
            robustClick(CareersLocator.CAREERS_LINK_FALLBACK, "Careers link (fallback contains)");
            wait.until(ExpectedConditions.visibilityOfElementLocated(CareersLocator.CAREERS_PAGE));
            extTest.log(Status.INFO, "Clicked fallback careers link.");
            return;
        } catch (Exception fallbackEx) {
            extTest.log(Status.FAIL, "All attempts to click Careers link failed: " + fallbackEx.getMessage());
            throw new RuntimeException(fallbackEx);
        }
    }

    // Check careers page visibility
    public boolean isCareersPageDisplayed() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(CareersLocator.CAREERS_PAGE));
            return true;
        } catch (TimeoutException te) {
            return false;
        }
    }

    // Scroll helpers and visibility checks (unchanged)
    public void scrollBy(int pixels) {
        try {
            ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, arguments[0]);", pixels);
            extTest.log(Status.INFO, "Scrolled by " + pixels + " pixels");
            try { sleepMillis(300); } catch (Exception ignored) {}
        } catch (Exception e) {
            extTest.log(Status.INFO, "scrollBy failed: " + e.getMessage());
        }
    }

    public void scrollToHowDidWeStart() {
        try {
            try {
                WebElement heading = wait.until(ExpectedConditions.visibilityOfElementLocated(CareersLocator.HOW_DID_WE_START_HEADING));
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({behavior:'smooth', block:'center'});", heading);
                extTest.log(Status.INFO, "Scrolled to 'How did we Start' heading.");
                try { sleepMillis(500); } catch (Exception ignored) {}
                return;
            } catch (Exception ignored) {
                extTest.log(Status.INFO, "'How did we Start' heading not found, trying paragraph fallback.");
            }

            try {
                WebElement para = wait.until(ExpectedConditions.visibilityOfElementLocated(CareersLocator.HOW_DID_WE_START_PARAGRAPH));
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({behavior:'smooth', block:'center'});", para);
                extTest.log(Status.INFO, "Scrolled to 'How did we Start' paragraph (fallback).");
                try { sleepMillis(500); } catch (Exception ignored) {}
                return;
            } catch (Exception ignored) {
                extTest.log(Status.INFO, "Paragraph fallback not found.");
            }

            scrollBy(900);
            extTest.log(Status.INFO, "Used fallback large scroll.");
        } catch (Exception ex) {
            extTest.log(Status.FAIL, "scrollToHowDidWeStart failed: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    public boolean isHowDidWeStartVisible() {
        try {
            WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(CareersLocator.HOW_DID_WE_START_HEADING));
            Object fullyVisible = ((JavascriptExecutor) driver).executeScript(
                    "var rect = arguments[0].getBoundingClientRect();" +
                    "return (rect.top >= 0 && rect.bottom <= (window.innerHeight || document.documentElement.clientHeight));",
                    element);
            if (fullyVisible instanceof Boolean && (Boolean) fullyVisible) {
                extTest.log(Status.INFO, "'How did we Start' heading is fully visible.");
                return true;
            }
            Object partiallyVisible = ((JavascriptExecutor) driver).executeScript(
                    "var rect = arguments[0].getBoundingClientRect();" +
                    "return !(rect.bottom < 0 || rect.top > (window.innerHeight || document.documentElement.clientHeight));",
                    element);
            if (partiallyVisible instanceof Boolean && (Boolean) partiallyVisible) {
                extTest.log(Status.INFO, "'How did we Start' heading is at least partially visible.");
                return true;
            }
            extTest.log(Status.INFO, "'How did we Start' heading not visible in viewport.");
            return false;
        } catch (Exception e) {
            extTest.log(Status.INFO, "isHowDidWeStartVisible check failed (heading not found): " + e.getMessage());
            try {
                WebElement para = driver.findElement(CareersLocator.HOW_DID_WE_START_PARAGRAPH);
                if (para.isDisplayed()) {
                    extTest.log(Status.INFO, "'How did we Start' paragraph is visible (fallback).");
                    return true;
                }
            } catch (Exception ignored) {}
            return false;
        }
    }

    public void navigateBackToCareers() {
        try {
            robustClick(CareersLocator.BACK_TO_CAREERS, "Back to Careers");
        } catch (Exception e) {
            driver.navigate().back();
        }
        wait.until(ExpectedConditions.visibilityOfElementLocated(CareersLocator.CAREERS_PAGE));
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
