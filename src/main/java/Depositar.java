import base.BaseRecorder;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import page.CheckingviewPage;
import page.DepositPage;
import page.DigitalBankPage;
import page.HomePage;

import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;


public class Depositar extends BaseRecorder {

    @Test
    public void testDepositar() throws Throwable {


        DigitalBankPage digitalBankPage = new DigitalBankPage(driver);
        digitalBankPage.setUsernameText("jsmith@demo.io");
        digitalBankPage.setPassword("Demo123!");
        digitalBankPage.clickSignInSubmit();


        HomePage homePage = new HomePage(driver);
        homePage.clickDepositLink();


        DepositPage depositPage = new DepositPage(driver);
        depositPage.selectIdSelectOne("Joint Checking (Standard Checking)");
        depositPage.setAmountText("10");
        depositPage.clickSubmit();


        CheckingviewPage checkingviewPage = new CheckingviewPage(driver);
        assertEquals("View Checking Accounts", checkingviewPage.getPageTitleFieldText());
    }
}