package com.appuim;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.options.XCUITestOptions;
import okhttp3.*;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.testng.ITestResult;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.HashMap;

public class TheAppTest {
    private IOSDriver driver; // Change to IOSDriver

    public static final String USERNAME = "XXXXX"; // Replace with your BrowserStack username
    public static final String ACCESS_KEY = "XXXX"; // Replace with your BrowserStack access key
    public static final String BROWSERSTACK_URL = "https://" + USERNAME + ":" + ACCESS_KEY + "@hub-cloud.browserstack.com/wd/hub";

    @BeforeMethod(alwaysRun = true)
    public void setUp() throws IOException {
        String env = System.getProperty("env", "browserstack");

        if (env.equalsIgnoreCase("local")) {
            System.out.println("Running test on LOCAL connected iOS device...");
            setUpLocal();
        } else {
            System.out.println("Running test on BROWSERSTACK...");
            setUpBrowserStack();
        }

        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
    }

    private void setUpLocal() throws IOException {
        XCUITestOptions options = new XCUITestOptions(); // Use XCUITestOptions
        options.setDeviceName("iPhone 13"); // e.g., "iPhone 13"
        options.setPlatformName("iOS");
        options.setAutomationName("XCUITest");

        // Path to your .ipa file
        String appPath = System.getProperty("user.dir") + File.separator + "TheApp.ipa";
        options.setApp(appPath);

        // Required for local iOS setup
        options.setCapability("udid", "YOUR_UDID_HERE"); // Replace with your device's UDID
        options.setCapability("xcodeOrgId", "YOUR_XCODE_ORG_ID"); // Replace with your Xcode Org ID
        options.setCapability("xcodeSigningId", "iPhone Developer");

        driver = new IOSDriver(new URL("http://127.0.0.1:4723"), options);
    }

    private void setUpBrowserStack() throws IOException {
        String appPath = System.getProperty("user.dir") + File.separator + "Contacts.ipa";
        String appUrl = uploadApp(appPath);

        XCUITestOptions options = new XCUITestOptions(); // Use XCUITestOptions
        options.setDeviceName("iPhone 13");
        options.setPlatformVersion("17.0");
        options.setApp(appUrl);
        
        HashMap<String, Object> browserstackOptions = new HashMap<>();
        browserstackOptions.put("projectName", "iOS Sample Project");
        browserstackOptions.put("buildName", "ios-appium-build");
        browserstackOptions.put("sessionName", "Bstack Sample Test");
        options.setCapability("bstack:options", browserstackOptions);

        driver = new IOSDriver(new URL(BROWSERSTACK_URL), options); // Change to IOSDriver

        System.out.println("BrowserStack Session URL: https://automate.browserstack.com/dashboard/v2/sessions/" + driver.getSessionId());
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        if (driver != null) {
            String env = System.getProperty("env", "browserstack");
            if (!env.equalsIgnoreCase("local")) {
                JavascriptExecutor js = (JavascriptExecutor) driver;
                String reason = result.isSuccess() ? "Search input field is visible!" :
                        "Test failed: " + result.getThrowable().getMessage().replace("\"", "'");
                String status = result.isSuccess() ? "passed" : "failed";
                js.executeScript("browserstack_executor: {\"action\": \"setSessionStatus\", \"arguments\": {\"status\": \"" + status + "\", \"reason\": \"" + reason + "\"}}");
            }
            driver.quit();
        }
    }

    @Test(description = "Verify that the search input field is displayed after clicking the search container.")
    public void test1() {
        // iOS-specific selectors
        // Use Appium's accessibility ID for better cross-platform support where available
        // Note: The specific IDs below are examples and might need to be verified for the actual Wikipedia iOS app.
        // Locate the search input field using its accessibilityId.
        WebElement searchInput = driver.findElement(AppiumBy.accessibilityId("Search for contact"));

        // Click the search field to activate it.
        searchInput.click();

        // Enter the text "Chris" into the activated search field.
        searchInput.sendKeys("Chris");

        // assert 
        Assert.assertEquals(searchInput.getText(), "Chris", "Text 'Chris' was not entered correctly.");

    }

    private String uploadApp(String appPath) throws IOException {
        OkHttpClient client = new OkHttpClient();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", new File(appPath).getName(),
                        RequestBody.create(MediaType.parse("application/octet-stream"), new File(appPath)))
                .build();

        String credentials = Credentials.basic(USERNAME, ACCESS_KEY);

        Request request = new Request.Builder()
                .url("https://api-cloud.browserstack.com/app-automate/upload")
                .post(requestBody)
                .addHeader("Authorization", credentials)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response);
            }

            String responseBody = response.body().string();
            JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
            return jsonResponse.get("app_url").getAsString();
        }
    }
}