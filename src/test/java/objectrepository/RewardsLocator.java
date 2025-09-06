package objectrepository;

import org.openqa.selenium.By;

public class RewardsLocator {
    // reward icon on homepage
    public static final By REWARD_ICON = By.xpath("//*[@id='reward']");

    // a likely heading or section on rewards page; fallback to text 'Rewards'
    public static final By REWARDS_PAGE_HEADER = By.xpath(
        "//h1[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'reward')]" +
        " | //h2[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'reward')]" );

    // optional marker if rewards page has a specific container
    public static final By REWARDS_CONTAINER = By.xpath(
        "//*[contains(@class,'reward') or contains(@class,'rewards') or contains(@id,'reward')]" );

    // generic close / overlay selector that might block clicks
    public static final By GENERIC_MODAL_CLOSE_SVG = By.xpath(
        "//*[local-name()='svg' and (contains(@class,'close') or contains(@class,'cross'))] | //button[@aria-label='Close' or contains(@class,'close')]" );
}
