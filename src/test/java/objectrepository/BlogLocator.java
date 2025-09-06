package objectrepository;

import org.openqa.selenium.By;

public class BlogLocator {

    // top-right menu button
    public static final By MENU_BUTTON =
            By.xpath("//*[@id='main-menu']/div[1]/img | //button[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'menu') or contains(@class,'menu') or contains(@aria-label,'Menu')]");

    public static final By MENU_CONTAINER =
            By.xpath("//div[contains(@class,'nb__mainMenu') or @id='main-menu' or contains(@class,'mainMenu') or contains(@class,'main-menu')]");

    // Blog link inside menu
    public static final By BLOG_LINK =
            By.xpath("//a[normalize-space()='Blog' or contains(normalize-space(.),'Blog') or contains(.,'The NoBroker Times')]" );

    // Blog page heading
    public static final By BLOG_PAGE_HEADING =
            By.xpath("//h1[contains(.,'The NoBroker Times') or contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'blog')]");

    // 🔥 Corrected Post Free Property Ad button
    public static final By POST_FREE_PROPERTY_AD_BUTTON =
            By.xpath("/html/body/nav/div/div[2]/div/a | //a[contains(.,'Post Free Property Ad') or contains(.,'Post FREE Property Ad')]");

    // Post Ad page form
    public static final By POST_AD_FORM =
            By.xpath("//form[contains(@action,'post') or contains(@id,'post') or //*[contains(@id,'post') and (self::form or self::div)]]");

    // site logo
    public static final By SITE_LOGO =
            By.xpath("//a[contains(@href,'/')]//img[contains(@alt,'NoBroker') or contains(@class,'logo')] | //a[contains(@class,'logo') or contains(@id,'logo')]");

    // home page marker
    public static final By HOME_PAGE_IDENTIFIER =
            By.xpath("//input[@placeholder='Search upto 3 localities or landmarks'] | " +
                     "//*[contains(.,\"World's Largest NoBrokerage Property Site\") or contains(.,'Search upto 3 localities')]");

    // modal close
    public static final By GENERIC_MODAL_CLOSE_SVG =
            By.xpath("//*[local-name()='svg' and (contains(@class,'close') or contains(@class,'cross'))] | //button[@aria-label='Close' or contains(@class,'close')]");
}
