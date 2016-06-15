package com.cas.scratch.mobile;

import java.io.File;
import java.text.DateFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Point;
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

public class CtccScrachImpl extends AbstractScratch {
	
	protected final static String DATEFORMAT = GlobalConsts.DATETIME_FORMAT_STRING;
	private String driverName = "";
	private String scratchURL = "";
	private String userName = "";
	private String passWord = "";
	public CtccScrachImpl(){
		
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
			gotoUserPage(driver);
			loopPage(driver);
			
			System.out.println("success");
			driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
			logout(driver);
			driver.close();
		}
	}
	
	public boolean login(WebDriver driver){
		try{
			
			WebDriverWait wait = new WebDriverWait(driver, 10);
			WebElement usernameInput = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id='txtAccount']")));
			usernameInput.clear();
			usernameInput.sendKeys(userName);
			
			WebElement txtShowPwd = driver.findElement(By.xpath("//*[@id='txtShowPwd']"));
			txtShowPwd.click();
			wait = new WebDriverWait(driver, 10);
			WebElement txtPassword = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id='txtPassword']")));
			txtPassword.clear();
			txtPassword.sendKeys(passWord);
			
			WebElement LoginButton = driver.findElement(By.xpath("//*[@id='loginbtn']"));
			LoginButton.click();
			
			try{
				wait = new WebDriverWait(driver, 10);
				wait.until(ExpectedConditions.urlContains("http://www.189.cn/sn/"));
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
		String xPath = "//div[@id='loginShow']/a[contains(text(),'退出')]";
		WebDriverWait wait = new WebDriverWait(driver, 20);
		WebElement logout = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xPath)));
		logout.click();
	}
	
	public void gotoUserPage(WebDriver driver){
		final String xPath = "//li[@class='down_05']";
		WebDriverWait wait = new WebDriverWait(driver, 30);
		WebElement down_05 = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xPath)));
		
		Actions actions = new Actions(driver);
		actions.moveToElement(down_05);
		actions.perform();
		final String xPath2 = "//a[@href='http://www.189.cn/dqmh/my189/initMy189home.do?fastcode=10000199']";

		wait = new WebDriverWait(driver, 10);
		WebElement billQuery = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xPath + xPath2)));

		actions.moveToElement(billQuery);
		actions.click();
		actions.perform();
		//driver.get("http://www.189.cn/dqmh/my189/initMy189home.do?fastcode=10000199");
		
	}
	
	public void loopPage(WebDriver driver){
		String mainWindowHandle = driver.getWindowHandle();
		String newWindowHandle = getNewWindowHandle(driver);
		WebDriver newDriver = driver.switchTo().window(newWindowHandle);
		newDriver.manage().window().maximize();
		
		newDriver.switchTo().frame("bodyIframe");
		WebDriverWait wait = new WebDriverWait(driver, 60);
		WebElement feeTypeDiv = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@id='feeTypeDiv']")));
		
		Actions actions = new Actions(driver);
		actions.moveToElement(feeTypeDiv);
		actions.perform();
		
		List<WebElement> listTr = feeTypeDiv.findElements(By.xpath("table[1]/tbody/tr']"));
		for(int i=1;i<listTr.size();i++){
			WebElement tr = listTr.get(i);
			List<WebElement> listTd = tr.findElements(By.xpath("td"));
			listTd.get(4).click();
			loopDetailPage(driver);
		}
		
		newDriver.close();
		driver.switchTo().window(mainWindowHandle);
	}
	
	public void loopDetailPage(WebDriver driver){
		String mainWindowHandle = driver.getWindowHandle();
		String newWindowHandle = getNewWindowHandle(driver);
		WebDriver newDriver = driver.switchTo().window(newWindowHandle);
		newDriver.manage().window().maximize();
		
		WebDriverWait wait = new WebDriverWait(driver, 30);
		WebElement billContent = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id='billContent']")));
		WebElement totalFee  = billContent.findElement(By.xpath("tr[@id='totalFee']"));
		System.out.println(totalFee);
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
