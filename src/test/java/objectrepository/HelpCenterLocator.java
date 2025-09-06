package objectrepository;

import org.openqa.selenium.By;

public class HelpCenterLocator {

    // Help Center button (parent button of the image)
    public static final By HELP_CENTER_BTN = By.xpath("//*[@id='app']/div/div/div[1]/button");

    // Service option (currently hardcoded 4th option)
    public static final By SERVICE_OPTION = By.xpath("//*[@id='app']/div/div/div[1]/div/div[1]/div[3]/div[2]/div/div[4]");

    // Complaint issue option
    public static final By ISSUE_OPTION = By.xpath("//*[@id='app']/div/div/div[1]/div/div[1]/div[2]/div[2]/div/div[4]/div[1]");

    // Close button (img inside button)
    public static final By CLOSE_BTN = By.xpath("//*[@id='app']/div/div/div[2]/button/img");
}

