package com.saucelabs;

import static org.junit.Assert.assertEquals;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.saucelabs.common.SauceOnDemandAuthentication;
import com.saucelabs.common.SauceOnDemandSessionIdProvider;
import com.saucelabs.junit.ConcurrentParameterized;
import com.saucelabs.junit.Parallelized;
import com.saucelabs.junit.SauceOnDemandTestWatcher;

/**
 * Demonstrates how to write a JUnit test that runs tests against Sauce Labs using multiple browsers in parallel.
 * <p/>
 * The test also includes the {@link SauceOnDemandTestWatcher} which will invoke the Sauce REST API to mark
 * the test as passed or failed.
 *
 * @author Ross Rowe
 */
@RunWith(Parameterized.class)
public class SampleSauceTest implements SauceOnDemandSessionIdProvider {

    /**
     * Constructs a {@link SauceOnDemandAuthentication} instance using the supplied user name/access key.  To use the authentication
     * supplied by environment variables or from an external file, use the no-arg {@link SauceOnDemandAuthentication} constructor.
     */
    public SauceOnDemandAuthentication authentication = new SauceOnDemandAuthentication("Binita", "f457fba2-5e19-4574-a417-8da626d2c4bb");

    /**
     * JUnit Rule which will mark the Sauce Job as passed/failed when the test succeeds or fails.
     */
    @Rule
    public SauceOnDemandTestWatcher resultReportingTestWatcher = new SauceOnDemandTestWatcher(this, authentication, true); //adding true didnt work. Nothing is displayed in Sauce Jobs Tab in Bamboo

    public @Rule TestName testName = new TestName();
    /**
     * Represents the browser to be used as part of the test run.
     */
    private WebDriver driver;
	private static DesiredCapabilities capabilities;
	private static Platform ANDROID, LINUX, MAC, UNIX, VISTA, WINDOWS, XP, platformValue;
	private String browser, browserVersion, platform, sessionId = "";
    
	
	Platform[] platformValues = Platform.values();

	public Platform setPlatformCapabilities(String platformParam) {
		String platformVal = platformParam;
		for (int p=0; p<platformValues.length; p++) {
			platformValue = platformValues[p++]; //tried with [p] as well. doesn't work
			if (platformValue.toString() == platformVal) break;
		}
		return platformValue;
	}
	
	@Parameters
	public static Collection<Object[]> data() {
		String json = System.getenv("bamboo_SAUCE_ONDEMAND_BROWSERS");
		System.out.println(json);
		List<Object[]> browsers = new ArrayList<Object[]>();
		//JSONArray browserArray = null;
		try {
			JSONArray browserArray = new JSONArray(json);
			for (int i =0;i<browserArray.length();i++) {
				JSONObject browserJSON = browserArray.getJSONObject(i);
				browsers.add(new Object[]{browserJSON.get("browser"), browserJSON.get("browser-version"), browserJSON.get("os")});
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return browsers;
	}
    

    /**
     * Constructs a new instance of the test.  The constructor requires three string parameters, which represent the operating
     * system, version and browser to be used when launching a Sauce VM.  The order of the parameters should be the same
     * as that of the elements within the {@link #browsersStrings()} method.
     * @param os
     * @param version
     * @param browser
     */
    public SampleSauceTest(String s1, String s2, String s3) {
    	browser = s1;
		browserVersion = s2;
		platform = s3;
		
    }

    
    /**
     * Constructs a new {@link RemoteWebDriver} instance which is configured to use the capabilities defined by the {@link #browser},
     * {@link #version} and {@link #os} instance variables, and which is configured to run against ondemand.saucelabs.com, using
     * the username and access key populated by the {@link #authentication} instance.
     *
     * @throws Exception if an error occurs during the creation of the {@link RemoteWebDriver} instance.
     */
    @Before
    public void setUp() throws Exception {

    	capabilities = new DesiredCapabilities();
        capabilities.setCapability("browserName", browser);
		capabilities.setCapability("version", browserVersion);
		capabilities.setCapability("platform", platform);
		
		capabilities.setCapability("name", this.getClass().getName() + "." + testName.getMethodName());
		this.driver = new RemoteWebDriver(
		new URL("http://" + authentication.getUsername() + ":" + authentication.getAccessKey() + "@ondemand.saucelabs.com:80/wd/hub"),
		capabilities);
		this.sessionId = ((RemoteWebDriver)driver).getSessionId().toString();
		if (browserVersion == "") browserVersion = "unspecified";
		String browserName = String.format("%-19s", browser).replaceAll(" ", ".").replaceFirst("[.]", " ");
		String browserVer = String.format("%-19s", browserVersion).replaceAll(" ", ".");
		System.out.println("@Test "+testName.getMethodName()+"testing browser/version: " + browserName + browserVer + "platform: " + platform);
	}

    /**
     * Runs a simple test verifying the title of the amazon.com homepage.
     * @throws Exception
     */
    @Test
    public void googleTest() throws Exception {
        driver.get("http://www.google.com/");       
		System.out.println("SauceOnDemandSessionID="+getSessionId() +" job-name=googleTest"); 
        assertEquals("Google", driver.getTitle());
		driver.findElement(By.id("sb_ifc0")).sendKeys("SauceLabs"+Keys.ENTER);
    }

    /**
     * Closes the {@link WebDriver} session.
     *
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception {
        driver.quit();
    }

    /**
     *
     * @return the value of the Sauce Job id.
     */
    @Override
    public String getSessionId() {
        return sessionId;
    }
}