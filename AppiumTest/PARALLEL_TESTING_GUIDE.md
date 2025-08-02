# Parallel Device Testing Setup and Execution Guide

## Step-by-Step Execution:

### 1. Setup Environment Variables (One-time setup)

**Method 1: Using Windows Settings (GUI)**

1. Press `Win + X` and select "System"
2. Click "Advanced system settings"
3. Click "Environment Variables..."
4. Under "User variables", select "Path" and click "Edit..."
5. Click "New" and add these two paths:
   - `C:\Users\%USERNAME%\AppData\Local\Android\Sdk\platform-tools`
   - `C:\Users\%USERNAME%\AppData\Local\Android\Sdk\emulator`
6. Click "OK" to save and **restart PowerShell**

**Method 2: Using PowerShell (Command)**

```powershell
# Add Android SDK tools to PATH permanently
$env:Path += ";C:\Users\$env:USERNAME\AppData\Local\Android\Sdk\platform-tools;C:\Users\$env:USERNAME\AppData\Local\Android\Sdk\emulator"
```

After setup, restart PowerShell and you can use simple commands like:

```powershell
adb devices
emulator -list-avds
```

### 2. Device Configuration

Your project is already configured for these devices:

- **Device 1**: `Pixel_4` (emulator-5554) → Appium server port 4723
- **Device 2**: `Medium_Phone` (emulator-5556) → Appium server port 4725

_Note: Device configuration is in `DeviceConfig.java`. Update only if you use different AVD names._

### 3. Start Android Virtual Devices

**Option A: Using Android Studio (Recommended)**

- Open Android Studio → Tools → AVD Manager
- Click the "Play" button for `Pixel_4` and `Medium_Phone`

**Option B: Using Command Line**

```powershell
# Start first AVD
emulator -avd Pixel_4

# Start second AVD in another terminal
emulator -avd Medium_Phone
```

### 4. Verify Devices are Running

Check your devices are connected:

```powershell
adb devices
emulator -list-avds
```

You should see:

```
List of devices attached
emulator-5554   device
emulator-5556   device
```

### 5. Start Appium Servers

Open **two separate PowerShell terminals** and run:

**Terminal 1 (for Pixel_4):**

```powershell
appium server --port 4723 --session-override
```

**Terminal 2 (for Medium_Phone):**

```powershell
appium server --port 4725 --session-override
```

### 6. Run Parallel Tests

```powershell
mvn clean test
```

**What happens:**

- Tests will run simultaneously on both devices
- You'll see output for both `Pixel_4` and `Medium_Phone`
- Total execution time is reduced due to parallel execution

## Quick Start Guide (If Environment is Already Set Up)

1. **Start AVDs:** Use Android Studio AVD Manager or command line
2. **Verify devices:** `adb devices` (should show emulator-5554 and emulator-5556)
3. **Start Appium servers:** Two terminals with ports 4723 and 4725
4. **Run tests:** `mvn clean test`

## Current Configuration

- **Framework:** TestNG with parallel execution
- **Thread Count:** 2 (configured in testng.xml)
- **Test Class:** `TestClickAProduct.java`
- **Device Config:** `DeviceConfig.java`

## Common Issues and Solutions:

1. **"adb/emulator not recognized"**: Complete Step 1 (Environment Variables setup) and restart PowerShell

2. **Port conflicts**: Make sure ports 4723 and 4725 are free

   ```powershell
   netstat -an | findstr ":4723\|:4725"
   ```

3. **Device not found**:

   - Verify AVDs are running: `adb devices`
   - Check AVD names match those in `DeviceConfig.java`

4. **Appium server issues**:

   - Ensure both servers are running on different ports
   - Check server logs in the Appium terminal windows

5. **App installation**: Make sure `shop.apk` is in the `/apps` folder

6. **TestNG issues**: Project uses TestNG (not JUnit) - ensure no JUnit imports in test files

## Verification Checklist:

✅ **Before Running Tests:**

- [ ] Both AVDs are running (visible in Android Studio or `adb devices`)
- [ ] Two Appium servers running on ports 4723 and 4725
- [ ] `shop.apk` exists in `/apps` folder
- [ ] Environment variables set (can run `adb` and `emulator` commands)

✅ **During Test Execution:**

- Both devices should start the app simultaneously
- Console shows logs for both `Pixel_4` and `Medium_Phone`
- Tests run in parallel, reducing total execution time
- TestNG reports show 2 tests executed

## File Structure:

```
src/main/java/org/example/eshop/
├── TestClickAProduct.java    # Main parallel test class
├── DeviceConfig.java         # Device configuration
testng.xml                    # TestNG parallel configuration
pom.xml                       # Maven dependencies (TestNG, Appium, Selenium)
```
