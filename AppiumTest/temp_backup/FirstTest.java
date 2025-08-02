package org.example;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

public class FirstTest {
    public AndroidDriver driver;

    @BeforeTest
    public void setup() throws MalformedURLException {
        String appiumServerUrl = "http://127.0.0.1:4723";

        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setCapability("platformName", "Android");
        caps.setCapability("appium:automationName", "uiautomator2");
        caps.setCapability("appium:app", System.getProperty("user.dir")+ "/apps/shop.apk");
        //caps.setCapability("appium:appPackage", "com.android.settings");

        driver = new AndroidDriver( new URL(appiumServerUrl), caps);
    }


    @Test
    public void test(){
        //driver.findElement(AppiumBy.androidUIAutomator("new UiSelector().text(\"Get Started\")")).click();
        //driver.findElement(AppiumBy.xpath("//android.widget.TextView[@text='SIGN UP']/following-sibling::android.widget.Button")).click();


        // Click 'Get Started'
        driver.findElement(AppiumBy.androidUIAutomator(
                "new UiSelector().className(\"android.widget.Button\").instance(0)"
        )).click();

        // Wait for first EditText (email)
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                AppiumBy.androidUIAutomator(
                        "new UiSelector().className(\"android.widget.EditText\").instance(0)"
                )
        ));

        // Fill email
        driver.findElement(AppiumBy.androidUIAutomator(
                "new UiSelector().className(\"android.widget.EditText\").instance(0)"
        )).sendKeys("tgkhang22@clc.fitus.edu.vn");

        // Fill password
        driver.findElement(AppiumBy.androidUIAutomator(
                "new UiSelector().className(\"android.widget.EditText\").instance(1)"
        )).sendKeys("123456");

        // Click 'Sign In'
        driver.findElement(AppiumBy.androidUIAutomator(
                "new UiSelector().textContains(\"Sign In\")"
        )).click();

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }



        // Example 2: Find by element ID (use AppiumBy.id)
        // Note: If that is a resource-id, use it directly. If it’s an Accessibility ID, use AppiumBy.accessibilityId.
        // If it’s a UUID-like element id you got from inspecting, use driver.findElement(By.id(...)) with caution,
        // because Appium element IDs are dynamic and usually you don’t locate by them.

        // For demonstration, using findElement with an explicit ID:
        // driver.findElement(AppiumBy.id("00000000-0000-002a-3b9a-ca0a00000004")).click();

        // OR, if it's an accessibility ID:
        // driver.findElement(AppiumBy.accessibilityId("00000000-0000-002a-3b9a-ca0a00000004")).click();        //test tag

        /*

        Button(

            modifier = Modifier.testTag("signup_button")
                    ) {
                Text("SIGN UP")
            }
         */
        //driver.findElement(AppiumBy.accessibilityId("signup_button")).click();


    }

    @AfterTest
    public void close(){
        driver.quit();
    }
}
