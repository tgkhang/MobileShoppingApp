package org.example.youtube;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import org.openqa.selenium.By;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.net.URL;
import java.time.Duration;
import java.util.Arrays;

public class Video {
    public AndroidDriver driver;

    @BeforeTest
    public void setup() throws Exception {
        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setCapability("platformName", "Android");
        caps.setCapability("appium:automationName", "uiautomator2");
        caps.setCapability("appium:appPackage", "com.google.android.youtube");
        caps.setCapability("appium:appActivity", "com.google.android.youtube.HomeActivity");
        caps.setCapability("appium:noReset", true);
        driver = new AndroidDriver(new URL("http://127.0.0.1:4723"), caps);
    }

    @Test
    public void test() throws Exception {
        Thread.sleep(3000);

        // Click search
        driver.findElement(AppiumBy.androidUIAutomator(
                "new UiSelector().description(\"Search\")"
        )).click();

        Thread.sleep(1000);

        // Enter keyword
        driver.findElement(AppiumBy.androidUIAutomator(
                "new UiSelector().className(\"android.widget.EditText\")"
        )).sendKeys("[HCMUS] GIỚI THIỆU");

        Thread.sleep(500);

        // Press Enter
        driver.pressKey(new KeyEvent(AndroidKey.ENTER));

        Thread.sleep(3000);

        // Click video by XPath
        driver.findElement(By.xpath("//android.view.ViewGroup[@content-desc=\"[HCMUS] GIỚI THIỆU TRƯỜNG ĐẠI HỌC KHOA HỌC TỰ NHIÊN, ĐHQG-HCM - 7 minutes, 14 seconds - Go to channel - Thông Tin Truyền Thông - ĐH KHTN, ĐHQG - HCM - 5.7 thousand views - 2 years ago - play video\"]/android.view.ViewGroup[2]/android.view.ViewGroup/android.view.ViewGroup[1]")).click();

        Thread.sleep(5000);

        // Handle advertisements that might appear
        handleAdvertisements();

        // Skip to 1 minute mark
        skipToOneMinute();

        Thread.sleep(5000);
    }

    private void handleAdvertisements() throws Exception {
        // Wait a bit for potential ads to load
        Thread.sleep(3000);
        
        // Try to find and click "Skip Ad" button (multiple attempts for different ad types)
        for (int i = 0; i < 3; i++) {
            try {
                // Look for "Skip Ad" button - common selectors
                if (isElementPresent(AppiumBy.androidUIAutomator("new UiSelector().text(\"Skip Ad\")"))) {
                    driver.findElement(AppiumBy.androidUIAutomator("new UiSelector().text(\"Skip Ad\")")).click();
                    Thread.sleep(1000);
                    break;
                }
                
                // Alternative skip button text
                if (isElementPresent(AppiumBy.androidUIAutomator("new UiSelector().text(\"Skip Ads\")"))) {
                    driver.findElement(AppiumBy.androidUIAutomator("new UiSelector().text(\"Skip Ads\")")).click();
                    Thread.sleep(1000);
                    break;
                }
                
                // Look for skip button with description
                if (isElementPresent(AppiumBy.androidUIAutomator("new UiSelector().descriptionContains(\"Skip\")"))) {
                    driver.findElement(AppiumBy.androidUIAutomator("new UiSelector().descriptionContains(\"Skip\")")).click();
                    Thread.sleep(1000);
                    break;
                }
                
                // Look for close button (X) for some ad types
                if (isElementPresent(AppiumBy.androidUIAutomator("new UiSelector().className(\"android.widget.ImageButton\").descriptionContains(\"Close\")"))) {
                    driver.findElement(AppiumBy.androidUIAutomator("new UiSelector().className(\"android.widget.ImageButton\").descriptionContains(\"Close\")")).click();
                    Thread.sleep(1000);
                    break;
                }
                
                // Wait a bit before trying again
                Thread.sleep(2000);
            } catch (Exception e) {
                // If skip button not found, continue waiting
                Thread.sleep(2000);
            }
        }
        
        // Wait a bit more to ensure ad is fully skipped/finished
        Thread.sleep(2000);
    }

    private boolean isElementPresent(By locator) {
        try {
            driver.findElement(locator);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void skipToOneMinute() throws Exception {
        // Bấm vào giữa video để hiện thanh điều khiển
        clickScreenCenter();
        clickScreenCenter();
//        Thread.sleep(1000);
//
//        // Giả sử seekbar nằm ngang dưới màn hình:
//        // startX = vị trí hiện tại (0: đầu video)
//        // endX = vị trí bạn muốn seek tới
//        // Y = vị trí vertical cố định (gần cuối màn hình)
//
//        int startX = 200; // tuỳ device
//        int endX = 700;   // tuỳ device (khoảng 20% => 1 phút)
//        int y = 1200;     // tuỳ device
//
//        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
//        Sequence swipe = new Sequence(finger, 1);
//
//        swipe.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), startX, y));
//        swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
//        swipe.addAction(finger.createPointerMove(Duration.ofMillis(500), PointerInput.Origin.viewport(), endX, y));
//        swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
//
//        driver.perform(Arrays.asList(swipe));
//
//        Thread.sleep(2000);
    }
    private void clickScreenCenter() {
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence tap = new Sequence(finger, 1);

        int centerX = 540; // Giữa chiều ngang (1080 / 2)
        int centerY = 300; // Tầm giữa player (tuỳ chỉnh)

        tap.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), centerX, centerY));
        tap.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        tap.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

        driver.perform(Arrays.asList(tap));
    }


    @AfterTest
    public void close() {
        driver.quit();
    }
}
