package org.example;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.net.MalformedURLException;
import java.net.URL;

public class FirstTest {
    public AndroidDriver driver;

    @BeforeTest
    public void setup() throws MalformedURLException {
        String appiumServerUrl = "http://127.0.0.1:4723";

        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setCapability("platformName", "Android");
        caps.setCapability("appium:automationName", "uiautomator2");
        caps.setCapability("appium:app", System.getProperty("user.dir")+ "/apps/shop.apk");

        driver = new AndroidDriver( new URL(appiumServerUrl), caps);
    }

    @Test
    public void test(){
        driver.findElement(AppiumBy.androidUIAutomator("new UiSelector().text(\"Get Started\")")).click();
        //driver.findElement(AppiumBy.xpath("//android.widget.TextView[@text='SIGN UP']/following-sibling::android.widget.Button")).click();

        //test tag
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
