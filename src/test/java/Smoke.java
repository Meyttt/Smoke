import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by a.chebotareva on 11.05.2017.
 */
public class Smoke {

    ChromeDriver chromeDriver;
    Config config = new Config("config.properties");
    WebDriverWait wait;

    public Smoke() throws IOException {
    }
////*[@id="wrapper"]/ng-component/div[1]/div[1]/div[2]/div/div/div/div/div/div/p
    @BeforeClass
    private void initChromeDriver(){
        System.setProperty("webdriver.chrome.driver", "data/chromedriver.exe");
        HashMap<String, Object> chromePrefs = new HashMap<String, Object>();
        chromePrefs.put("profile.default_content_settings.popups", 0);
        chromePrefs.put("download.default_directory", (new File("data/docs")).getAbsolutePath());
        chromePrefs.put("plugins.plugins_disabled", new String[] {
                "Adobe Flash Player",
                "Chrome PDF Viewer"
        });
        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("prefs", chromePrefs);
        DesiredCapabilities cap = DesiredCapabilities.chrome();
        cap.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
        cap.setCapability(ChromeOptions.CAPABILITY, options);
        chromeDriver = new ChromeDriver(cap);
        wait = new WebDriverWait(chromeDriver, 15);
    }

    @Test
    void PageAndLinks(){
        //наличие надписи
        chromeDriver.get(config.get("url"));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"wrapper\"]/div[1]/div/div/div/div[1]/h3")));
        Assert.assertTrue(Pattern.matches("Данный ресурс предназначен[\\w\\s]+для операторов связи",chromeDriver.findElementByXPath("//*[@id=\"wrapper\"]/ng-component/div[1]/div[1]/div[2]/div/div/div/div/div/div/p").getText()));
        //Проверка ссылки "О связи"
        chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/div[2]/div/div[3]/div/div/p/button[1]")).click();
//        chromeDriver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"on_communication\"]/div/div[1]/h3")));
        Assert.assertTrue(Pattern.matches("Обязанности операторов[\\w\\s]+связи",chromeDriver.findElementByXPath("//*[@id=\"on_communication\"]/div/div[1]/h3").getText()));
        chromeDriver.findElement(By.cssSelector("a[title='Close']")).click();


    }
    @AfterClass
    public void closeDriver(){
        if(chromeDriver!=null){
            chromeDriver.close();
            chromeDriver.quit();
        }
    }
}
