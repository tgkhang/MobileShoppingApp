package org.example.draft;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

public class YouTubeSearchTest {

    public static void main(String[] args) throws MalformedURLException, InterruptedException {
        String appiumServerUrl = "http://127.0.0.1:4723";

        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setCapability("platformName", "Android");
        caps.setCapability("automationName", "uiautomator2");
        caps.setCapability("appPackage", "com.google.android.youtube");
        caps.setCapability("appActivity", "com.google.android.apps.youtube.app.WatchWhileActivity");
        caps.setCapability("noReset", true);  // Do not reset YouTube each time

        AndroidDriver driver = new AndroidDriver(new URL(appiumServerUrl), caps);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // Wait for the Search button and click it
        WebElement el1 = wait.until(ExpectedConditions.elementToBeClickable(AppiumBy.accessibilityId("Search")));
        el1.click();

        // Wait for the search input box, click and type
        WebElement el2 = wait.until(ExpectedConditions.elementToBeClickable(AppiumBy.id("com.google.android.youtube:id/search_edit_text")));
        el2.click();
        el2.sendKeys("hcmus");

        // Wait for the search suggestion list and click the first result
        WebElement el3 = wait.until(ExpectedConditions.elementToBeClickable(
                AppiumBy.androidUIAutomator("new UiSelector().resourceId(\"com.google.android.youtube:id/linearLayout\").instance(0)")));
        el3.click();

        // Wait for video thumbnail or desired ImageView and click it
        WebElement el4 = wait.until(ExpectedConditions.elementToBeClickable(
                AppiumBy.androidUIAutomator("new UiSelector().className(\"android.widget.ImageView\").instance(4)")));
        el4.click();

        // Optional: Observe result
        Thread.sleep(5000);

        // Quit driver
        driver.quit();
    }
}
