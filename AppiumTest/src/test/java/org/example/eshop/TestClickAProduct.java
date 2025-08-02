
package org.example.eshop;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

public class TestClickAProduct {
    private ThreadLocal<AndroidDriver> driver = new ThreadLocal<AndroidDriver>();
    private ThreadLocal<WebDriverWait> wait = new ThreadLocal<WebDriverWait>();

    @DataProvider(name = "deviceConfig", parallel = true)
    public Object[][] deviceConfigProvider() {
        DeviceConfig[] configs = DeviceConfig.getDeviceConfigs();
        Object[][] data = new Object[configs.length][1];
        for (int i = 0; i < configs.length; i++) {
            data[i][0] = configs[i];
        }
        return data;
    }

    @BeforeMethod
    public void setup() throws MalformedURLException {
        // Setup will be handled in the test method itself
    }

    @Test(dataProvider = "deviceConfig")
    public void testClickProduct(DeviceConfig deviceConfig) throws MalformedURLException {
        System.out.println("Testing on device: " + deviceConfig.getDeviceName());
        
        // Setup driver for this specific device
        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setCapability("platformName", "Android");
        caps.setCapability("appium:automationName", "uiautomator2");
        caps.setCapability("appium:app", System.getProperty("user.dir") + "/apps/shop.apk");
        caps.setCapability("appium:deviceName", deviceConfig.getDeviceName());
        caps.setCapability("appium:platformVersion", deviceConfig.getPlatformVersion());
        caps.setCapability("appium:udid", deviceConfig.getUdid());
        caps.setCapability("appium:systemPort", deviceConfig.getSystemPort());
        
        
        AndroidDriver androidDriver = new AndroidDriver(new URL(deviceConfig.getAppiumServerUrl()), caps);
        driver.set(androidDriver);
        
        // Initialize WebDriverWait with 15 seconds timeout for slower loading
        wait.set(new WebDriverWait(androidDriver, Duration.ofSeconds(15)));
        
        // Wait for and click first button (Get Started)
        WebElement el1 = wait.get().until(ExpectedConditions.elementToBeClickable(
                AppiumBy.className("android.widget.Button")));
        el1.click();

        // Wait for and enter email
        WebElement el2 = wait.get().until(ExpectedConditions.visibilityOfElementLocated(
                AppiumBy.androidUIAutomator("new UiSelector().className(\"android.widget.EditText\").instance(0)")));
        el2.sendKeys("tgkhang22@clc.fitus.edu.vn");

        // Wait for and enter password
        WebElement el3 = wait.get().until(ExpectedConditions.visibilityOfElementLocated(
                AppiumBy.androidUIAutomator("new UiSelector().className(\"android.widget.EditText\").instance(1)")));
        el3.sendKeys("123456");

        // Wait for and click second button (Sign In)
        WebElement el4 = wait.get().until(ExpectedConditions.elementToBeClickable(
                AppiumBy.androidUIAutomator("new UiSelector().className(\"android.widget.Button\").instance(1)")));
        el4.click();

        // Force click notification permission
        try {
            Thread.sleep(3000); // Wait for permission dialog
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("Attempting to click Allow button for device: " + deviceConfig.getDeviceName());
        
        // Try multiple ways to click Allow button
        boolean clickedAllow = false;
        
        // Method 1: resourceId
        // if (!clickedAllow) {
        //     try {
        //         driver.get().findElement(AppiumBy.androidUIAutomator("new UiSelector().resourceId(\"com.android.permissioncontroller:id/permission_allow_button\")")).click();
        //         System.out.println("SUCCESS: Clicked Allow with resourceId for device: " + deviceConfig.getDeviceName());
        //         clickedAllow = true;
        //     } catch (Exception e1) {
        //         System.out.println("FAILED: resourceId method - " + e1.getMessage());
        //     }
        // }
        
        // Method 2: text "Allow"
        // if (!clickedAllow) {
        //     try {
        //         driver.get().findElement(AppiumBy.androidUIAutomator("new UiSelector().text(\"Allow\")")).click();
        //         System.out.println("SUCCESS: Clicked Allow with text for device: " + deviceConfig.getDeviceName());
        //         clickedAllow = true;
        //     } catch (Exception e2) {
        //         System.out.println("FAILED: text method - " + e2.getMessage());
        //     }
        // }
        
        // Method 3: button with textContains
        // if (!clickedAllow) {
        //     try {
        //         driver.get().findElement(AppiumBy.androidUIAutomator("new UiSelector().className(\"android.widget.Button\").textContains(\"Allow\")")).click();
        //         System.out.println("SUCCESS: Clicked Allow with textContains for device: " + deviceConfig.getDeviceName());
        //         clickedAllow = true;
        //     } catch (Exception e3) {
        //         System.out.println("FAILED: textContains method - " + e3.getMessage());
        //     }
        // }
        
        // Method 4: xpath
        // if (!clickedAllow) {
        //     try {
        //         driver.get().findElement(AppiumBy.xpath("//android.widget.Button[contains(@text,'Allow')]")).click();
        //         System.out.println("SUCCESS: Clicked Allow with xpath for device: " + deviceConfig.getDeviceName());
        //         clickedAllow = true;
        //     } catch (Exception e4) {
        //         System.out.println("FAILED: xpath method - " + e4.getMessage());
        //     }
        // }
        
        // Method 5: Try to find any button and click the last one (usually Allow)
        if (!clickedAllow) {
            try {
                var buttons = driver.get().findElements(AppiumBy.androidUIAutomator("new UiSelector().className(\"android.widget.Button\")"));
                System.out.println("Found " + buttons.size() + " buttons on screen");
                if (buttons.size() > 0) {
                    buttons.get(buttons.size() - 1).click();
                    System.out.println("SUCCESS: Clicked last button (assuming it's Allow) for device: " + deviceConfig.getDeviceName());
                    clickedAllow = true;
                }
            } catch (Exception e5) {
                System.out.println("FAILED: last button method - " + e5.getMessage());
            }
        }
        
        if (!clickedAllow) {
            System.out.println("WARNING: Could not find or click Allow button on device: " + deviceConfig.getDeviceName());
        }
        
        try {
            Thread.sleep(2000); // Wait after clicking
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Wait for app to load after login and click the product
        WebElement el5 = wait.get().until(ExpectedConditions.elementToBeClickable(
                AppiumBy.androidUIAutomator("new UiSelector().text(\"updatedTest3\")")));
        el5.click();

          try {
            Thread.sleep(3000); // Wait after clicking
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("Test completed successfully on device: " + deviceConfig.getDeviceName());
    }

    @AfterMethod
    public void close() {
        AndroidDriver androidDriver = driver.get();
        if (androidDriver != null) {
            androidDriver.quit();
            driver.remove();
            wait.remove();
        }
    }
}
