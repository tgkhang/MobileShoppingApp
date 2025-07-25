
package org.example.eshop;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

public class TestClickAProduct {
    public AndroidDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    public void setup() throws MalformedURLException {
        String appiumServerUrl = "http://127.0.0.1:4723";
        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setCapability("platformName", "Android");
        caps.setCapability("appium:automationName", "uiautomator2");
        caps.setCapability("appium:app", System.getProperty("user.dir") + "/apps/shop.apk");
        driver = new AndroidDriver(new URL(appiumServerUrl), caps);
        
        // Initialize WebDriverWait with 15 seconds timeout for slower loading
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @Test
    public void testClickProduct() {
        // Wait for and click first button (Get Started)
        WebElement el1 = wait.until(ExpectedConditions.elementToBeClickable(
                AppiumBy.className("android.widget.Button")));
        el1.click();

        // Wait for and enter email
        WebElement el2 = wait.until(ExpectedConditions.visibilityOfElementLocated(
                AppiumBy.androidUIAutomator("new UiSelector().className(\"android.widget.EditText\").instance(0)")));
        el2.sendKeys("tgkhang22@clc.fitus.edu.vn");

        // Wait for and enter password
        WebElement el3 = wait.until(ExpectedConditions.visibilityOfElementLocated(
                AppiumBy.androidUIAutomator("new UiSelector().className(\"android.widget.EditText\").instance(1)")));
        el3.sendKeys("123456");

        // Wait for and click second button (Sign In)
        WebElement el4 = wait.until(ExpectedConditions.elementToBeClickable(
                AppiumBy.androidUIAutomator("new UiSelector().className(\"android.widget.Button\").instance(1)")));
        el4.click();

        // Wait for app to load after login and click the product
        WebElement el5 = wait.until(ExpectedConditions.elementToBeClickable(
                AppiumBy.androidUIAutomator("new UiSelector().text(\"Mivi Play Bluetooth Speaker with 12 Hours Playtime. Wireless Speaker Made in India with Exceptional Sound Quality, Portable and Built in Mic-Black\")")));
        el5.click();
    }

    @AfterEach
    public void close() {
        if (driver != null) {
            driver.quit();
        }
    }
}
