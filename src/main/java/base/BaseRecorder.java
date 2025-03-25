package base;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.ITestContext;
import org.testng.annotations.*;
import org.testng.xml.XmlSuite;
import recorder.ParasoftRecorder;
import utils.*;

import io.github.bonigarcia.wdm.WebDriverManager;

import java.util.List;

public class BaseRecorder {
    private ParasoftRecorder recorder;
    public WebDriver driver;

    @BeforeTest
    @Parameters({"url_base"})
    public void beforeTest(String url_base) {
        WebDriverManager.chromedriver().setup();
        System.setProperty("webdriver.chrome.driver", "C:\\Users\\Administrator\\Documents\\chromedriver\\chromedriver.exe");

        recorder = new ParasoftRecorder("localhost","9080","localhost","40090");
        recorder.deleteAllSessions();

        ChromeOptions opts = new ChromeOptions();
        opts.addArguments("--start-maximized");
        opts.addArguments("--disable-geolocation");
        // opts.addArguments("--incognito");
        opts.addArguments("--enable-strict-powerful-feature-restrictions");
        opts.addArguments("--remote-allow-origins=*");
        opts.addArguments("--ignore-certificate-errors");


        driver = recorder.startRecordingAndSetupChromeDriver(opts);

        driver.manage().window().maximize();
        driver.get(url_base);
    }

    @AfterTest
    @Parameters({"job_name","soatest_server","smoketest"})
    public void afterTest(ITestContext context, String job_name, String soatest_server, String smoketest) {
        String testClassName = this.getClass().getSimpleName();
        recorder.stopRecordingAndCreateTST(testClassName);

        XmlSuite suiteName = context.getCurrentXmlTest().getSuite();
        String suiteFilePath = suiteName.getFileName();

        List<String> clases = XMLManager.obtenerClasesDesdeXML(suiteFilePath);

//        String jobId = Jobs.obtenerJobId(job_name);
//        System.out.println("*************************************");
//        System.out.println(jobId);
//        if(jobId == null){
//            jobId = Jobs.crearJob(job_name, "", Constants.CTP_USER);
//        }
//
//        List<String> tsts = Servers.obtenerIds(clases,soatest_server);
//
//        Jobs.agregarTstAJob(jobId, tsts);

        System.out.println(suiteFilePath);
        if (driver != null) {
            driver.quit();
        }

        // Si está activada la opción de smoketest, se ejecuta el job
        if (smoketest.equals("true")){
            RESTClient.GET("http://base.laboratorytechnologylatam.com:8080/generic-webhook-trigger/invoke?token=selenic&nombreTst=users%5Canonymous%5C"+testClassName+".tst");
        }
    }
}
