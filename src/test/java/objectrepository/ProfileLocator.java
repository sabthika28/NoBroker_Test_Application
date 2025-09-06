package objectrepository;

import org.openqa.selenium.By;

public class ProfileLocator {

    // Top-right profile avatar that opens dropdown
    public static final By PROFILE_ICON = By.xpath(
        "//div[contains(@class,'profile-avatar') or contains(@class,'profile-icon') or @id='profile-icon' or @role='button']");

    // Alternate: username text that can open the dropdown
    public static final By PROFILE_USERNAME_ALT = By.xpath(
        "//div[contains(@class,'profile-name') or contains(@class,'username') or contains(.,'Profile') or contains(.,'Hi ')]");

    // "Profile" link inside dropdown (primary) - first matching anchor with Profile/My Profile text
    public static final By PROFILE_LINK = By.xpath(
        "//*[@id='profile-menu-dropdown']//a[contains(normalize-space(.),'Profile') or contains(normalize-space(.),'My Profile')][1]");

    // generic dropdown container (if needed)
    public static final By PROFILE_DROPDOWN = By.xpath(
        "//*[@id='profile-menu-dropdown' or contains(@class,'profile-dropdown') or contains(@class,'user-menu')]");

    // Left navigation items on Profile page (links/buttons)
    public static final By LEFT_NAV_ITEMS = By.xpath(
        "//nav[contains(@class,'left') or contains(@class,'profile-nav') or contains(@class,'sidebar')]//a | " +
        "//ul[contains(@class,'profile-sidebar') or contains(@class,'sidebar-list')]//li//a | " +
        "//div[contains(@class,'profile-sidebar') or contains(@class,'sidebar')]//a");

    // also accept left nav items that are plain divs (some sites use divs rather than anchors)
    public static final By LEFT_NAV_ITEMS_ALT = By.xpath(
        "//nav[contains(@class,'left') or contains(@class,'profile-nav') or contains(@class,'sidebar')]//div[normalize-space()] | " +
        "//div[contains(@class,'profile-sidebar') or contains(@class,'sidebar')]//div[normalize-space()]");

    // Generic main heading on Profile page (fallback)
    public static final By MAIN_HEADING = By.xpath("//h1 | //h2 | //header//h1");

    // Generic page container for profile (fallback)
    public static final By PROFILE_PAGE_CONTAINER = By.xpath(
        "//*[contains(@class,'profile-page') or contains(@id,'profile') or contains(@class,'my-account') or contains(@class,'account-page')]");

    private static String xpathLiteral(String s) {
        if (s == null || s.isEmpty()) {
            return "''";
        }
        if (!s.contains("'")) {
            return "'" + s + "'";
        }
        // contains single quotes -> build concat('a', "'", 'b', ...)
        StringBuilder sb = new StringBuilder("concat(");
        String[] parts = s.split("'");
        for (int i = 0; i < parts.length; i++) {
            if (!parts[i].isEmpty()) {
                sb.append("'").append(parts[i]).append("'");
                if (i < parts.length - 1) {
                    sb.append(",\"'\",");
                }
            } else {
                // consecutive quotes or leading/trailing quote -> just add the quote literal
                if (i < parts.length - 1) {
                    sb.append("\"'\"");
                    if (i < parts.length - 1) sb.append(",");
                }
            }
        }
        sb.append(")");
        return sb.toString();
    }

    public static By headingFor(String menuText) {
        if (menuText == null) menuText = "";
        String trimmed = menuText.trim();
        String lit = xpathLiteral(trimmed);

        // case-insensitive contains: translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')
        String lowerFunc = "translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')";
        String lowerLit = "translate(" + lit + ",'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')";

        // exact-match heading (case-insensitive)
        String b1 = String.format("//h1[normalize-space(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz') = %s)] | //h2[normalize-space(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz') = %s)]", lowerLit, lowerLit);

        // contains heading (case-insensitive)
        String b2 = String.format("//h1[contains(%s,%s)] | //h2[contains(%s,%s)]", lowerFunc, lowerLit, lowerFunc, lowerLit);

        // We check aria-label/id/class for containing the text (case-sensitive fallback) as an additional heuristic.
        String b3 = String.format("//*[contains(@aria-label,%s) or contains(@id,%s) or contains(@class,%s)]", lit, lit, lit);

        String combined = String.format("(%s) | (%s) | (%s)", b1, b2, b3);
        return By.xpath(combined);
    }
}
