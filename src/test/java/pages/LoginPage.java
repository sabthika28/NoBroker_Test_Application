package pages;

import java.time.Duration;
import java.util.List;
import java.util.Scanner;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;

import objectrepository.LoginLocator;
import utils.ClickHelper;

public class LoginPage {

    WebDriver driver;
    WebDriverWait wait;
    ExtentTest extTest;

    // small and default timeouts used in methods
    private final Duration DEFAULT_WAIT = Duration.ofSeconds(10);
    private final Duration SHORT_WAIT = Duration.ofSeconds(3);

    public LoginPage(WebDriver driver, ExtentTest extTest) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, DEFAULT_WAIT);
        this.extTest = extTest;
    }

    /**
     * Robust click wrapper that uses ClickHelper to try multiple fallbacks quietly.
     * Only logs final PASS/FAIL to Extent.
     */
    private void robustClick(By target, String actionDescription) {
        try {
            // 1) short wait for backdrop to disappear (if it exists)
            try {
                WebDriverWait shortWait = new WebDriverWait(driver, SHORT_WAIT);
                shortWait.until(ExpectedConditions.invisibilityOfElementLocated(LoginLocator.loginBackdrop));
            } catch (Exception ignored) {
                // ignore - proceed to attempts
            }

            // 2) wait until the element is present/visible/clickable
            wait.until(ExpectedConditions.presenceOfElementLocated(target));
            WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(target));

            // scroll into view to reduce click-intercepted cases
            try {
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center', inline:'center'});", el);
            } catch (Exception ignored) {}

            // 3) use central ClickHelper (pass null so helper does not log to Extent)
            boolean clicked = ClickHelper.safeClick(driver, el, null);

            if (clicked) {
                extTest.log(Status.PASS, "Clicked " + actionDescription + ".");
                return;
            } else {
                // final fallback: try JS click (last resort) then treat as failure if still no success
                try {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
                    extTest.log(Status.PASS, "Clicked " + actionDescription + " using JS fallback.");
                    return;
                } catch (Exception jsEx) {
                    extTest.log(Status.FAIL, "Failed to click " + actionDescription + " after retries: " + jsEx.getMessage());
                    throw new RuntimeException("Failed to click " + actionDescription, jsEx);
                }
            }
        } catch (TimeoutException te) {
            extTest.log(Status.FAIL, "Timed out waiting to click " + actionDescription + ": " + te.getMessage());
            throw te;
        } catch (StaleElementReferenceException stale) {
            // Stale - try one retry cycle (find element again and use ClickHelper)
            try {
                WebElement el2 = wait.until(ExpectedConditions.visibilityOfElementLocated(target));
                boolean retryClicked = ClickHelper.safeClick(driver, el2, null);
                if (retryClicked) {
                    extTest.log(Status.PASS, "Clicked " + actionDescription + " after stale retry.");
                    return;
                } else {
                    try {
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el2);
                        extTest.log(Status.PASS, "Clicked " + actionDescription + " using JS after stale retry.");
                        return;
                    } catch (Exception e) {
                        extTest.log(Status.FAIL, "Failed to click " + actionDescription + " after stale retry: " + e.getMessage());
                        throw new RuntimeException("Failed to click after stale retry", e);
                    }
                }
            } catch (Exception e) {
                extTest.log(Status.FAIL, "Failed to recover from stale element for " + actionDescription + ": " + e.getMessage());
                throw new RuntimeException("Stale recovery failed", e);
            }
        } catch (RuntimeException re) {
            extTest.log(Status.FAIL, "Failed to click " + actionDescription + ": " + re.getMessage());
            throw re;
        } catch (Exception e) {
            extTest.log(Status.FAIL, "Unexpected error while clicking " + actionDescription + ": " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Click the login trigger and ensure phone input is visible.
     * If phone input does not appear quickly, re-attempt click and wait longer.
     */
    public void clickLogin() {
        try {
            robustClick(LoginLocator.loginButton, "Login button");

            // After click, wait a bit longer for phone input (UI may animate)
            try {
                WebDriverWait postClickWait = new WebDriverWait(driver, Duration.ofSeconds(15));
                postClickWait.until(ExpectedConditions.visibilityOfElementLocated(LoginLocator.inputNumber));
                extTest.log(Status.PASS, "Clicked Login button and phone input is visible.");
            } catch (TimeoutException te) {
                // Recovery: re-click login and wait longer
                extTest.log(Status.INFO, "Phone input not visible after initial click - attempting recovery.");
                try {
                    robustClick(LoginLocator.loginButton, "Login button (recovery)");
                } catch (Exception clickEx) {
                    extTest.log(Status.INFO, "Re-clicking Login failed: " + clickEx.getMessage());
                }
                WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(25));
                longWait.until(ExpectedConditions.visibilityOfElementLocated(LoginLocator.inputNumber));
                extTest.log(Status.PASS, "Phone input visible after recovery click.");
            }
        } catch (Exception e) {
            extTest.log(Status.FAIL, "Failed to click Login button or show phone input: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Enter mobile number with recovery if input isn't visible.
     */
    public void enterMobileNumber(String mobile) {
        try {
            // Normal path: wait default then type
            wait.until(ExpectedConditions.visibilityOfElementLocated(LoginLocator.inputNumber));
            WebElement mobileInput = driver.findElement(LoginLocator.inputNumber);
            mobileInput.clear();
            mobileInput.sendKeys(mobile);
            extTest.log(Status.PASS, "Entered mobile number: " + mobile);
            return;
        } catch (TimeoutException firstWaitFailed) {
            extTest.log(Status.INFO, "Phone input not visible - attempting recovery. Will re-click Login and wait longer.");
            // Try recovery
            try {
                robustClick(LoginLocator.loginButton, "Login button (recovery from enterMobileNumber)");
            } catch (Exception clickEx) {
                extTest.log(Status.INFO, "Re-clicking Login during recovery failed: " + clickEx.getMessage());
            }
            try {
                WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(25));
                longWait.until(ExpectedConditions.visibilityOfElementLocated(LoginLocator.inputNumber));
                WebElement mobileInput = driver.findElement(LoginLocator.inputNumber);
                mobileInput.clear();
                mobileInput.sendKeys(mobile);
                extTest.log(Status.PASS, "Entered mobile number after recovery: " + mobile);
                return;
            } catch (TimeoutException secondWaitFailed) {
                extTest.log(Status.FAIL, "Phone input still not visible after recovery attempts: " + secondWaitFailed.getMessage());
                throw secondWaitFailed;
            } catch (Exception e) {
                extTest.log(Status.FAIL, "Unexpected error while entering mobile number during recovery: " + e.getMessage());
                throw e;
            }
        } catch (StaleElementReferenceException stale) {
            extTest.log(Status.INFO, "Stale element when entering mobile - retrying once.");
            try {
                WebElement mobileInput = driver.findElement(LoginLocator.inputNumber);
                mobileInput.clear();
                mobileInput.sendKeys(mobile);
                extTest.log(Status.PASS, "Entered mobile number after stale retry: " + mobile);
                return;
            } catch (Exception e) {
                extTest.log(Status.FAIL, "Failed on stale retry entering mobile: " + e.getMessage());
                throw e;
            }
        } catch (Exception e) {
            extTest.log(Status.FAIL, "Failed to enter mobile number: " + e.getMessage());
            throw e;
        }
    }

    public void clickContinue() {
        try {
            robustClick(LoginLocator.continueButton, "Continue button");
            extTest.log(Status.PASS, "Clicked Continue button");
        } catch (Exception e) {
            extTest.log(Status.FAIL, "Failed to click Continue button: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Enter OTP - supports both multi-input (per-digit) and single-input flows.
     * Accepts either a 1-field OTP string or full OTP string to populate multi-inputs.
     */
    public void enterOtp(String otp) {
        try {
            // Prefer multi-input approach if multiple inputs are present
            List<WebElement> otpBoxes = driver.findElements(LoginLocator.otpInputs);
            if (otpBoxes != null && otpBoxes.size() >= 4) {
                // If single digits expected, enter digit by digit (use min(length, boxes.size()))
                int sendLen = Math.min(otp.length(), otpBoxes.size());
                for (int i = 0; i < sendLen; i++) {
                    WebElement box = otpBoxes.get(i);
                    wait.until(ExpectedConditions.visibilityOf(box));
                    box.clear();
                    box.sendKeys(String.valueOf(otp.charAt(i)));
                }
                extTest.log(Status.PASS, "Entered OTP into multi-input boxes: " + otp);
                return;
            }

            // Fallback to single OTP field
            try {
                wait.until(ExpectedConditions.visibilityOfElementLocated(LoginLocator.otpSingle));
                WebElement single = driver.findElement(LoginLocator.otpSingle);
                single.clear();
                single.sendKeys(otp);
                extTest.log(Status.PASS, "Entered OTP into single field: " + otp);
                return;
            } catch (TimeoutException te) {
                // no OTP input found
                extTest.log(Status.FAIL, "OTP input field(s) not visible: " + te.getMessage());
                throw te;
            }
        } catch (Exception e) {
            extTest.log(Status.FAIL, "Failed to enter OTP: " + e.getMessage());
            throw e;
        }
    }

    public void enterOtpManually(WebDriver driver) {
        Scanner sc = null;
        try {
            sc = new Scanner(System.in);
            System.out.print("Enter OTP from SMS: ");
            String otp = sc.nextLine();

            WebDriverWait otpWait = new WebDriverWait(driver, Duration.ofSeconds(15));
            // try multi input first
            List<WebElement> otpBoxes = driver.findElements(LoginLocator.otpInputs);
            if (otpBoxes != null && otpBoxes.size() >= 4) {
                int sendLen = Math.min(otp.length(), otpBoxes.size());
                for (int i = 0; i < sendLen; i++) {
                    WebElement box = otpBoxes.get(i);
                    otpWait.until(ExpectedConditions.visibilityOf(box));
                    box.clear();
                    box.sendKeys(String.valueOf(otp.charAt(i)));
                }
                extTest.log(Status.PASS, "Manually entered OTP into multi-input boxes: " + otp);
            } else {
                WebElement otpBox = otpWait.until(ExpectedConditions.visibilityOfElementLocated(LoginLocator.otpSingle));
                otpBox.clear();
                otpBox.sendKeys(otp);
                extTest.log(Status.PASS, "Manually entered OTP into single field: " + otp);
            }
        } catch (Exception e) {
            extTest.log(Status.FAIL, "Failed to enter OTP manually: " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            if (sc != null) {
                try { sc.close(); } catch (Exception ignored) {}
            }
        }
    }

    public void clickResendOtp() {
        try {
            WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(35));
            longWait.until(ExpectedConditions.elementToBeClickable(LoginLocator.resendOtpButton));
            robustClick(LoginLocator.resendOtpButton, "Resend OTP button");
            extTest.log(Status.PASS, "Clicked Resend OTP button");
        } catch (Exception e) {
            extTest.log(Status.FAIL, "Failed to click Resend OTP button: " + e.getMessage());
            throw e;
        }
    }

    public boolean getNumErrorMessage() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(LoginLocator.invalidPhoneMsg));
            extTest.log(Status.PASS, "Displayed error: Invalid phone number");
            return true;
        } catch (TimeoutException te) {
            extTest.log(Status.FAIL, "Error message for invalid phone number not displayed");
            return false;
        }
    }

    public boolean getOtpErrorMessage() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(LoginLocator.invalidOtpMsg));
            extTest.log(Status.PASS, "Displayed error: Invalid OTP");
            return true;
        } catch (TimeoutException te) {
            extTest.log(Status.FAIL, "Error message for invalid OTP not displayed");
            return false;
        }
    }

    public boolean loginsuccessful() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(LoginLocator.userAvatar));
            extTest.log(Status.PASS, "Login successful - Profile icon is visible");
            return true;
        } catch (TimeoutException te) {
            extTest.log(Status.FAIL, "Login failed - Profile icon not found");
            return false;
        }
    }

    // ----------------- UPDATED OTP EXPIRY DETECTION METHODS -----------------

    /**
     * Wait until the OTP is considered expired by the UI.
     * This method waits up to timeoutSeconds (default 120s) for one of these to happen:
     *  - LoginLocator.otpExpiredMsg becomes visible (if present)
     *  - Any visible element containing text 'expired' appears
     *  - A 'resend' control becomes visible and usable
     *
     * Throws TimeoutException if none of the conditions are met within the timeout.
     */
    public void waitUntilOtpExpires() {
        waitUntilOtpExpires(120); // default 120 seconds
    }

    public void waitUntilOtpExpires(long timeoutSeconds) {
        try {
            WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));

            boolean expiredDetected = longWait.until(d -> {
                // 1) explicit otp expired locator (preferred if present)
                try {
                    List<WebElement> explicit = d.findElements(LoginLocator.otpExpiredMsg);
                    if (!explicit.isEmpty()) {
                        WebElement e = explicit.get(0);
                        if (isElementVisible(e)) return true;
                    }
                } catch (Exception ignored) {}

                // 2) generic text-based fallback: any visible element that contains 'expired'
                try {
                    List<WebElement> textMatches = d.findElements(By.xpath(
                        "//*[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'expired')]"
                    ));
                    for (WebElement e : textMatches) {
                        try {
                            if (isElementVisible(e)) return true;
                        } catch (StaleElementReferenceException sere) { continue; }
                    }
                } catch (Exception ignored) {}

                // 3) 'Resend' becomes available: accept many element types (button/a/div/span)
                try {
                    String resendXpath =
                        "//button[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'resend')]" +
                        " | //a[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'resend')]" +
                        " | //div[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'resend')]" +
                        " | //span[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'resend')]";

                    List<WebElement> resendElems = d.findElements(By.xpath(resendXpath));
                    for (WebElement r : resendElems) {
                        try {
                            if (!isElementVisible(r)) continue;
                            if (isElementClickableOrEnabled(r)) return true;
                        } catch (StaleElementReferenceException sere) { continue; }
                    }
                } catch (Exception ignored) {}

                // not expired yet
                return false;
            });

            if (expiredDetected) {
                extTest.log(Status.PASS, "OTP expiry detected within " + timeoutSeconds + " seconds.");
            } else {
                extTest.log(Status.FAIL, "OTP expiry not detected but wait returned false unexpectedly.");
            }
        } catch (TimeoutException te) {
            extTest.log(Status.FAIL, "OTP did not expire within " + timeoutSeconds + " seconds: " + te.getMessage());
            throw te;
        } catch (Exception e) {
            extTest.log(Status.FAIL, "Error while waiting for OTP expiry: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Quick non-blocking check whether an OTP expired message is visible right now.
     * Returns true if a visible expired indicator is found, otherwise false.
     */
    public boolean isOtpExpiredMessageDisplayed() {
        try {
            // 1) explicit locator
            try {
                List<WebElement> els = driver.findElements(LoginLocator.otpExpiredMsg);
                if (!els.isEmpty()) {
                    WebElement e = els.get(0);
                    if (isElementVisible(e)) return true;
                }
            } catch (Exception ignored) {}

            // 2) fallback: any visible element containing 'expired'
            try {
                List<WebElement> textMatches = driver.findElements(By.xpath(
                    "//*[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'expired')]"
                ));
                for (WebElement e : textMatches) {
                    try {
                        if (isElementVisible(e)) return true;
                    } catch (StaleElementReferenceException sere) {
                        // ignore and continue
                    }
                }
            } catch (Exception ignored) {}

            // 3) resend enabled as a hint (button/a/div/span)
            try {
                String resendXpath =
                    "//button[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'resend')]" +
                    " | //a[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'resend')]" +
                    " | //div[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'resend')]" +
                    " | //span[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'resend')]";

                List<WebElement> resend = driver.findElements(By.xpath(resendXpath));
                for (WebElement r : resend) {
                    try {
                        if (!isElementVisible(r)) continue;
                        if (isElementClickableOrEnabled(r)) return true;
                    } catch (StaleElementReferenceException sere) { continue; }
                }
            } catch (Exception ignored) {}

            return false;
        } catch (Exception e) {
            // be conservative on errors
            return false;
        }
    }

    // ----------------- helper utilities used by the two methods -----------------

    /**
     * Visible check: element attached, displayed and has non-zero size.
     */
    private boolean isElementVisible(WebElement e) {
        try {
            if (e == null) return false;
            if (!e.isDisplayed()) return false;
            try {
                return e.getSize() != null && e.getSize().getHeight() > 0 && e.getSize().getWidth() > 0;
            } catch (Exception ignored) {
                // fallback to bounding rect via JS
                try {
                    Object rect = ((JavascriptExecutor) driver).executeScript(
                        "var r = arguments[0].getBoundingClientRect(); return (r && r.width>0 && r.height>0);", e);
                    if (rect instanceof Boolean) return (Boolean) rect;
                } catch (Exception ignored2) {}
                return false;
            }
        } catch (StaleElementReferenceException sere) {
            return false;
        }
    }

    /**
     * Determine if an element is effectively clickable or enabled.
     * Covers isEnabled(), aria-disabled, and common CSS 'disabled' class checks.
     */
    private boolean isElementClickableOrEnabled(WebElement e) {
        try {
            // if element reports enabled, accept it
            try { if (e.isEnabled()) return true; } catch (Exception ignored) {}

            // aria-disabled attribute check
            try {
                String aria = e.getAttribute("aria-disabled");
                if (aria != null && (aria.equalsIgnoreCase("false") || aria.equalsIgnoreCase("0"))) return true;
            } catch (Exception ignored) {}

            // disabled attribute check
            try {
                String disabledAttr = e.getAttribute("disabled");
                if (disabledAttr == null || disabledAttr.trim().isEmpty()) return true;
                // if disabledAttr present with value 'true' treat as disabled
                if ("true".equalsIgnoreCase(disabledAttr) || "disabled".equalsIgnoreCase(disabledAttr)) return false;
            } catch (Exception ignored) {}

            // class-based heuristic: if class contains 'disabled' or 'inactive' treat as not enabled
            try {
                String cls = e.getAttribute("class");
                if (cls != null) {
                    String low = cls.toLowerCase();
                    if (low.contains("disabled") || low.contains("inactive") || low.contains("aria-disabled")) return false;
                    // otherwise assume usable
                    return true;
                }
            } catch (Exception ignored) {}

            // if none of the above indicated disabled, assume it's usable
            return true;
        } catch (StaleElementReferenceException sere) {
            return false;
        }
    }

    // --------------------------------------------------------------------------
}
