package org.example;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.annotations.BeforeTest;

public class FirstTest {
    public AndroidDriver driver;

    @BeforeTest
    public void setup(){
        String appiumServer = "http://127.0.0.1:4723";

        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setCapability("platformName", "Android");
        caps.setCapability("appium:automationName", "uiautomator2");
        caps.setCapability("appium:app", System.getProperty("user.dir")+ "/apps/shop.apk");
    }
}
