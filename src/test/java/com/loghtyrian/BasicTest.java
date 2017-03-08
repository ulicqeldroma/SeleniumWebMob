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
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
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
import java.util.concurrent.TimeUnit;
import java.io.*;

public class BasicTest {
	
	private final String pattern = "smetrics";
	private final String vidClass = "vjs-paused";
	private final String locDriver = "C:\\Users\\Loghtyrian\\Selenium\\chromedriver.exe";
	FileOutputStream fos;

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
		
		System.setProperty("webdriver.chrome.driver", locDriver);
		/*G:\Programacion\Selenium\chromedriver_win32*/
		WebDriver driver = new ChromeDriver(capabilities);
		
		//Set implicit wait
		//driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

        // enable more detailed HAR capture, if desired (see CaptureType for the complete list)
		proxy.enableHarCaptureTypes(CaptureType.REQUEST_CONTENT, CaptureType.RESPONSE_CONTENT);

		// create a new HAR with the label "statefarm.com"
		proxy.newHar("statefarm.com");

		// open website
		driver.get(searchString);

		// get the HAR data
		Har har = proxy.getHar();

        System.out.println("Page title is: " + driver.getTitle());
		
		// Find the input element by its ID
        WebElement scVideo = driver.findElement(By.id("play-control"));
		
		// Wait for the DOM to be complete, timeout after 60 seconds
        (new WebDriverWait(driver, 20)).until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver d) {
				return ((JavascriptExecutor)d).executeScript("return document.readyState").equals("complete");
            }
        });
		
		//Click on the Video
		scVideo.click();
		
		// Wait for the video to finish, timeout after 60 seconds
        (new WebDriverWait(driver, 60)).until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver d) {
				Pattern rgxvid = Pattern.compile(vidClass);
				Matcher m = rgxvid.matcher(d.findElement(By.id("play-control")).getAttribute("class"));
                return m.find();
            }
        });
		
		//Find a link in the left
		WebElement scLink = driver.findElement(By.cssSelector("div#left-channel-links li a"));
		
		//Click on the Link
		scLink.click();
		
		//Write to File
		try{
			fos = new FileOutputStream("C:\\Users\\Loghtyrian\\Documents\\SeleniumSamples\\MobBrowser\\SeleniumWebMob\\result.txt");
		} catch(Exception e){
			//TODO
		}
		
		//Get Requests to smetrics
		HarLog harLog = har.getLog();
		List<HarEntry> logEntries = harLog.getEntries();
		String httpMethod = "";
		for(HarEntry harEntry : logEntries){
			HarRequest harRequest = harEntry.getRequest();
			Matcher m = regex.matcher(harRequest.getUrl());
			if(m.find()){
				System.out.println(harRequest.getQueryString());
				try{
					String _content = harRequest.getQueryString().toString();
					byte[] content = _content.getBytes();
					fos.write(content);
				} catch(Exception e){
					//TODO
				}
			}
		}
		
		try{
			fos.flush();
			fos.close();
		} catch(Exception e){
			//TODO
		}
			
		proxy.stop();
		driver.quit();

    }

    @Test
    public void checkLifeVideo() {
        checkAnalytics("https://www.statefarm.com/insurance/life");
    }

}
