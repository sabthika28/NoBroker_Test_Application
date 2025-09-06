package objectrepository;

import org.openqa.selenium.By;

public class InvalidPropertyLocator {
    // Landmark input field
    public static final By LANDMARK_INPUT = By.cssSelector(
        "input[name='landmark'], input[placeholder*='landmark'], input[aria-label*='landmark']"
    );

    // Inline error that may appear near the field (if any)
    public static final By LANDMARK_ERROR = By.cssSelector(
        ".landmark-error, .inline-error[for='landmark'], .field-error"
    );

    // Generic toast selectors (role=alert or common toast classes)
    public static final By TOAST_ERROR = By.cssSelector(
        "div[role='alert'], .toast, .notification, .nb__3P-mO, .nb__message, .toast-error"
    );

    // Fallback: any visible element containing the phrase "PLEASE SELECT" (case-insensitive)
    public static final By TOAST_TEXT_CONTAINS_PLEASE_SELECT = By.xpath(
        "//*[contains(translate(normalize-space(string(.)),'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ'),'PLEASE SELECT')]"
    );

    // === Exact text locator for the toast you reported (case-insensitive) ===
    public static final By TOAST_TEXT_EXACT_CHENNAI = By.xpath(
        "//*[translate(normalize-space(string(.)),'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ') = 'PLEASE SELECT A LOCALITY WITHIN CHENNAI']"
    );
}
