package objectrepository;

import org.openqa.selenium.By;

public class LogoutLocator {

    // Combined profile icon locator (wrapper, svg, img, button) — prefer this in page code
    public static final By PROFILE_ICON =
            By.xpath(
                "//*[@id='profile-icon']"
              + " | //*[@id='profile-icon']/div/svg"
              + " | //button[@id='profile-icon']//svg"
              + " | //img[contains(@class,'avatar') or contains(@alt,'profile') or contains(@id,'profile-icon')]"
              + " | //div[contains(@class,'profile') or contains(@class,'avatar')]"
              + " | //button[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'profile') or contains(@aria-label,'profile')]"
            );

    // Specific fallbacks (kept for compatibility)
    public static final By PROFILE_ICON_SVG =
            By.xpath("//*[@id='profile-icon']/div/svg | //button[@id='profile-icon']//svg");
    public static final By PROFILE_ICON_IMG =
            By.xpath("//img[contains(@class,'avatar') or contains(@alt,'profile') or contains(@id,'profile-icon')]");
    public static final By PROFILE_ICON_WRAPPER =
            By.xpath("//*[@id='profile-icon'] | //div[contains(@class,'profile') or contains(@class,'avatar')] | //button[contains(@aria-label,'profile')]");

    // Profile dropdown container and sign-out entry
    public static final By PROFILE_MENU =
            By.xpath("//*[@id='profile-menu-dropdown' or contains(@class,'profile-menu') or contains(@class,'profileDropdown')]");
    // Sign out — several fallbacks for div or anchor
    public static final By SIGN_OUT_LINK =
            By.xpath("//*[@id='profile-menu-dropdown']//div[contains(normalize-space(.),'Sign Out') or contains(normalize-space(.),'Sign out') or contains(normalize-space(.),'Logout')]" 
                   + " | //a[normalize-space()='Sign Out' or normalize-space()='Logout' or contains(.,'Sign Out')]");

    // A reliable indicator of the login page / login controls (phone input, login form)
    public static final By LOGIN_PAGE_IDENTIFIER =
            By.xpath("//input[@type='tel' or @name='phone' or contains(@placeholder,'phone') or //form[contains(@action,'login')]]");

    // Header links visible when logged out
    public static final By LOGIN_LINK =
            By.xpath("//a[normalize-space()='Log in' or normalize-space()='Log In' or contains(normalize-space(.),'Log in') or contains(normalize-space(.),'Login')]");
    public static final By SIGNUP_LINK =
            By.xpath("//a[normalize-space()='Sign up' or normalize-space()='Sign Up' or contains(normalize-space(.),'Sign up') or contains(normalize-space(.),'Signup')]");

    // Generic modal close (svg/button)
    public static final By GENERIC_MODAL_CLOSE_SVG =
            By.xpath("//*[local-name()='svg' and (contains(@class,'close') or contains(@class,'cross'))] | //button[@aria-label='Close' or contains(@class,'close')]");

}
