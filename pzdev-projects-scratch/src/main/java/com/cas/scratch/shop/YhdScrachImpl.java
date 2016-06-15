package com.cas.scratch.shop;

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
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.cas.scratch.util.ImageParserUtil;
import com.cas.scratch.util.WebElementExtender;
import com.sac.scratch.AbstractScratch;
import com.sac.utility.common.FileUtil;
import com.sac.utility.common.GlobalConsts;

public class YhdScrachImpl extends AbstractScratch {
	
	protected final static String DATEFORMAT = GlobalConsts.DATETIME_FORMAT_STRING;
	private String scratchURL = "";
	private String source = "";
	private String userName = "";
	private String passWord = "";
	public YhdScrachImpl(){
		
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
			driver.get(scratchURL);
			runScratch(driver);
		}else{
			gotoUserPage(driver);
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			loopPage(driver);
			logout(driver);
		}
	}
	
	public boolean login(WebDriver driver){
		try{
			WebDriverWait wait = new WebDriverWait(driver, 10);
			WebElement usernameInput = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id='un']")));
			usernameInput.clear();
			usernameInput.sendKeys(userName);
			WebElement passwordInput = driver.findElement(By.xpath("//*[@id='pwd']"));
			passwordInput.sendKeys(passWord);
			
			WebElement authcode = driver.findElement(By.xpath("//*[@id='vcd_div']"));//if auth code display then quit
			System.out.println("authcode.isDisplayed:"+authcode.getCssValue("display"));
			if("block".equals(authcode.getCssValue("display"))){
				
				wait = new WebDriverWait(driver, 10);
				WebElement codeElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id='valid_code_pic']")));
				ImageParserUtil imageParserUtil = new ImageParserUtil("eng");
				String authCode = imageParserUtil.getAuthCodeText(WebElementExtender.captureElementPicture(codeElement));
				
				WebElement vcd = driver.findElement(By.xpath("//*[@id='vcd']"));
				vcd.clear();
				vcd.sendKeys(authCode);
			}
			
			WebElement LoginImageButton = driver.findElement(By.xpath("//*[@id='login_button']"));
			LoginImageButton.click();
		
			try{
				wait = new WebDriverWait(driver, 10);
				wait.until(ExpectedConditions.urlContains("http://www.yhd.com/"));
				return true;
			}catch(Exception e1){
				e1.printStackTrace();
			}
			return false;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}
	
	public void logout(WebDriver driver){
		WebDriverWait wait = new WebDriverWait(driver, 10);
		WebElement ttbar = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id='global_login']/div[1]")));
		Actions actions = new Actions(driver);
		actions.moveToElement(ttbar);
		actions.perform();
		
		wait = new WebDriverWait(driver, 5);
		WebElement logout = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id='global_login']/div[2]/a")));
		actions.moveToElement(logout);
		actions.click();
		actions.perform();
		
		wait = new WebDriverWait(driver, 5);
		wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//*[@id='global_login']")));
	}
	
	public void gotoUserPage(WebDriver driver){
		WebDriverWait wait = new WebDriverWait(driver, 10);
		WebElement ttbar = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id='glHdMyYhd']/a")));
		Actions actions = new Actions(driver);
		actions.moveToElement(ttbar);
		actions.perform();
		wait = new WebDriverWait(driver, 5);
		WebElement mylist = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id='glHdMyYhd']/div/ul/li[1]/a")));
		actions.moveToElement(mylist);
		actions.click();
		actions.perform();
	}
	
	public void loopPage(WebDriver driver){
		String mainWindowHandle = driver.getWindowHandle();
		String newWindowHandle = getNewWindowHandle(driver);
		WebDriver newDriver = driver.switchTo().window(newWindowHandle);
		
		final String xPath = "//*[@id='gridContent']";
		WebDriverWait wait = new WebDriverWait(newDriver, 20);
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xPath)));
		
		newDriver.close();
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
