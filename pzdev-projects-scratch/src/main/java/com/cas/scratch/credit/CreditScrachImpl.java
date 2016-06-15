package com.cas.scratch.credit;

import java.io.File;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.cas.scratch.util.ImageCleaner;
import com.cas.scratch.util.ImageParserUtil;
import com.cas.scratch.util.WebElementExtender;
import com.sac.scratch.AbstractScratch;
import com.sac.utility.common.GlobalConsts;

public class CreditScrachImpl extends AbstractScratch {
	
	protected final static String DATEFORMAT = GlobalConsts.DATETIME_FORMAT_STRING;
	private String driverName = "Chrome";
	private String scratchURL = "";
	private String userData = "";
	private String userName = "";
	private String passWord = "";
	public CreditScrachImpl(){
		
	}

	@Override
	public void doScratch() {
		this.scratchURL = scratchElement.attributeValue("scratchUrl");
		WebDriver driver = getWebDriver(driverName);
		
		for(int i=0;i<userList.size();i++){
			String userStr = userList.get(i);
			userName = userStr.split("	")[0];
			passWord = userStr.split("	")[1];
			driver.get(scratchURL);
			runScratch(driver);
		}
		driver.quit();
	}
	
	public void runScratch(WebDriver driver){
		driver.switchTo().frame("headerFrame");
		WebDriverWait wait = new WebDriverWait(driver, 10);
		WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id='header']/div[2]/a[1]")));
		loginBtn.click();
		driver.switchTo().defaultContent();
		if(!login(driver)){
			driver.get(scratchURL);
			runScratch(driver);
		}else{
			//gotoUserPage(driver);
			//loopPage(driver);
			System.out.println("success");
			//driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			logout(driver);
		}
	}
	
	public boolean login(WebDriver driver){
		try{
			String xPath = "//*[@id='conFrame']";
			WebDriverWait wait = new WebDriverWait(driver, 20);
			WebElement conFrame = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xPath)));
			Point framePoint = conFrame.getLocation();
			driver.switchTo().frame(conFrame);
			
			WebElement usernameInput = driver.findElement(By.xpath("//*[@id='loginname']"));
			usernameInput.clear();
			usernameInput.sendKeys(userName);
			WebElement passwordInput = driver.findElement(By.xpath("//*[@id='password']"));
			passwordInput.clear();
			passwordInput.sendKeys(passWord);
			
			wait = new WebDriverWait(driver, 10);
			WebElement codeElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id='imgrc']")));
			ImageParserUtil imageParserUtil = new ImageParserUtil("eng");
			File imageFile = WebElementExtender.captureElementPicture(framePoint, codeElement);
			ImageCleaner.thresholdImage(imageFile,imageFile, 130, 255);
			String authCode = imageParserUtil.getAuthCodeText(imageFile);
			System.out.println("authCode:"+authCode);
			if(authCode.length()<6)	return false;
			
			WebElement imagecode = driver.findElement(By.xpath("//*[@id='_@IMGRC@_']"));
			imagecode.clear();
			imagecode.sendKeys(authCode);
			
			
			WebElement LoginImageButton = driver.findElement(By.xpath("//form[@name='loginForm']/div[5]/div[2]/input"));
			LoginImageButton.click();
			driver.switchTo().defaultContent();
			try{
				driver.switchTo().frame("conFrame");
				wait = new WebDriverWait(driver, 5);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id='centerDiv1']")));
				driver.switchTo().defaultContent();
				return true;
			}catch(Exception e1){
				logger.info("Login failed!");
			}
			return false;
			
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}
	
	public void logout(WebDriver driver){
		driver.switchTo().frame("headerFrame");
		String xPath = "//*[@id='header']/div[2]/a[1]";
		WebDriverWait wait = new WebDriverWait(driver, 20);
		WebElement logout = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xPath)));
		logout.click();
		wait = new WebDriverWait(driver, 5);
		wait.until(ExpectedConditions.alertIsPresent());
		driver.switchTo().alert().accept();
		driver.switchTo().defaultContent();
		/*wait = new WebDriverWait(driver, 5);
		wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath(xPath)));*/
	}
	
	public void gotoUserPage(WebDriver driver){
		final String xPath = "/html/body/form/div/table[2]/tbody/tr/td[3]/img";
		WebDriverWait wait = new WebDriverWait(driver, 10);
		WebElement mylist = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xPath)));
		mylist.click();
		
	}
	
	public void loopPage(WebDriver driver){
		String mainWindowHandle = driver.getWindowHandle();
		String newWindowHandle = getNewWindowHandle(driver);
		WebDriver newDriver = driver.switchTo().window(newWindowHandle);
		
		final String xPath = "//*[@id='order02']/div[@class='mc']/table";
		WebDriverWait wait = new WebDriverWait(newDriver, 20);
		WebElement listTable = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xPath)));
		
		newDriver.close();
		driver.switchTo().window(mainWindowHandle);
	}

	private void clickNextPage(WebDriver driver){
		try {
			WebElement pager = driver.findElement(By.className("my_pg"));
			if(pager.isDisplayed()){
				List<WebElement> pageHrefs = pager.findElements(By.tagName("a"));
				WebElement nextPage = null;
				for(int n=pageHrefs.size()-2; n>=0; n--){
					WebElement tmpPage = pageHrefs.get(n);
					if(tmpPage.getText().equals("下一页")){
						nextPage = tmpPage;
						break;
					}
				}
				if(nextPage!=null){
					nextPage.findElement(By.tagName("span")).click();
					loopPage(driver);
				}
			}
		}
		catch (NoSuchElementException e) {
			logger.info(e.getMessage());
		}
	}
	
}
