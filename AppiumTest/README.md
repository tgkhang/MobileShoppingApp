# AppiumTest - Mobile Shopping App Testing

This is an Appium-based automated testing project for the Mobile Shopping App. This project contains automated tests for Android mobile application testing using Java, TestNG, and Appium.

## Project Structure

```
AppiumTest/
├── pom.xml                    # Maven configuration file
├── apps/
│   └── shop.apk              # Mobile application under test (excluded from git)
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── org/example/
│   │   │       ├── FirstTest.java    # Main test class
│   │   │       └── Main.java         # Main application entry point
│   │   └── resources/
│   └── test/
│       └── java/
├── README.md                  # This file
└── .gitignore                # Git ignore configuration
```

## Prerequisites

Before running the tests, ensure you have the following installed:

1. **Java Development Kit (JDK)** - Version 24 or compatible
2. **Maven** - For dependency management and build automation
3. **Android SDK** - For Android development tools
4. **Appium Server** - For mobile automation
5. **Android Emulator or Physical Device** - For test execution

## Dependencies

This project uses the following main dependencies:

- **Appium Java Client** (v9.4.0) - For mobile automation
- **TestNG** (v7.11.0) - For test framework and assertions

## Setup Instructions

1. **Clone the repository** (if this is part of a larger repo)
2. **Install dependencies**:

   ```bash
   mvn clean install
   ```

3. **Start Appium Server**:

   ```bash
   appium
   ```

   The server should be running on `http://127.0.0.1:4723`

4. **Prepare Android Environment**:

   - Start Android emulator or connect physical device
   - Enable USB debugging on physical device
   - Verify device connection: `adb devices`

5. **Place the APK file**:
   - Add your `shop.apk` file to the `apps/` directory
   - The APK file is automatically excluded from version control

## Running Tests

### Using Maven

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=FirstTest

# Clean and run tests
mvn clean test
```

### Using IDE

- Import the project as a Maven project
- Run individual test methods or classes directly from your IDE

## Configuration

The test configuration is set up in `FirstTest.java`:

- **Platform**: Android
- **Automation Name**: uiautomator2
- **App Path**: `./apps/shop.apk`
- **Appium Server**: `http://127.0.0.1:4723`

## Test Structure

- **FirstTest.java**: Contains the main test setup and configuration
- Tests use TestNG annotations for test lifecycle management
- Desired capabilities are configured for Android testing

## Troubleshooting

Common issues and solutions:

1. **Appium server not running**: Ensure Appium server is started before running tests
2. **Device not connected**: Check `adb devices` and device connection
3. **APK not found**: Ensure `shop.apk` is placed in the `apps/` directory
4. **Java version issues**: Verify Java 24 compatibility or adjust Maven compiler settings

## Contributing

This is part of a larger repository structure. When making changes:

1. Follow the existing code structure
2. Add appropriate test documentation
3. Ensure tests pass before committing
4. The APK file is excluded from version control

## Notes

- This is a subfolder of a larger repository
- APK files are excluded from version control for security and size reasons
- Test configurations may need adjustment based on your specific device/emulator setup
- Make sure Appium server is running before executing tests

## Contact

For questions or issues related to this testing project, please refer to the main repository documentation or contact the development team.
