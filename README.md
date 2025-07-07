# MobileShoppingApp

A comprehensive mobile shopping application project with automated testing capabilities. This project combines an Android shopping app with Appium-based automated testing to ensure quality and functionality.

## 📋 Project Overview

This project consists of two main components:
- **ShopApp**: An Android mobile shopping application built with Kotlin and Jetpack Compose
- **AppiumTest**: Automated testing suite using Appium for mobile app testing

## 🏗️ Project Structure

```
MobileShoppingApp/
├── README.md                   # Project documentation
├── src/                        # Main source code directory
│   ├── README.md              # Source code documentation
│   ├── ShopApp/               # Android mobile application
│   │   ├── app/               # Main app module
│   │   │   ├── src/           # App source code
│   │   │   ├── build.gradle.kts
│   │   │   └── google-services.json
│   │   ├── build.gradle.kts   # Project build configuration
│   │   ├── gradle.properties
│   │   ├── settings.gradle.kts
│   │   └── libs/              # External libraries
│   └── tmp/                   # Temporary files
└── AppiumTest/                # Appium testing project
    ├── README.md              # Testing documentation
    ├── pom.xml                # Maven configuration
    ├── apps/                  # Test applications
    │   └── shop.apk          # Mobile app APK for testing
    ├── src/                   # Test source code
    │   ├── main/java/         # Main test classes
    │   └── test/java/         # Test cases
    └── target/                # Maven build output
```

## 📱 Mobile App (src/ShopApp)

### Technologies Used
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Build Tool**: Gradle (Kotlin DSL)
- **Services**: Google Services integration
- **Target SDK**: Android 35 (Android 15)
- **Minimum SDK**: Android 25 (Android 7.1)

### Features
- Modern Android shopping application
- Jetpack Compose UI
- Google Services integration
- Material Design components

### Getting Started with the Mobile App

#### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK 35
- Kotlin 1.9+
- Gradle 8.0+

#### Setup Instructions
1. Navigate to the `src/ShopApp` directory
2. Open the project in Android Studio
3. Sync the project with Gradle files
4. Build and run the application

```bash
cd src/ShopApp
./gradlew build
./gradlew installDebug
```

## 🧪 Automated Testing (AppiumTest)

### Technologies Used
- **Testing Framework**: Appium
- **Language**: Java
- **Build Tool**: Maven
- **Java Version**: 17
- **Test Runner**: TestNG/JUnit

### Getting Started with Testing

#### Prerequisites
- Java 17 or later
- Maven 3.6+
- Appium Server
- Android SDK
- Connected Android device or emulator

#### Setup Instructions
1. Navigate to the `AppiumTest` directory
2. Install dependencies:
   ```bash
   cd AppiumTest
   mvn clean install
   ```
3. Start Appium server:
   ```bash
   appium
   ```
4. Run tests:
   ```bash
   mvn test
   ```

#### Environment Setup
1. Install Appium:
   ```bash
   npm install -g appium
   ```
2. Install required drivers:
   ```bash
   appium driver install uiautomator2
   ```
3. Verify installation:
   ```bash
   appium doctor
   ```

## 🚀 Quick Start Guide

### 1. Clone the Repository
```bash
git clone [repository-url]
cd MobileShoppingApp
```

### 2. Build the Mobile App
```bash
cd src/ShopApp
./gradlew build
```

### 3. Generate APK for Testing
```bash
./gradlew assembleDebug
```

### 4. Copy APK to Test Directory
```bash
cp app/build/outputs/apk/debug/app-debug.apk ../../AppiumTest/apps/shop.apk
```

### 5. Run Automated Tests
```bash
cd ../../AppiumTest
mvn clean test
```

## 📋 Testing Scenarios

The automated tests cover:
- App launch and initialization
- Navigation between screens
- User interface interactions
- Shopping cart functionality
- User authentication flows
- Product browsing and search

## 🔧 Configuration

### Mobile App Configuration
- Package name: `com.example.shopapp`
- Version: 1.0
- Build configuration in `build.gradle.kts`

### Test Configuration
- Maven configuration in `pom.xml`
- Appium capabilities in test classes
- Device/emulator settings

## 📖 Documentation

- **Mobile App**: See `src/README.md` for detailed app documentation
- **Testing**: See `AppiumTest/README.md` for testing documentation
- **Original Project**: [GitLab Repository](https://gitlab.com/tgkhang/shop-android)

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Ensure all tests pass
6. Submit a pull request

## 📝 License

This project is for educational purposes. Please refer to the original project for licensing information.

## 🔗 Links

- **Original Project**: https://gitlab.com/tgkhang/shop-android
- **Appium Documentation**: https://appium.io/
- **Android Development**: https://developer.android.com/

---

*This project demonstrates mobile app development with automated testing practices using modern Android development tools and testing frameworks.*