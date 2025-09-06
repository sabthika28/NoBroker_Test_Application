package objectrepository;

import org.openqa.selenium.By;

public class RentalAgreementLocator {

    // ---------- Menu ----------
    public static final By MENU_BUTTON = By.xpath("//*[@id=\"main-menu\"]/div[1]/img");
    // menu container when menu is opened (fallback)
    public static final By MENU_CONTAINER = By.xpath("//div[contains(@class,'nb__mainMenu') or @id='main-menu']");

    // ---------- Rental Agreement ----------
    // Preferred: direct xpath you provided (menu item)
    public static final By RENTAL_AGREEMENT_LINK = By.xpath("//*[@id=\"main-menu\"]/div[2]/a[2]");
    // fallback: link text containing 'Rental Agreement'
    public static final By RENTAL_AGREEMENT_LINK_FALLBACK = By.xpath("//a[contains(normalize-space(.),'Rental Agreement')]");
    // page heading or unique element on the Rental Agreement page
    public static final By RENTAL_AGREEMENT_PAGE = By.xpath("//h1[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'rental agreement')]");

    // ---------- Renew Your Agreement ----------
    public static final By RENEW_YOUR_AGREEMENT = By.xpath("//*[@id='topSection']/div[3]/div[1]/div[2]/img");
    public static final By RENEWAL_PAGE = By.xpath("//h1[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'renew') or contains(.,'Renew')]");

    // ---------- Upload Your Draft ----------
    public static final By UPLOAD_YOUR_DRAFT = By.xpath("//*[@id=\"content-wrapper\"]/div[1]/div/div[2]/div[1]/div[2]/div[1]/div/div[2]/div[1]/div[2]/img");
    public static final By UPLOAD_PAGE = By.xpath("//h1[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'upload') or contains(.,'Upload Draft') or contains(.,'Upload your Draft')]");

    // ---------- E-Stamped Agreement ----------
    public static final By E_STAMPED_AGREEMENT = By.xpath("//*[@id=\"content-wrapper\"]/div[1]/div/div[2]/div[1]/div[2]/div[2]/div/div[3]/div[1]/div[2]/img");
    public static final By E_STAMPED_PAGE = By.xpath("//h1[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'e-stamp') or contains(.,'E-Stamped') or contains(.,'E Stamped')]");

    // ---------- Aadhaar E-Sign ----------
    public static final By AADHAAR_ESIGN = By.xpath("//*[@id=\"content-wrapper\"]/div[1]/div/div[2]/div[1]/div[2]/div[3]/div/div[3]/div[1]/div[2]/img");
    public static final By AADHAAR_ESIGN_PAGE = By.xpath("//h1[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'aadhaar') or contains(.,'E-Sign') or contains(.,'Aadhaar')]");

    // ---------- Your Ongoing Agreements ----------
    // exact xpath you supplied
    public static final By YOUR_ONGOING_AGREEMENTS = By.xpath("//*[@id='topSection']/div[3]/div[2]/div[2]/img");
    public static final By YOUR_ONGOING_PAGE = By.xpath("//h1[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'ongoing agreements') or contains(.,'Ongoing Agreements') or contains(.,'Your Ongoing Agreements')]");

    // ---------- Modal close (generic) ----------
    public static final By GENERIC_MODAL_CLOSE_SVG = By.xpath("//*[@id='modalContent']//svg|//*[@id='modalContent']//button[contains(@class,'close')]");
    public static final By GENERIC_MODAL_CLOSE_SVG_EXACT = By.xpath("//*[@id=\"modalContent\"]/div[1]/div[1]/div[2]/svg");

    // ---------- Back ----------
    public static final By BACK_TO_RENTAL = By.xpath("//button[contains(.,'Back') or contains(.,'Return')]|//a[contains(.,'Back') or contains(.,'Return')]");
}
