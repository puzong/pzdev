package com.cas.scratch.sb;

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

public class SbScrachImpl extends AbstractScratch {
	
	protected final static String DATEFORMAT = GlobalConsts.DATETIME_FORMAT_STRING;
	private String scratchURL = "";
	private String userName = "";
	private String passWord = "";
	public SbScrachImpl(){
		
	}

	@Override
	public void doScratch() {
		this.scratchURL = scratchElement.attributeValue("scratchUrl");
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
			WebElement iframe = driver.findElement(By.xpath("//*[@id='iFrame2']"));
			Point ifrmaePoint = iframe.getLocation();
			driver.switchTo().frame("iFrame2");
			final String xPath = "//*[@name='userid']";
			
			WebDriverWait wait = new WebDriverWait(driver, 30);
			WebElement usernameInput = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xPath)));
			
			usernameInput.clear();
			usernameInput.sendKeys(userName);
			WebElement passwordInput = driver.findElement(By.xpath("//*[@name='userpw']"));
			passwordInput.sendKeys(passWord);
			
			wait = new WebDriverWait(driver, 10);
			WebElement codeElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@name='loginForm']/table/tbody/tr[4]/td[2]/img")));
			ImageParserUtil imageParserUtil = new ImageParserUtil("eng");
			File imageFile = WebElementExtender.captureElementPicture(ifrmaePoint, codeElement);
			ImageCleaner.removeVerticalLines(imageFile, imageFile);
			String authCode = imageParserUtil.getAuthCodeText(imageFile);
			System.out.println(authCode);
			if(authCode.length()<4)	return false;
			
			WebElement userjym = driver.findElement(By.xpath("//*[@id='userjym']"));
			userjym.sendKeys(authCode);
			
			WebElement LoginImageButton = driver.findElement(By.xpath("//*[@name='loginForm']/table/tbody/tr[6]/td[2]/img"));
			LoginImageButton.click();
		
			wait = new WebDriverWait(driver, 10);
			wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath(xPath)));
			WebElement resultElement = driver.findElement(By.xpath("/html/body/table/tbody/tr/td/span"));
			if(resultElement.getText().contains("登陆成功")){
				driver.switchTo().defaultContent();
				return true;
			}else{
				return false;
			}
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}
	
	public void logout(WebDriver driver){
		final String xPath = "/html/body/form/div/table[2]/tbody/tr/td[3]/img";
		WebDriverWait wait = new WebDriverWait(driver, 30);
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
