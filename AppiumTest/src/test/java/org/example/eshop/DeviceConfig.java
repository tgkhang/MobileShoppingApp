package org.example.eshop;

public class DeviceConfig {
    private String deviceName;
    private String platformVersion;
    private String udid;
    private String appiumServerUrl;
    private int systemPort;

    public DeviceConfig(String deviceName, String platformVersion, String udid, String appiumServerUrl, int systemPort) {
        this.deviceName = deviceName;
        this.platformVersion = platformVersion;
        this.udid = udid;
        this.appiumServerUrl = appiumServerUrl;
        this.systemPort = systemPort;
    }

    // Getters
    public String getDeviceName() { return deviceName; }
    public String getPlatformVersion() { return platformVersion; }
    public String getUdid() { return udid; }
    public String getAppiumServerUrl() { return appiumServerUrl; }
    public int getSystemPort() { return systemPort; }

    // Static method to get device configurations
    public static DeviceConfig[] getDeviceConfigs() {
        return new DeviceConfig[]{
            // Device 1 - Your Pixel_4 AVD
            new DeviceConfig(
                "Pixel_4",         // Your actual AVD name
                "11.0",            // Android version
                "emulator-5554",   // UDID when AVD starts
                "http://127.0.0.1:4723", 
                8200
            ),
            // Device 2 - Your Medium_Phone AVD
            new DeviceConfig(
                "Medium_Phone",    // Your actual AVD name
                "11.0",            // Android version
                "emulator-5556",   // UDID when second AVD starts
                "http://127.0.0.1:4725", 
                8201
            )
        };
    }
}
