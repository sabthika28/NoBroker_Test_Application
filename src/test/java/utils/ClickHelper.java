package utils;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;

public class ClickHelper {

    public static boolean safeClick(WebDriver driver, WebElement element, Object test) {
        try {
            element.click();
            return true;
        } catch (Exception e1) {
            // Try Actions click
            try {
                Actions actions = new Actions(driver);
                actions.moveToElement(element).click().perform();
                return true;
            } catch (Exception e2) {
                // Try JS click
                try {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
                    return true;
                } catch (Exception e3) {
                    // Final failure — log if test provided
                    if (test != null) {
                        try {
                            Class<?> extentHelper = Class.forName("utils.ExtentHelper");
                            extentHelper.getMethod("logFail", Object.class, String.class, Throwable.class)
                                    .invoke(null, test, "Click failed on element: " + element.toString(), e3);
                        } catch (Exception ignored) {}
                    }
                    return false;
                }
            }
        }
    }
}
