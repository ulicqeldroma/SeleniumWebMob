package com.loghtyrian;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.Test;
import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import net.lightbody.bmp.proxy.CaptureType;
import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.core.har.HarLog;
import net.lightbody.bmp.core.har.HarEntry;
import net.lightbody.bmp.core.har.HarRequest;
import java.io.FileOutputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;

public class BasicTest {
	
	private final String pattern = "smetrics";

    private void checkAnalytics(final String searchString) {
		
		//Regex
		Pattern regex = Pattern.compile(pattern);
		
		 // start the proxy
		BrowserMobProxy proxy = new BrowserMobProxyServer();
		proxy.start(0);
		
		// get the Selenium proxy object
		Proxy seleniumProxy = ClientUtil.createSeleniumProxy(proxy);
		
		// configure it as a desired capability
		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities.setCapability(CapabilityType.PROXY, seleniumProxy);
		
		System.setProperty("webdriver.chrome.driver", "C:\\Users\\Loghtyrian\\Selenium\\chromedriver.exe");
		/*G:\Programacion\Selenium\chromedriver_win32*/
		WebDriver driver = new ChromeDriver(capabilities);

        // enable more detailed HAR capture, if desired (see CaptureType for the complete list)
		proxy.enableHarCaptureTypes(CaptureType.REQUEST_CONTENT, CaptureType.RESPONSE_CONTENT);

		// create a new HAR with the label "statefarm.com"
		proxy.newHar("statefarm.com");

		// open website
		driver.get(searchString);

		// get the HAR data
		Har har = proxy.getHar();

        System.out.println("Page title is: " + driver.getTitle());
		
		//Write to File
		try{
			FileOutputStream fos = new FileOutputStream("C:\\Users\\Loghtyrian\\Documents\\SeleniumSamples\\MobBrowser\\SeleniumWebMob\\result.txt");
			har.writeTo(fos);
		} catch(Exception e){
			
		}
		
		HarLog harLog = har.getLog();
		List<HarEntry> logEntries = harLog.getEntries();
		String httpMethod = "";
		for(HarEntry harEntry : logEntries){
			HarRequest harRequest = harEntry.getRequest();
			Matcher m = regex.matcher(harRequest.getUrl());
			if(m.find()){
				System.out.println(harRequest.getQueryString());
			}
		}
			
		proxy.stop();
		driver.quit();

    }

    @Test
    public void checkLife() {
        checkAnalytics("https://www.statefarm.com/insurance/life");
    }

}
