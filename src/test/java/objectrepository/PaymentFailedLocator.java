package objectrepository;

import org.openqa.selenium.By;

public class PaymentFailedLocator {

    // ---------- Menu ----------
    public static final By MENU_BUTTON = By.xpath("//*[@id='main-menu']/div[1]/img");
    public static final By MENU_CONTAINER = By.xpath("//div[contains(@class,'nb__mainMenu') or @id='main-menu']");

    // ---------- Seller Plans ----------
    public static final By SELLER_PLANS_LINK = By.xpath("//a[normalize-space()='Seller Plans'] | //a[contains(normalize-space(.),'Seller Plan')]");
    public static final By SELLER_PLAN_HEADING = By.xpath("//h1[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'seller')] | //h2[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'seller')]");

    // ---------- Subscribe ----------
    public static final By SUBSCRIBE_BUTTON = By.xpath("//button[normalize-space()='Subscribe' or contains(normalize-space(.),'Subscribe') or contains(@data-test,'subscribe')]");

    // ---------- Payment page ----------
    public static final By PAYMENT_PAGE_HEADING = By.xpath("//h1[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'payment') or contains(.,'Payment')]");
    public static final By PAYMENT_CONTAINER = By.xpath("//form[contains(@action,'payment')]|//div[contains(@class,'payment') or contains(@id,'payment')]");

    // ---------- Back button (exact) ----------
    public static final By BACK_BUTTON = By.xpath("//*[@id='headerBack']");

    // ---------- Cancel Payment modal ----------
    public static final By CANCEL_PAYMENT_MODAL = By.xpath("//div[contains(@class,'modal') and (contains(.,'Cancel Payment') or contains(.,'Cancel payment'))]");

    // ✅ Yes button (exact XPath you provided)
    public static final By CANCEL_PAYMENT_YES_BUTTON = By.xpath("/html/body/div[1]/div[2]/div[2]/div/div/div/div/div[2]/div/button[1]");

    // (Optional) No button if you want later
    public static final By CANCEL_PAYMENT_NO_BUTTON = By.xpath("//button[normalize-space()='No' or normalize-space()='NO']");

    // ---------- Payment failed toast ----------
    public static final By PAYMENT_ERROR_TOAST = By.xpath("//*[contains(normalize-space(.),'Payment Failed') or contains(normalize-space(.),'Payment failed')]");
}
