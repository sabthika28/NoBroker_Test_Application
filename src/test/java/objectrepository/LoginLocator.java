package objectrepository;

import org.openqa.selenium.By;

public class LoginLocator {

    // login page locator (header login trigger)
    public static By loginButton = By.xpath("//*[@id=\"navHeader\"]/div[5]/div[2]/div[2]/div");

    // phone input
    public static By inputNumber = By.id("signUp-phoneNumber");

    // continue / submit button after entering phone
    public static By continueButton = By.id("signUpSubmit");

    // OTP inputs (multi-digit) - general selector that matches common patterns
    public static By otpInputs = By.xpath("//input[contains(@class,'otp') or contains(@id,'otp') or contains(translate(@aria-label,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'digit') or contains(translate(@aria-label,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'verification code')]");

    // fallback single OTP field
    public static By otpSingle = By.xpath("//input[@id='otp' or contains(@name,'otp') or contains(translate(@aria-label,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'verification code')]");

    // resend OTP button - forgiving selector (button/div/a)
    public static By resendOtpButton = By.xpath("//button[contains(normalize-space(.),'Resend') or contains(normalize-space(.),'Resend OTP') or //div[contains(normalize-space(.),'Resend') and contains(@class,'resend')] | //a[contains(normalize-space(.),'Resend')]");

    // Backdrop/overlay that may intercept clicks when login modal opens
    public static By loginBackdrop = By.cssSelector(".login-signup__backdrop, .nb__backdrop, .modal-backdrop, .backdrop");

    // Error messages (use these or your own xpaths if site strings differ)
    public static By invalidPhoneMsg = By.xpath("//div[contains(text(),'Please enter valid phone number') or contains(text(),'valid phone') or contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'invalid phone')]");
    public static By invalidOtpMsg = By.xpath("//div[contains(text(),'Please enter valid OTP') or contains(text(),'Invalid OTP') or contains(text(),'invalid OTP') or contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'invalid otp')]");

    // OTP expired indicator (preferred explicit locator; falls back to generic 'expired' text in UI)
    public static By otpExpiredMsg = By.xpath("//*[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'otp') and contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'expired')] | //*[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'expired') and (contains(.,'OTP') or contains(.,'otp'))]");

    // Logged in indicator (profile icon) - existing code expects id "profile-icon"
    public static By userAvatar = By.id("profile-icon");
}
