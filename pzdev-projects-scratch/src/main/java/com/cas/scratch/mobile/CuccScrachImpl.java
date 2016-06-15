package com.cas.scratch.mobile;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.sac.scratch.AbstractScratch;
import com.sac.utility.common.GlobalConsts;

public class CuccScrachImpl extends AbstractScratch {
	
	protected final static String DATEFORMAT = GlobalConsts.DATETIME_FORMAT_STRING;
	private String driverName = "Chrome";
	private String scratchURL = "";
	private String userName = "";
	private String passWord = "";
	public CuccScrachImpl(){
		
	}

	@Override
	public void doScratch() {
		this.scratchURL = scratchElement.attributeValue("scratchUrl");
		WebDriver driver = getWebDriver(driverName);
		
		for(int i=0;i<userList.size();i++){
			driver.quit();
			driver = getWebDriver(driverName);
			driver.get(scratchURL);
			String userStr = userList.get(i);
			userName = userStr.split("	")[0];
			passWord = userStr.split("	")[1];
			runScratch(driver);
		}
		driver.quit();
	}
	
	public void runScratch(WebDriver driver){
		if(!login(driver)){
			driver.get(scratchURL);
			runScratch(driver);
		}else{
			//gotoUserPage(driver);
			//loopPage(driver);
			
			System.out.println("success");
			driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
			logout(driver);
			driver.close();
		}
	}
	
	public boolean login(WebDriver driver){
		try{
			final String xPath = "//*[@name='userName']";
			
			WebDriverWait wait = new WebDriverWait(driver, 30);
			WebElement usernameInput = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xPath)));
			
			usernameInput.clear();
			usernameInput.sendKeys(userName);
			WebElement passwordInput = driver.findElement(By.xpath("//*[@name='userPwd']"));
			passwordInput.clear();
			passwordInput.sendKeys(passWord);
			
			WebElement LoginButton = driver.findElement(By.xpath("//*[@id='login1']"));
			LoginButton.click();
			
			try{
				wait = new WebDriverWait(driver, 3);
				wait.until(ExpectedConditions.alertIsPresent());
				driver.switchTo().alert().dismiss();
				return false;
			}catch(Exception e){
				try{
					wait = new WebDriverWait(driver, 10);
					wait.until(ExpectedConditions.urlContains("http://www.10010.com"));
					return true;
				}catch(Exception e1){
					e1.printStackTrace();
				}
			}
			return false;
			
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}
	
	public void logout(WebDriver driver){
		String xPath = "//div[@class='toplink_02']/a";
		WebDriverWait wait = new WebDriverWait(driver, 20);
		WebElement logout = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xPath)));
		logout.click();
	}
	
	public void gotoUserPage(WebDriver driver){
		final String xPath = "/html/body/form/div/table[2]/tbody/tr/td[3]/img";
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
