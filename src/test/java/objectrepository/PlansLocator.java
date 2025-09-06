package objectrepository;

import org.openqa.selenium.By;

public class PlansLocator {

    // ---------- Menu ----------
    public static final By MENU_BUTTON = By.xpath("//*[@id='main-menu']/div[1]/img");
    public static final By MENU_CONTAINER = By.xpath("//div[contains(@class,'nb__mainMenu') or @id='main-menu']");

    // ---------- Plans Links (menu) ----------
    public static final By TENANT_PLANS_LINK = By.xpath("//a[normalize-space()='Tenant Plans'] | //a[contains(normalize-space(.),'Tenant Plan')]");
    public static final By BUYER_PLANS_LINK  = By.xpath("//a[normalize-space()='Buyer Plans']  | //a[contains(normalize-space(.),'Buyer Plan')]");
    public static final By OWNER_PLANS_LINK  = By.xpath("//a[normalize-space()='Owner Plans']  | //a[contains(normalize-space(.),'Owner Plan')]");
    public static final By SELLER_PLANS_LINK = By.xpath("//a[normalize-space()='Seller Plans'] | //a[contains(normalize-space(.),'Seller Plan')]");

    // Generic fallback for any plan name inside menu
    public static By planLinkByName(String planName) {
        return By.xpath("//a[normalize-space()='" + planName + "'] | //div[contains(@class,'menu')]//a[contains(normalize-space(.),'" + planName + "')]");
    }

    // ---------- Plan Page Headings ----------
    public static By planPageHeading(String planName) {
        return By.xpath("//h1[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'), '" + planName.toLowerCase() + "')] | //h2[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'), '" + planName.toLowerCase() + "')]");
    }

    // ---------- Subscribe buttons inside a plan page - common patterns ----------
    public static By subscribeButtonForPlan(String planName) {
        // Generic subscribe selector (tries common patterns)
        return By.xpath("//button[normalize-space()='Subscribe' or contains(normalize-space(.),'Subscribe') or contains(@data-test,'subscribe')]");
    }

    // ---------- Payment page markers ----------
    public static final By PAYMENT_PAGE_HEADING = By.xpath("//h1[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'payment') or contains(.,'Payment')]");
    public static final By PAYMENT_CONTAINER = By.xpath("//form[contains(@action,'payment')]|//div[contains(@class,'payment') or contains(@id,'payment')]");

    // ---------- Generic modal close ----------
    public static final By GENERIC_MODAL_CLOSE_SVG = By.xpath("//*[@id='modalContent']//svg|//*[@id='modalContent']//button[contains(@class,'close')]");
    public static final By BACK_BUTTON = By.xpath("//button[contains(.,'Back') or contains(.,'Return')] | //a[contains(.,'Back') or contains(.,'Return')]");
}
