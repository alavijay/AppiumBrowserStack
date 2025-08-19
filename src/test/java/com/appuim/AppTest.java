package com.appuim;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.testng.ITestResult;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.HashMap;

public class AppTest {
	private AndroidDriver driver;
	public static final String USERNAME = "XXXXXXXX"; // Replace with your BrowserStack username
	public static final String ACCESS_KEY = "XXXXXXX"; // Replace with your BrowserStack access ke
	public static final String BROWSERSTACK_URL = "https://" + USERNAME + ":" + ACCESS_KEY + "@hub-cloud.browserstack.com/wd/hub";

	@BeforeMethod(alwaysRun = true)
	public void setUp() throws MalformedURLException {
		UiAutomator2Options options = new UiAutomator2Options();
		// Set standard Appium capabilities
		options.setDeviceName("Google Pixel 6");
		options.setPlatformVersion("12.0");
		options.setApp("bs://b79e5b3e2a6393c6ac46ca8469910839689cd3db");
		options.setAutoGrantPermissions(true);

		// Set BrowserStack specific capabilities
		HashMap<String, Object> browserstackOptions = new HashMap<>();
		browserstackOptions.put("projectName", "Android Sample Project");
		browserstackOptions.put("buildName", "android-appium-build-1");
		browserstackOptions.put("sessionName", "Bstack Sample Test");
		options.setCapability("bstack:options", browserstackOptions);

		driver = new AndroidDriver(new URL(BROWSERSTACK_URL), options);
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

		// Print the session URL to the console
		System.out.println("BrowserStack Session URL: https://automate.browserstack.com/dashboard/v2/sessions/" + driver.getSessionId());
	}

	@AfterMethod(alwaysRun = true)
	public void tearDown(ITestResult result) {
		if (driver != null) {
			JavascriptExecutor js = (JavascriptExecutor) driver;
			String reason = result.isSuccess() ? "Search input field is visible!" : "Test failed: " + result.getThrowable().getMessage().replace("\"", "'");
			String status = result.isSuccess() ? "passed" : "failed";
			js.executeScript("browserstack_executor: {\"action\": \"setSessionStatus\", \"arguments\": {\"status\": \"" + status + "\", \"reason\": \"" + reason + "\"}}");
			driver.quit();
		}
	}

	@Test(description = "Verify that the search input field is displayed after clicking the search container.")
	public void test1() {
		driver.findElement(AppiumBy.id("org.wikipedia.alpha:id/search_container")).click();
		WebElement searchInput = driver.findElement(AppiumBy.id("org.wikipedia.alpha:id/search_src_text"));
		Assert.assertTrue(searchInput.isDisplayed(), "Search input field was not displayed.");
	}
}