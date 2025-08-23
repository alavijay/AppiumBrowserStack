package com.appuim;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
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

public class AppTest {
    private AndroidDriver driver;

    public static final String USERNAME = "vijayalapati1"; // Replace with your BrowserStack username
    public static final String ACCESS_KEY = "RsGNxWuTnebMjdrdueZX"; // Replace with your BrowserStack access key
    public static final String BROWSERSTACK_URL = "https://" + USERNAME + ":" + ACCESS_KEY + "@hub-cloud.browserstack.com/wd/hub";

    @BeforeMethod(alwaysRun = true)
    public void setUp() throws IOException {
        String env = System.getProperty("env", "browserstack"); // default to BrowserStack

        if (env.equalsIgnoreCase("local")) {
            System.out.println("Running test on LOCAL connected device...");
            setUpLocal();
        } else {
            System.out.println("Running test on BROWSERSTACK...");
            setUpBrowserStack();
        }

        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
    }

    private void setUpLocal() throws IOException {
        UiAutomator2Options options = new UiAutomator2Options();
        options.setDeviceName("RFCW90GG6BL"); // or specific device name from adb
        options.setPlatformName("Android");
        options.setAutomationName("UiAutomator2");

        // Path to APK in your project directory
        String appPath = System.getProperty("user.dir") + File.separator + "WikipediaSample.apk";
        options.setApp(appPath);
        options.setNoReset(true);

        driver = new AndroidDriver(new URL("http://127.0.0.1:4723"), options);
    }

    private void setUpBrowserStack() throws IOException {
        String appPath = System.getProperty("user.dir") + File.separator + "WikipediaSample.apk";
        String appUrl = uploadApp(appPath);

        UiAutomator2Options options = new UiAutomator2Options();
        options.setDeviceName("Google Pixel 6");
        options.setPlatformVersion("12.0");
        options.setApp(appUrl);
        options.setAutoGrantPermissions(true);

        HashMap<String, Object> browserstackOptions = new HashMap<>();
        browserstackOptions.put("projectName", "Android Sample Project");
        browserstackOptions.put("buildName", "android-appium-build-1");
        browserstackOptions.put("sessionName", "Bstack Sample Test");
        options.setCapability("bstack:options", browserstackOptions);

        driver = new AndroidDriver(new URL(BROWSERSTACK_URL), options);

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
        driver.findElement(AppiumBy.id("org.wikipedia.alpha:id/search_container")).click();
        WebElement searchInput = driver.findElement(AppiumBy.id("org.wikipedia.alpha:id/search_src_text"));
        Assert.assertTrue(searchInput.isDisplayed(), "Search input field was not displayed.");
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
