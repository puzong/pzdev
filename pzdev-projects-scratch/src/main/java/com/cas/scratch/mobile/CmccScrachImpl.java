package com.cas.scratch.mobile;

import java.io.File;
import java.text.DateFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.cas.scratch.util.ImageCleaner;
import com.cas.scratch.util.ImageParserUtil;
import com.cas.scratch.util.WebElementExtender;
import com.sac.scratch.AbstractScratch;
import com.sac.utility.common.FileUtil;
import com.sac.utility.common.GlobalConsts;

public class CmccScrachImpl extends AbstractScratch {
	
	protected final static String DATEFORMAT = GlobalConsts.DATETIME_FORMAT_STRING;
	private String scratchURL = "";
	private String source = "";
	private String userName = "";
	private String passWord = "";
	public CmccScrachImpl(){
		
	}

	@Override
	public void doScratch() {
		this.scratchURL = scratchElement.attributeValue("scratchUrl");
		this.source = scratchElement.attributeValue("source");
		WebDriver driver = getWebDriver();
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
		
		if(!login(driver)){
			//driver.quit();
			//driver = getWebDriver();
			driver.get(scratchURL);
			runScratch(driver);
		}else{
			//gotoUserPage(driver);
			//loopPage(driver);
			//driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
			System.out.println("sucess");
			logout(driver);
		}
		
		/*if(driver.getCurrentUrl().contains("http://www.sh.10086.cn/sh/my/")){
			
		}*/
	}
	
	public boolean login(WebDriver driver){
		try{
			if(driver.getCurrentUrl().contains("http://www.sh.10086.cn/sh/my/")){
				return true;
			}
			String xPath = "//*[@id='w_telno']";
			
			WebDriverWait wait = new WebDriverWait(driver, 10);
			WebElement usernameInput = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xPath)));
			
			usernameInput.clear();
			usernameInput.sendKeys(userName);
			WebElement passwordInput = driver.findElement(By.xpath("//*[@id='w_fwmm']"));
			passwordInput.sendKeys(passWord);
			
			//enterAuthCode(driver);
			
			wait = new WebDriverWait(driver, 10);
			WebElement codeElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@class='wt-login-table']/tbody/tr[6]/td[3]/img")));
			ImageParserUtil imageParserUtil = new ImageParserUtil("eng");
			File imageFile = WebElementExtender.captureElementPicture(codeElement);
			
			ImageCleaner.thresholdImage(imageFile,imageFile, 170, 255);
			ImageCleaner.removeVerticalLines(imageFile,imageFile, 1, 1);
			String authCode = imageParserUtil.getAuthCodeText(imageFile);
			System.out.println(authCode);
			if(authCode.length()<4)	return false;
			
			WebElement userjym = driver.findElement(By.xpath("//*[@id='w_yzm']"));
			userjym.sendKeys(authCode);
			
			WebElement LoginImageButton = driver.findElement(By.xpath("//*[@class='wt-login-btn']"));
			LoginImageButton.click();
			
		/*	userjym = driver.findElement(By.xpath("//*[@id='w_yzm']"));
			System.out.println("userjym:"+userjym.getText());
			if("".equals(userjym.getText().trim())){
				login(driver);
			}*/
			try{
				wait = new WebDriverWait(driver, 6);
				wait.until(ExpectedConditions.urlContains("http://www.sh.10086.cn/sh/my/"));
				return true;
			}catch(Exception e){
				logger.info("Try to login failed!");
			}
			return false;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
			
		
	}
	private void enterAuthCode(WebDriver driver){
		
	}
	
	public void logout(WebDriver driver){
		final String xPath = "//*[@id='logout']";
		WebDriverWait wait = new WebDriverWait(driver, 30);
		WebElement logout = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xPath)));
		logout.click();
	}
	
	public void gotoUserPage(WebDriver driver){
		
		final String xPath = "//*[@class='myacc-zhandan']";
		WebDriverWait wait = new WebDriverWait(driver, 30);
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
		
		//System.out.println(listTable.getAttribute("innerHTML"));
		//List<WebElement> trs = listTable.findElements(By.xpath(xPath+"/tbody/tr"));
		
		/*for (int i = 0; i < trs.size(); i++) {
			WebElement tr = trs.get(i);
		}*/
		
		//closeWindow(newDriver, newWindowHandle);
		newDriver.close();
		//handles.remove(detailWindowHandle);
		driver.switchTo().window(mainWindowHandle);
		//if(!isOver)	clickNextPage(driver);
		//return driver;
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
