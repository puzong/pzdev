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
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.sac.scratch.AbstractScratch;
import com.sac.utility.common.FileUtil;
import com.sac.utility.common.GlobalConsts;

public class JingdongScrachImpl extends AbstractScratch {
	
	protected final static String DATEFORMAT = GlobalConsts.DATETIME_FORMAT_STRING;
	private String scratchURL = "";
	private String source = "";
	private String userName = "";
	private String passWord = "";
	public JingdongScrachImpl(){
		
	}

	@Override
	public void doScratch() {
		this.scratchURL = scratchElement.attributeValue("scratchUrl");
		this.source = scratchElement.attributeValue("source");
		WebDriver driver = getWebDriver();
		driver.get(scratchURL);
		for(int i=0;i<userList.size();i++){
			String userStr = userList.get(i);
			userName = userStr.split("	")[0];
			passWord = userStr.split("	")[1];
			runScratch(driver);
		}
		driver.quit();
	}
	
	public void runScratch(WebDriver driver){
		if(!login(driver)){
			driver.quit();
			driver = getWebDriver();
			driver.get(scratchURL);
			runScratch(driver);
			
		}else{
			gotoUserPage(driver);
			loopPage(driver);
			driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
			logout(driver);
		}
	}
	
	public boolean login(WebDriver driver){
		final String xPath = "//*[@id='loginname']";
		
		WebDriverWait wait = new WebDriverWait(driver, 30);
		WebElement usernameInput = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xPath)));
		
		usernameInput.clear();
		usernameInput.sendKeys(userName);
		WebElement passwordInput = driver.findElement(By.xpath("//*[@id='nloginpwd']"));
		passwordInput.sendKeys(passWord);
		
		WebElement LoginImageButton = driver.findElement(By.xpath("//*[@id='loginsubmit']"));
		LoginImageButton.click();
		
		(new WebDriverWait(driver, 30)).until(new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver d) {
				try{
					WebElement authcode = d.findElement(By.xpath("//*[@id='o-authcode']"));
					return "block".equals(authcode.getCssValue("display"));
				}catch(Exception e){
					return true;
				}
			}
		});
		try{
			WebElement authcode = driver.findElement(By.xpath("//*[@id='o-authcode']"));//if auth code display then quit
			System.out.println("authcode.isDisplayed:"+authcode.getCssValue("display"));
			if("block".equals(authcode.getCssValue("display"))){
				return false;
			}else{
				return true;
			}
		}catch(Exception e){
			return true;
		}
	}
	
	public void logout(WebDriver driver){
		final String xPath = "//*[@id='ttbar-login']";
		WebDriverWait wait = new WebDriverWait(driver, 30);
		WebElement ttbar = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xPath)));
		WebElement logout = ttbar.findElement(By.xpath("a[@class='link-logout']"));
		logout.click();
	}
	
	public void gotoUserPage(WebDriver driver){
		final String xPath = "//*[@id='shortcut-2014']/div/ul[@class='fr']/li[@class='fore2']/div/a";
		
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
