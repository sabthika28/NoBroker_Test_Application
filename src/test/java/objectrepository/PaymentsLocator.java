package objectrepository;

import org.openqa.selenium.By;

public class PaymentsLocator {

    // Pay Rent: prefer using id rentPayment (root) then find clickable child with text
    public static final By PAY_RENT_BTN =
        By.xpath("//*[@id='rentPayment']//*[self::button or self::a or self::div][contains(normalize-space(.),'Pay Rent')][1]");

    // Payments (Payments via Credit Card) page header
    public static final By PAYMENTS_PAGE_HEADER =
        By.xpath("//h1[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'payments via credit card') " +
                 "or contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'payments')]");

    // Preferred (non-absolute) My Payments locator - primary attempt
    public static final By MY_PAYMENTS_BTN = By.xpath(
        "//*[self::button or self::a or self::div][contains(normalize-space(.),'My Payments') or contains(@aria-label,'My Payments')][1]");

    // Absolute fallback you provided (last resort)
    public static final By MY_PAYMENTS_BTN_ABSOLUTE = By.xpath("/html/body/div[1]/div/main/div[1]/div/div[1]");

    // Payments History page header
    public static final By PAYMENTS_HISTORY_HEADER =
        By.xpath("//h1[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'payments history')]" );

    // Generic payment container (heuristic)
    public static final By PAYMENT_CONTAINER =
        By.xpath("//*[contains(translate(@class,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'payment') " +
                 "or contains(translate(@class,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'checkout') " +
                 "or contains(translate(@id,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'payment') ]");

    // No transactions message
    public static final By NO_TRANSACTIONS_MSG =
        By.xpath("//*[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'you have no transactions')]");

    public static final By GENERIC_MODAL_CLOSE_SVG =
        By.xpath("//*[local-name()='svg' and (contains(@class,'close') or contains(@class,'cross'))] | //button[@aria-label='Close' or contains(@class,'close')]");
}
