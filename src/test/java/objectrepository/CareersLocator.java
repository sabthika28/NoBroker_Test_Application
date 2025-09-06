package objectrepository;

import org.openqa.selenium.By;

public class CareersLocator {

    // ---------- Menu ----------
    public static final By MENU_BUTTON = By.xpath("//*[@id=\"main-menu\"]/div[1]/img");
    public static final By MENU_CONTAINER = By.xpath("//div[contains(@class,'nb__mainMenu') or @id='main-menu']");

    // ---------- Careers link ----------
    public static final By CAREERS_LINK = By.xpath("//*[@id=\"main-menu\"]/div[2]/a[11]");
    public static final By CAREERS_LINK_FALLBACK = By.xpath("//a[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'career')]");
    public static final By CAREERS_PAGE = By.xpath("//h1[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'careers')] | //title[contains(.,'Careers')]");

    // ---------- Visible content we will scroll to ----------
    public static final By HOW_DID_WE_START_HEADING = By.xpath("//h2[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'how did we start')]");
    public static final By HOW_DID_WE_START_PARAGRAPH = By.xpath("//p[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'nobroker was founded') or contains(.,'NoBroker was founded')]");

    // ---------- Back ----------
    public static final By BACK_TO_CAREERS = By.xpath("//button[contains(.,'Back') or contains(.,'Return')] | //a[contains(.,'Back') or contains(.,'Return')]");
}
