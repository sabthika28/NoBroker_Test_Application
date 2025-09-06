package objectrepository;

import org.openqa.selenium.By;

public class ChatLocator {

    // Profile dropdown opener (top-right)
    public static final By PROFILE_ICON = By.xpath(
        "//div[contains(@class,'profile-avatar') or contains(@class,'profile-icon') or @id='profile-icon' or @role='button']");

    // Alternate username element that opens dropdown
    public static final By PROFILE_USERNAME_ALT = By.xpath(
        "//div[contains(@class,'profile-name') or contains(@class,'username') or contains(.,'Hi ') or contains(.,'SABTHIKA')]");

    // Primary exact location you provided for "My Chats"
    public static final By MY_CHATS_LINK = By.xpath("//*[@id='profile-menu-dropdown']/div[1]/span");

    // Fallbacks for "My Chats" inside the dropdown
    public static final By MY_CHATS_LINK_FALLBACK = By.xpath(
        "//a[contains(normalize-space(.),'My Chats') or contains(normalize-space(.),'Chats')] | " +
        "//*[@id='profile-menu-dropdown']//span[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'chat')]");

    // Possible chat page indicators (headings)
    public static final By MY_CHATS_HEADING = By.xpath(
        "//h1[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'chat')] | " +
        "//h2[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'chat')]");

    // Generic chat list container selectors (many sites use these patterns)
    public static final By CHAT_LIST = By.xpath(
        "//*[contains(@class,'chat-list') or contains(@class,'my-chats') or contains(@class,'conversations') or contains(@class,'messages') or contains(@class,'inbox')]");
    public static final By CHAT_ITEM = By.xpath(
        "//*[contains(@class,'chat-item') or contains(@class,'conversation-item') or contains(@class,'list-item') or contains(@class,'nb-chat')]");

    // VERY TOLERANT fallback: detect a few strings visible in your screenshots
    // (adjust or add strings that are stable on your site)
    public static final By CHAT_ITEM_TEXT_FALLBACK = By.xpath(
        "//*[contains(normalize-space(.),'NoBroker Support') or contains(normalize-space(.),'Rental Agreement Support') " +
        "or contains(normalize-space(.),'Packers & Movers') or contains(normalize-space(.),'Please provide feedback')]");
    
    // URL fragments which strongly indicate chat page
    public static final String[] CHAT_URL_FRAGMENTS = new String[] {
        "newChat", "newchat", "chat", "message", "conversation", "inbox"
    };
}
