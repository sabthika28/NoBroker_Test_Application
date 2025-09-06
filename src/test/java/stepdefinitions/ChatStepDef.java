package stepdefinitions;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;
import pages.ChatPage;

public class ChatStepDef {

    private ChatPage chatPage;

    @When("the user opens My Chats from the dropdown")
    public void the_user_opens_my_chats_from_dropdown() {
        chatPage = new ChatPage(Hooks.driver, Hooks.extTest);
        chatPage.openMyChats();
    }

    @Then("the My Chats page should be displayed")
    public void the_my_chats_page_should_be_displayed() {
        // ensure chatPage exists (in case step ordering changed)
        if (chatPage == null) {
            chatPage = new ChatPage(Hooks.driver, Hooks.extTest);
        }
        boolean displayed = chatPage.isMyChatsPageDisplayed();
        Assert.assertTrue(displayed, "My Chats page was not displayed.");
    }
}
