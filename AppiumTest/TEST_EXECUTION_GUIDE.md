# Test Execution Guide

## 🎯 Running Tests Separately

### **Method 1: Using Test Class Names (Simplest)**

```powershell
# Run ONLY parallel product test (2 devices)
mvn test -Dtest=TestClickAProduct

# Run ONLY login bug report test (1 device)
mvn test -Dtest=TestLogin

# Run BOTH tests
mvn test
```

### **Method 2: Using Maven Profiles (Recommended)**

```powershell
# Run parallel product testing
mvn test -Pparallel

# Run login bug report testing
mvn test -Plogin

# Run both tests (default)
mvn test
```

### **Method 3: Using Specific TestNG XML Files**

```powershell
# Run parallel tests
mvn test -DsuiteXmlFile=testng-parallel.xml

# Run login tests
mvn test -DsuiteXmlFile=testng-login.xml

# Run both tests
mvn test -DsuiteXmlFile=testng.xml
```

## 📋 **Test Descriptions**

### **TestClickAProduct** (Parallel Testing)

- **Purpose**: Tests product clicking functionality
- **Devices**: Runs on 2 devices simultaneously (Pixel_4 + Medium_Phone)
- **Requirements**: 2 Appium servers (ports 4723, 4725)
- **Duration**: ~30-60 seconds (parallel execution)

### **TestLogin** (Bug Report Testing)

- **Purpose**: Tests login with various credentials + generates bug reports
- **Devices**: Runs on 1 device (emulator-5554)
- **Requirements**: 1 Appium server (port 4723)
- **Output**: Writes failed tests to `bug_reports.csv`
- **Duration**: ~2-5 minutes (multiple test cases)

## 🚀 **Quick Commands**

| What you want to test                | Command                             |
| ------------------------------------ | ----------------------------------- |
| Product clicking (fast, 2 devices)   | `mvn test -Dtest=TestClickAProduct` |
| Login + bug reports (slow, 1 device) | `mvn test -Dtest=TestLogin`         |
| Everything                           | `mvn test`                          |

## 📁 **File Structure**

```
├── testng.xml              # Both tests
├── testng-parallel.xml     # Only parallel product test
├── testng-login.xml        # Only login test
├── TestClickAProduct.java  # Parallel testing
└── TestLogin.java          # Login + bug reporting
```
