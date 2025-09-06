package stepdefinitions;

import io.cucumber.java.en.Given;
import org.testng.Assert;
import pages.HomePage;
import pages.LoginPage;
import objectrepository.LoginLocator;
import org.openqa.selenium.WebDriver;

/**
 * CommonSteps - simplified. Does NOT perform login.
 * Assumes a prior login scenario has already logged the user in and session persists.
 */
public class CommonSteps {

    // static guard — remains true while JVM process runs (so next scenarios won't re-check)
    private static boolean alreadyLoggedIn = false;

    @Given("the user is logged in")
    public void the_user_is_logged_in() {
        // If we've already verified login in this JVM run, do nothing.
        if (alreadyLoggedIn) {
            return;
        }

        if (Hooks.driver == null) {
            throw new IllegalStateException("Hooks.driver is null — make sure Hooks.setup() initializes the driver.");
        }

        WebDriver driver = Hooks.driver;

        // Ensure we're on the home page (cookies / domain)
        new HomePage(driver).openHomePage();

        // Quick non-invasive check: look for the profile icon / logged-in indicator.
        LoginPage loginPage = new LoginPage(driver, Hooks.extTest);
        boolean loggedIn = loginPage.loginsuccessful();

        // If not logged in, fail fast with a clear message telling the tester to run the login scenario first.
        if (!loggedIn) {
            throw new IllegalStateException(
                "User is not logged in. Run the login scenario first (or ensure an active session) before running this scenario."
            );
        }

        // Mark guard so subsequent scenarios skip this check.
        alreadyLoggedIn = true;
    }

    @Given("the user is on the NoBroker home page")
    public void the_user_is_on_home_page() {
        if (Hooks.driver == null) {
            throw new IllegalStateException("Hooks.driver is null — make sure Hooks.setup() initializes the driver.");
        }
        new HomePage(Hooks.driver).openHomePage();
    }
}
