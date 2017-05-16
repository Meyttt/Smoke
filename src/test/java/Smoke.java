import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by a.chebotareva on 11.05.2017.
 */
public class Smoke {

    ChromeDriver chromeDriver;
    Config config = new Config("config.properties");
    WebDriverWait wait;
    File file = new File("data/docs");

    public Smoke() throws IOException {
    }
////*[@id="wrapper"]/ng-component/div[1]/div[1]/div[2]/div/div/div/div/div/div/p
    @BeforeClass
    private void initChromeDriver(){
        System.setProperty("webdriver.chrome.driver", "data/chromedriver.exe");
        HashMap<String, Object> chromePrefs = new HashMap<String, Object>();
        chromePrefs.put("profile.default_content_settings.popups", 0);
        chromePrefs.put("download.default_directory", (file.getAbsolutePath()));
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

    @BeforeClass@AfterClass
    void cleanDirectory(){
       File[] files=file.listFiles();
       for(int i=0;i<files.length;i++){
           files[i].delete();
       }
    }
    @Test(description = "Просмотр страниц и ссылок в открытой части портала")
    void PageAndLinks(){
        //наличие надписи
        chromeDriver.get(config.get("url"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"wrapper\"]/div[1]/div/div/div/div[1]/h3")));
        Assert.assertTrue(Pattern.matches("Данный ресурс предназначен[\\w\\s]+для операторов связи",chromeDriver.findElementByXPath("//*[@id=\"wrapper\"]/ng-component/div[1]/div[1]/div[2]/div/div/div/div/div/div/p").getText()));
        //Проверка ссылки "О связи"
        chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/div[2]/div/div[3]/div/div/p/button[1]")).click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"on_communication\"]/div/div[1]/h3")));
        Assert.assertTrue(Pattern.matches("Обязанности операторов[\\w\\s]+связи",chromeDriver.findElementByXPath("//*[@id=\"on_communication\"]/div/div[1]/h3").getText()));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a[title='Close']")));
        chromeDriver.findElement(By.cssSelector("a[title='Close']")).click();
        chromeDriver.manage().timeouts().implicitlyWait(100,TimeUnit.MILLISECONDS);
        //Проверка "КоАП"
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"wrapper\"]/div[2]/div/div[3]/div/div/p/button[2]")));
        chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/div[2]/div/div[3]/div/div/p/button[2]")).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"provide_info\"]/div/div[1]/h3")));
        Assert.assertTrue(Pattern.matches("Ответственность за[\\w\\s]+непредоставление сведений",chromeDriver.findElementByXPath("//*[@id=\"provide_info\"]/div/div[1]/h3").getText()));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a[title='Close']")));
        chromeDriver.findElement(By.cssSelector("a[title='Close']")).click();
        chromeDriver.manage().timeouts().implicitlyWait(100,TimeUnit.MILLISECONDS);
        //Переходим на страницу "Статистика"
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("a[ng-reflect-router-link='statistics']")));
        chromeDriver.findElement(By.cssSelector("a[ng-reflect-router-link='statistics']")).click();
        //cкачиваем файл
        WebElement webElement =chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/div[2]/div[4]/ul[2]/li[1]/div/a"));
        Assert.assertEquals(webElement.findElement(By.tagName("h4")).getText(),"Абоненты ШПД (I квартал 2016 года)");
        webElement.click(); //TODO: добавить проверку, скачалось ли
        //Переходим на страницу "События и документы"
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("a[ng-reflect-router-link='events']")));
        chromeDriver.findElement(By.cssSelector("a[ng-reflect-router-link='events']")).click();
        //На странице присутствуют строки "События", "Письмо Минкомсвязи России о порядке предоставления статистической отчетности"
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.partialLinkText("События")));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"wrapper\"]/ng-component/div[2]/div[2]/ul/li[1]/div/div[2]/p")));
        Assert.assertTrue(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/div[2]/div[2]/ul/li[1]/div/div[2]/p")).getText().contains("Письмо Минкомсвязи России о порядке предоставления статистической отчетности"));
        //Переходим на вкладку "Документы"
        chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/div[2]/div[1]/div/div/div/ul/li[2]")).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"wrapper\"]/ng-component/div[2]/div[3]/ul/li[1]/div/a")));
        //На странице присутствуют строки "Документы", "Письмо Минкомсвязи России о порядке предоставления статистической отчетности"
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"wrapper\"]/ng-component/div[2]/div[1]/div/div/div/ul/li[2]")));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"wrapper\"]/ng-component/div[2]/div[3]/ul/li[1]/div/a/h4")));
        Assert.assertTrue(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/div[2]/div[3]/ul/li[1]/div/a/h4")).getText().contains("Письмо Минкомсвязи России от 29.12.2016 №НН-П14-062-28741 «О порядке"));
        Assert.assertTrue(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/div[2]/div[3]/ul/li[1]/div/a/h4")).getText().contains("представления статистической отчетности»"));
        //Скачивается файл %D0%9D%D0%9D-%D0%9F14-062-28741.rar по ссылке Письмо Минкомсвязи России от 29.12.2016 №НН-П14-062-28741 «О порядке представления статистической отчетности»
        chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/div[2]/div[3]/ul/li[1]/div/a")).click();
        //Переходим на страницу "Вопросы и ответы"
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("a[ng-reflect-router-link='faq']")));
        chromeDriver.findElement(By.cssSelector("a[ng-reflect-router-link='faq']")).click();
        //На странице присутствуют строки "Часто задаваемые вопросы", "Где можно ознакомиться с порядком предоставления статистической отчетности в сфере связи?"
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("blc_frequently_questions_ttl")));
        Assert.assertTrue(chromeDriver.findElement(By.className("blc_frequently_questions_ttl")).findElement(By.tagName("h3")).getText().equals("Часто задаваемые вопросы"));
        Assert.assertTrue(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ul/li[1]/div[1]/h4")).getText().contains("Где можно ознакомиться с порядком предоставления статистической отчетности в сфере связи?"));
        //Переходим на страницу "Контакты"
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("a[ng-reflect-router-link='contacts']")));
        chromeDriver.findElement(By.cssSelector("a[ng-reflect-router-link='contacts']")).click();
        //"На странице присутствуют строки ""Адрес 125375, г. Москва, ул. Тверская, д. 7"""
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"wrapper\"]/ng-component/div[2]/div/div/ul/li[1]/div/p")));
        Assert.assertTrue(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/div[2]/div/div/ul/li[1]/div/p")).getText().contains("Адрес"));
        Assert.assertTrue(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/div[2]/div/div/ul/li[1]/div/span[1]")).getText().contains("125375, г. Москва,"));
        Assert.assertTrue(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/div[2]/div/div/ul/li[1]/div/span[2]")).getText().contains("ул. Тверская, д. 7"));
    }
    @Test(description = "Просмотр форм для сдачи при вводе лицензии")
    void numberOfForms() throws InterruptedException {
        //переход на страницу и ожидание загрузки
        chromeDriver.get(config.get("url"));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("licNumber")));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("selectService")));
        //раскрытие меню
        while (chromeDriver.findElements(By.xpath("//*[@id=\"selectService_chosen\"]/div/ul/li[.]")).size()==0){
            chromeDriver.findElement(By.xpath("//*[@id=\"selectService_chosen\"]/a/div/b")).click();
            while(!chromeDriver.findElements(By.xpath("//*[@id=\"selectService_chosen\"]/div/ul/li[.]")).get(0).isDisplayed()) {
                chromeDriver.findElement(By.xpath("//*[@id=\"selectService_chosen\"]/a/div/b")).click();
                chromeDriver.manage().timeouts().implicitlyWait(100, TimeUnit.MILLISECONDS);
            }
        }
        //ввод номера лицензии
        chromeDriver.findElement(By.xpath("//*[@id=\"licNumber\"]")).sendKeys(config.get("licenseNumber"));
        //выбор услуги из выпадающего списка
        List<WebElement> services = chromeDriver.findElements(By.xpath("//*[@id=\"selectService_chosen\"]/div/ul/li[.]"));
        for(WebElement webElement:services){
            if(webElement.getText().equals("Телематические услуги связи тест")){
                clicking:while(true){
                    try {
                        webElement.click();
                        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"wrapper\"]/ng-component/home-form/div[1]/div/div/div/div/button[1]")));
                        break clicking;
                    }catch (org.openqa.selenium.TimeoutException e){
                        continue clicking;
                    }
                }
            }
        }

        //отправка данных
        chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/home-form/div[1]/div/div/div/div/button[1]")).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"wrapper\"]/ng-component/home-form/div[2]/div[1]/h3")));
        //проверка общего числа форм.
        //todo: проверять не только общее число форм, но и число в каждой группе.
        Assert.assertEquals(chromeDriver.findElements(By.xpath("//*[@id=\"wrapper\"]/ng-component/home-form/div[2]/div[.]/div[2]/span")).size(),10);

    }

    @AfterClass
    public void closeDriver(){
        if(chromeDriver!=null){
            chromeDriver.close();
            chromeDriver.quit();
        }
    }

}
