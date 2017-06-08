import ExcelFun.ExcelParser;
import mail.MailClient;
import org.junit.Assert;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import sql.SqlManager;
import utils.MainUtil;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * Created by a.chebotareva on 11.05.2017.
 */
public class Smoke {

    ChromeDriver chromeDriver;
    Config config = new Config("config.properties");
    Config userConfig = new Config("userInfo.properties");
    Config databaseConfig = new Config("database.properties");
    WebDriverWait wait;
    File file = new File("data/docs");

    public Smoke() throws IOException {
    }

    ////*[@id="wrapper"]/ng-component/div[1]/div[1]/div[2]/div/div/div/div/div/div/p
    @BeforeClass
    private void initChromeDriver() {
        System.setProperty("webdriver.chrome.driver", "data/chromedriver.exe");
        HashMap<String, Object> chromePrefs = new HashMap<String, Object>();
        chromePrefs.put("profile.default_content_settings.popups", 0);
        chromePrefs.put("download.default_directory", (file.getAbsolutePath()));
        chromePrefs.put("plugins.plugins_disabled", new String[]{
                "Adobe Flash Player",
                "Chrome PDF Viewer"
        });
        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("prefs", chromePrefs);
        options.addExtensions(new File("data/cp.crx"));
        DesiredCapabilities cap = DesiredCapabilities.chrome();
        cap.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
        cap.setCapability(ChromeOptions.CAPABILITY, options);
        chromeDriver = new ChromeDriver(cap);
        wait = new WebDriverWait(chromeDriver, 15);
        wait.pollingEvery(100, TimeUnit.MILLISECONDS);
    }

    @BeforeClass
    @AfterClass
    void cleanDirectory() {
        if(!file.exists()) {
            file.mkdir();
            return;
        }
        File[] files = file.listFiles();
        if(files.length==0){
            return;
        }
        for (int i = 0; i < files.length; i++) {
            files[i].delete();
        }
    }

    @Test(description = "Просмотр страниц и ссылок в открытой части портала")
    void PageAndLinks() {
        //наличие надписи
        chromeDriver.get(config.get("url"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"wrapper\"]/div[1]/div/div/div/div[1]/h3")));
        Assert.assertTrue(Pattern.matches("Данный ресурс предназначен[\\w\\s]+для операторов связи", chromeDriver.findElementByXPath("//*[@id=\"wrapper\"]/ng-component/div[1]/div[1]/div[2]/div/div/div/div/div/div/p").getText()));
        //Проверка ссылки "О связи"
        chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/div[2]/div/div[3]/div/div/p/button[1]")).click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"on_communication\"]/div/div[1]/h3")));
        Assert.assertTrue(Pattern.matches("Обязанности операторов[\\w\\s]+связи", chromeDriver.findElementByXPath("//*[@id=\"on_communication\"]/div/div[1]/h3").getText()));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a[title='Close']")));
        chromeDriver.findElement(By.cssSelector("a[title='Close']")).click();
        chromeDriver.manage().timeouts().implicitlyWait(100, TimeUnit.MILLISECONDS);
        //Проверка "КоАП"
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"wrapper\"]/div[2]/div/div[3]/div/div/p/button[2]")));
        chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/div[2]/div/div[3]/div/div/p/button[2]")).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"provide_info\"]/div/div[1]/h3")));
        Assert.assertTrue(Pattern.matches("Ответственность за[\\w\\s]+непредоставление сведений", chromeDriver.findElementByXPath("//*[@id=\"provide_info\"]/div/div[1]/h3").getText()));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a[title='Close']")));
        chromeDriver.findElement(By.cssSelector("a[title='Close']")).click();
        chromeDriver.manage().timeouts().implicitlyWait(100, TimeUnit.MILLISECONDS);
        //Переходим на страницу "Статистика"
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("a[ng-reflect-router-link='statistics']")));
        chromeDriver.findElement(By.cssSelector("a[ng-reflect-router-link='statistics']")).click();
        //cкачиваем файл
        WebElement webElement = chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/div[2]/div[4]/ul[2]/li[1]/div/a"));
        Assert.assertEquals(webElement.findElement(By.tagName("h4")).getText(), "Абоненты ШПД (I квартал 2016 года)");
        webElement.click(); //TODO: добавить проверку, скачалось ли
        //Переходим на страницу "События и документы"
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("a[ng-reflect-router-link='events']")));
        chromeDriver.findElement(By.cssSelector("a[ng-reflect-router-link='events']")).click();
        //На странице присутствуют строки "События", "Письмо Минкомсвязи России о порядке предоставления статистической отчетности"
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.partialLinkText("События")));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"wrapper\"]/ng-component/div[2]/div[2]/ul/li[1]/div/div[2]/p")));
        Assert.assertTrue(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/div[2]/div[2]/ul/li[1]/div/div[2]/p")).getText().contains("Информационное письмо Минкомсвязи России о недопустимости использования посреднических услуг."));
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
        try {
            Thread.sleep(20000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //todo
        Assert.assertEquals(file.listFiles().length, 2);
    }

    @Test(description = "Просмотр форм для сдачи при вводе лицензии")
    void numberOfForms() throws InterruptedException {
        //переход на страницу и ожидание загрузки
        chromeDriver.get(config.get("url"));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("licNumber")));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("selectService")));
        //ввод номера лицензии
        chromeDriver.findElement(By.xpath("//*[@id=\"licNumber\"]")).sendKeys(config.get("licenseNumber"));
        //раскрытие меню
        while (chromeDriver.findElements(By.xpath("//*[@id=\"selectService_chosen\"]/div/ul/li[.]")).size() == 0) {
            chromeDriver.findElement(By.xpath("//*[@id=\"selectService_chosen\"]/a/div/b")).click();
            try {
                chromeDriver.manage().timeouts().implicitlyWait(500, TimeUnit.MILLISECONDS);
                while (!chromeDriver.findElement(By.xpath("//*[@id=\"selectService_chosen\"]/div/ul/li[.]")).isDisplayed()) {//todo: org.openqa.selenium.StaleElementReferenceException: stale element reference: element is not attached to the page document
                    chromeDriver.findElement(By.xpath("//*[@id=\"selectService_chosen\"]/a/div/b")).click();
                    chromeDriver.manage().timeouts().implicitlyWait(100, TimeUnit.MILLISECONDS);
                }
            } catch (StaleElementReferenceException e) {
                e.printStackTrace();//todo: надо ли?
//                System.err.println("Not ref, info:");
//                System.out.println(chromeDriver.findElements(By.xpath("//*[@id=\"selectService_chosen\"]/div/ul/li[.]")).size());
            }
        }

        //выбор услуги из выпадающего списка
        String webElementText = null;
        circle:
        while (true) {
            try {
                List<WebElement> services = chromeDriver.findElements(By.xpath("//*[@id=\"selectService_chosen\"]/div/ul/li[.]"));
                for (WebElement webElement : services) {
                    if (webElement.getText().equals("Телематические услуги связи тест")) { //org.openqa.selenium.StaleElementReferenceException: stale element reference: element is not attached to the page document
                        clicking:
                        while (true) {
                            try {
                                webElement.click();
                                wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"wrapper\"]/ng-component/home-form/div[1]/div/div/div/div/button[1]")));
                                wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"wrapper\"]/ng-component/home-form/div[1]/div/div/div/div/button[1]")));
                                break clicking;
                            } catch (org.openqa.selenium.TimeoutException e) {
                                continue clicking;
                            }
                        }
                    }
                }
                break circle;
            } catch (StaleElementReferenceException e) {
                continue circle;
            }

        }
        //отправка данных
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"wrapper\"]/ng-component/home-form/div[1]/div/div/div/div/button[1]"))).click();
//        chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/home-form/div[1]/div/div/div/div/button[1]")).click(); //todo: org.openqa.selenium.WebDriverException: unknown error: Element <button class="ln_start discover_btn" disabled="">...</button> is not clickable at point (455, 648). Other element would receive the click: <li class="active-result" data-option-array-index="4">...</li>

        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"wrapper\"]/ng-component/home-form/div[2]/div[1]/h3")));
        //проверка общего числа форм.

        Assert.assertEquals(chromeDriver.findElements(By.xpath("//*[@id=\"wrapper\"]/ng-component/home-form/div[2]/div[.]/div[2]/span")).size(), 12);
    }

    @Test(description = "")
    public void userActivation() {
        //Переход в Армаду
        chromeDriver.get(
                config.get("urlArmada"));
        chromeDriver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
        //ввод учетных данных при необходимости
        if (chromeDriver.findElements(By.xpath("//*[@id=\"username\"]")).size() > 0) {
            chromeDriver.findElement(By.xpath("//*[@id=\"username\"]")).sendKeys(config.get("armadaLog"));
            chromeDriver.findElement(By.xpath("//*[@id=\"password\"]")).sendKeys(config.get("armadaPass"));
            chromeDriver.findElement(By.xpath("/html/body/div[3]/div/div/form/fieldset/div[3]/div[2]/button")).click();
        }
        //Переход на страницу "Пользователи"
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/div[4]/div/div/div[1]/div[1]/h2")));
        chromeDriver.findElement(By.xpath("//*[@id=\"navbar-collapse\"]/ul[1]/li[2]/a")).click();
        List<WebElement> dictionaries = chromeDriver.findElements(By.xpath("//*[@id=\"navbar-collapse\"]/ul[1]/li[2]/ul/li[.]/a"));
        for (WebElement webElement : dictionaries) {
            if (webElement.getText().equals("Пользователи")) {
                webElement.click();
//                wait.until(ExpectedConditions.textToBePresentInElement(chromeDriver.findElement(By.xpath("/html/body/div[4]/div/div/div[1]/div/h2")),"Пользователи"));
                wait.until(ExpectedConditions.elementToBeClickable(By.xpath("/html/body/div[4]/div/div/div[2]/div/div[1]/nav/a[1]")));
            }
        }
        chromeDriver.findElement(By.xpath("/html/body/div[4]/div/div/div[2]/div/div[1]/nav/a[1]")).click();
        //удаление из бд пользователей, имеющих е-меил, используемый для регистрации
        SqlManager sqlManager = new SqlManager(databaseConfig.get("url"), databaseConfig.get("name"), databaseConfig.get("password"));
        sqlManager.deleteOperatorHistory(userConfig.get("mail"));
        sqlManager.changeUser(userConfig.get("mail"));
        //открытие окна "Создание пользователя"
        wait.until(ExpectedConditions.textToBePresentInElement(chromeDriver.findElement(By.xpath("/html/body/div[5]/div/div/div[1]/h4")), "Создание пользователя"));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/div[5]/div/div/div[2]/form/fieldset/div[1]/div/input")));
        //Ввод пользовательских данных
        chromeDriver.findElement(By.xpath("/html/body/div[5]/div/div/div[2]/form/fieldset/div[1]/div/input")).sendKeys(userConfig.get("login"));
        chromeDriver.findElement(By.xpath("/html/body/div[5]/div/div/div[2]/form/fieldset/div[2]/div/input")).sendKeys(userConfig.get("name"));
        chromeDriver.findElement(By.xpath("/html/body/div[5]/div/div/div[2]/form/fieldset/div[3]/div/input")).sendKeys(userConfig.get("mail"));
        chromeDriver.findElement(By.xpath("/html/body/div[5]/div/div/div[2]/form/fieldset/div[4]/div/input")).sendKeys(userConfig.get("snils"));
        chromeDriver.findElement(By.xpath("/html/body/div[5]/div/div/div[2]/form/fieldset/div[6]/div/input")).sendKeys(userConfig.get("appointment"));
        chromeDriver.findElement(By.xpath("/html/body/div[5]/div/div/div[2]/form/fieldset/div[7]/div/input")).sendKeys(userConfig.get("phone"));
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        chromeDriver.findElement(By.xpath("/html/body/div[5]/div/div/div[2]/form/fieldset/div[9]/div/span[1]/input[2]")).click();
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        chromeDriver.findElement(By.xpath("/html/body/div[5]/div/div/div[2]/form/fieldset/div[9]/div/span[1]/input[2]")).click();
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        while (true) {
            chromeDriver.findElement(By.xpath("/html/body/div[5]/div/div/div[2]/form/fieldset/div[9]/div/span[1]/input[2]")).clear();
            chromeDriver.findElement(By.xpath("/html/body/div[5]/div/div/div[2]/form/fieldset/div[9]/div/span[1]/input[2]")).sendKeys(userConfig.get("operator"));
            if (chromeDriver.findElements(By.xpath("/html/body/div[5]/div/div/div[2]/form/fieldset/div[9]/div/span[1]/input[2]")).size() > 0) {
                if (chromeDriver.findElement(By.xpath("/html/body/div[5]/div/div/div[2]/form/fieldset/div[9]/div/span[1]/input[2]")).getCssValue("display").equals("block")) {
                    break;
                }
            }

        }
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        chromeDriver.findElement(By.xpath("/html/body/div[5]/div/div/div[2]/form/fieldset/div[9]/div/span[1]/input[2]")).sendKeys(Keys.DOWN);
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        chromeDriver.findElement(By.xpath("/html/body/div[5]/div/div/div[2]/form/fieldset/div[9]/div/span[1]/input[2]")).sendKeys(Keys.DOWN);
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        chromeDriver.findElement(By.xpath("/html/body/div[5]/div/div/div[2]/form/fieldset/div[9]/div/span[1]/input[2]")).sendKeys(Keys.ENTER);
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //Отправка пользовательских данных
        chromeDriver.findElement(By.xpath("/html/body/div[5]/div/div/div[3]/button[1]")).click();
        MailClient.checkEmail("mail." + userConfig.get("mail").split("@")[1], userConfig.get("mail").split("@")[0], userConfig.get("mailpassword"));
        chromeDriver.get(MailClient.getUrl());
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"password\"]")));//todo:org.openqa.selenium.TimeoutException: Expected condition failed: waiting for presence of element located by: By.xpath: //*[@id="password"] (tried for 15 second(s) with 100 MILLISECONDS interval)

        chromeDriver.findElement(By.id("password")).sendKeys(userConfig.get("password"));
        chromeDriver.findElement(By.id("repassword")).sendKeys(userConfig.get("password"));
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"bg_create_pass\"]/div[2]/div/div/div/div/form/div[2]/button")));
        chromeDriver.findElement(By.xpath("//*[@id=\"bg_create_pass\"]/div[2]/div/div/div/div/form/div[2]/button")).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"bg_create_pass\"]/div[2]/div/div/div/div/div/a")));
        chromeDriver.findElement(By.xpath("//*[@id=\"bg_create_pass\"]/div[2]/div/div/div/div/div/a")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"email\"]")));
        chromeDriver.findElement(By.id("email")).sendKeys(userConfig.get("mail"));
        chromeDriver.findElement(By.id("password")).sendKeys(userConfig.get("password"));
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"bg_authorization\"]/div[1]/div/div/div[3]/div/form/div[4]/button")));
        chromeDriver.findElement(By.xpath("//*[@id=\"bg_authorization\"]/div[1]/div/div/div[3]/div/form/div[4]/button")).click();
        wait.until(ExpectedConditions.textToBePresentInElement(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/h2")), "Мои формы"));

    }

    @Test(description = "Просмотр списка форм ЛК", dependsOnMethods = "userActivation")
    public void reviewList() {
        //Переход на страницу с формой входа в лк
        chromeDriver.get(config.get("urlLK"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")));
        //Ввод пользовательских данных
        chromeDriver.findElement(By.id("email")).sendKeys(userConfig.get("mail"));
        chromeDriver.findElement(By.id("password")).sendKeys(userConfig.get("password"));
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"bg_authorization\"]/div[1]/div/div/div[3]/div/form/div[4]/button")));
        chromeDriver.findElement(By.xpath("//*[@id=\"bg_authorization\"]/div[1]/div/div/div[3]/div/form/div[4]/button")).click();
        //Загрузка форм 2016г
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/div/div[.]/div[1]/h3")));
        //Проверка заголовока страницы "Мои формы", На вкладке "2016" присутствует текст "Формы за 1 квартал", "Формы за 2 кварта"", "Формы за 3 квартал", "Формы за 4 квартал", "Формы за 2016 год"
        Assert.assertEquals(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/div/div[1]/div[1]/h3")).getText(), "Формы за 1 квартал");
        Assert.assertEquals(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/div/div[2]/div[1]/h3")).getText(), "Формы за 2 квартал");
        Assert.assertEquals(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/div/div[3]/div[1]/h3")).getText(), "Формы за 3 квартал");
        Assert.assertEquals(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/div/div[4]/div[1]/h3")).getText(), "Формы за 4 квартал");
        Assert.assertEquals(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/div/div[5]/div[1]/h3")).getText(), "Формы за 2016 год");
        //Проверка, что общее количество форм - 41
        Assert.assertEquals(chromeDriver.findElements(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/div/div[.]/div[.]/div[2]/div")).size(), 41);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/ul/li[2]")));
        chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/ul/li[2]")).click();
        //Загрузка форм 2017г
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/div/div[.]/div[1]/h3")));
        //Проверка заголовока страницы "Мои формы", На вкладке "2017" присутствует текст "Формы за 1 квартал", "Формы за 2 кварта"", "Формы за 3 квартал", "Формы за 4 квартал", "Формы за 2017 год"
        Assert.assertEquals(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/div/div[1]/div[1]/h3")).getText(), "Формы за 1 квартал");
        Assert.assertEquals(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/div/div[2]/div[1]/h3")).getText(), "Формы за 2 квартал");
        Assert.assertEquals(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/div/div[3]/div[1]/h3")).getText(), "Формы за 3 квартал");
        Assert.assertEquals(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/div/div[4]/div[1]/h3")).getText(), "Формы за 4 квартал");
        Assert.assertEquals(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/div/div[5]/div[1]/h3")).getText(), "Формы за 2017 год");
        //Проверка, что общее количество форм - 41
        Assert.assertEquals(chromeDriver.findElements(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/div/div[.]/div[.]/div[2]/div")).size(), 41);
        //проверка, что 4 блока не подсвечено
        List<WebElement> grey = chromeDriver.findElements(By.xpath("//*[@class=\"bg_colors empty\"]"));
        Assert.assertEquals(grey.size(), 4);
        //проверка, что отчеты за 2-4 квартал и 2017 год не подсвечены
        Assert.assertTrue(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/div/div[2]/div[1]/h3")).findElement(By.xpath(".//../..")).getAttribute("class").equals("bg_colors empty"));
        Assert.assertTrue(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/div/div[3]/div[1]/h3")).findElement(By.xpath(".//../..")).getAttribute("class").equals("bg_colors empty"));
        Assert.assertTrue(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/div/div[4]/div[1]/h3")).findElement(By.xpath(".//../..")).getAttribute("class").equals("bg_colors empty"));
        Assert.assertTrue(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/div/div[5]/div[1]/h3")).findElement(By.xpath(".//../..")).getAttribute("class").equals("bg_colors empty"));
        //Переход на страницу форм 2016
        chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/ul/li[1]")).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/div/div[1]/div[2]/div[2]/div/p/a")));
        chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/div/div[1]/div[2]/div[2]/div/p/a")).click();
        wait.until(ExpectedConditions.textToBePresentInElement(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/div/div/div/div/div/h3")), "Форма ФФСН № 5-связь"));
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//*[@class=\"clearfix global_table\"]")));
        Assert.assertEquals(chromeDriver.findElements(By.xpath("//*[@class=\"clearfix global_table\"]")).size(), 3);
        Assert.assertEquals(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[2]/div/div[1]/div[1]/div/p")).getText(), "Сводный отчет по организации");
        Assert.assertEquals(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[2]/div/div[3]/div[1]/div/p")).getText(), "Москва, Город");
        Assert.assertEquals(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[2]/div/div[4]/div[1]/div/p")).getText(), "Адыгея, Республика");

    }

    @Test(description = "", dependsOnMethods = "reviewList")
    public void sendEmptyForm() {
        chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[2]/div/div[4]/div[2]/div/p/a")).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/div/div/div/div/div/h3")));
        chromeDriver.findElement(By.id("author")).sendKeys("Исполнитель");
        chromeDriver.findElement(By.id("authorEmail")).sendKeys(userConfig.get("mail"));
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[6]/div/div/div[1]/div[1]/button")));
        Date date = new Date();
        chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[6]/div/div/div[1]/div[1]/button")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("ng2-toast > div > div.toast-text")));
        wait.until(ExpectedConditions.numberOfElementsToBe(By.cssSelector("ng2-toast"), 2));
        List<WebElement> popups = chromeDriver.findElements(By.cssSelector("ng2-toast"));
        String sum = popups.get(0).getText() + popups.get(1).getText();
        Assert.assertTrue(sum.contains("Сохранение"));
        Assert.assertTrue(sum.contains("Проверка формы"));
        wait.until(ExpectedConditions.numberOfElementsToBe(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[4]/div[2]/ul/li[.]"), 2));
        Assert.assertEquals(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[4]/div[2]/ul/li[1]/div[1]/div[2]/p")).getText(), "Обновлен черновик");
        Assert.assertEquals(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[4]/div[2]/ul/li[2]/div[1]/div[2]/p")).getText(), "Создан черновик");
        //todo формат дат? КОСТЫЫЫЫЛЬ
        String updateDate = chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/ul[1]/li[3]/div/span")).getText().replace("июн", " ").replace(" ", "");
        SimpleDateFormat siteDate = new SimpleDateFormat("ddyyyyHH:mm");
        String realDate = siteDate.format(date);
        try {
            Assert.assertEquals(updateDate, realDate);
        } catch (AssertionError e) {
            try {
                if (!(Math.abs(siteDate.parse(updateDate).getTime() - date.getTime()) < 60000)) {
                    throw e;
                }
            } catch (ParseException e1) {
                e1.printStackTrace();
            }
        }
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[1]/div/div/div/ul/li[2]/span")));
        chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[1]/div/div/div/ul/li[2]/span")).click();
        wait.until(ExpectedConditions.textToBePresentInElement(chromeDriver.findElement(By.xpath("//*[@id=\"gl_form2\"]/div[2]/table/tbody/tr[2]/td[4]/p")), "Величина показателя"));
        List<WebElement> inputs = chromeDriver.findElements(By.xpath("//*[@id=\"gl_form2\"]/div[2]/table/tbody/tr[.]/td/input"));
        int i = 0;
        for (WebElement webElement : inputs) {
            Assert.assertEquals(webElement.getAttribute("ng-reflect-model"), "0");
        }
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[6]/div/div/div[1]/div[2]/button")));
        chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[6]/div/div/div[1]/div[2]/button")).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"submitting_forms\"]/div/div[2]/div/button[1]")));
        chromeDriver.findElement(By.xpath("//*[@id=\"submitting_forms\"]/div/div[2]/div/button[1]")).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/ul[1]/li[4]/div/span")));
        wait.until(ExpectedConditions.textToBe(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/ul[1]/li[4]/div/span"), "Принят"));
        Assert.assertEquals(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/ul[1]/li[4]/div/span")).getText(), "Принят");

    }

    @Test(dependsOnMethods = "sendEmptyForm")
    public void sendWithWarnings() {
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/div/div/div/div/div/div")));
        chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/div/div/div/div/div/div")).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[2]/div/div[3]/div[2]/div/p/a")));
        chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[2]/div/div[3]/div[2]/div/p/a")).click();
        wait.until(ExpectedConditions.textToBe(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[1]/div/div/div/ul/li[1]/p"), "Шаг 1"));
        wait.until(ExpectedConditions.textToBe(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[1]/div/div/div/ul/li[1]/span"), "Данные об организации"));
        Assert.assertTrue(chromeDriver.findElement(By.xpath("//*[@id=\"titul_form\"]/div[1]/div/div[1]/div/div")).getText().contains("Наименование предприятия (структурного подразделения)"));
        Assert.assertEquals(chromeDriver.findElement(By.xpath("//*[@id=\"titul_form\"]/div[5]/div/h3")).getText(), "Контактные данные");
        Assert.assertEquals(chromeDriver.findElement(By.xpath("//*[@id=\"author\"]")).getCssValue("border-bottom-color"), "rgba(255, 0, 0, 1)");
        Assert.assertEquals(chromeDriver.findElement(By.xpath("//*[@id=\"authorEmail\"]")).getCssValue("border-bottom-color"), "rgba(255, 0, 0, 1)");
        Assert.assertEquals(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/ul[1]/li[4]/div/span")).getText(), "Надо сдать");
        chromeDriver.findElement(By.id("author")).sendKeys("Исполнитель");
        chromeDriver.findElement(By.id("authorEmail")).sendKeys(userConfig.get("mail"));
        chromeDriver.findElement(By.xpath("//*[@id=\"ogrn\"]")).clear();
        chromeDriver.findElement(By.xpath("//*[@id=\"ogrn\"]")).sendKeys("1127028000199");
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[1]/div/div/div/ul/li[2]")));
        chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[1]/div/div/div/ul/li[2]")).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"0101.03\"]")));
        chromeDriver.findElement(By.xpath("//*[@id=\"0101.03\"]")).sendKeys("100");
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[6]/div/div/div[1]/div[1]/button")));
        chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[6]/div/div/div[1]/div[1]/button")).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[5]/h3")));
        Assert.assertTrue(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[5]/h3")).getText().contains("Ошибки при заполнении"));
        Assert.assertTrue(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[5]/div/ul/li/div/div/div/div/div/div[2]/div[2]/div/p")).getText().
                contains("Правило не пройдено: Сумма значений ячеек по графе 03 строк 0103, 0111 должна быть равна значению ячейки по графе 03 по строке 0101"));

//        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[6]/h3")));//todo:org.openqa.selenium.TimeoutException: Expected condition failed: waiting for presence of element located by: By.xpath: //*[@id="wrapper"]/ng-component/ng-component/div[3]/div[6]/h3 (tried for 15 second(s) with 100 MILLISECONDS interval)

        Assert.assertTrue(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[6]/h3")).getText().contains("Предупреждение"));
        Assert.assertTrue(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[6]/div/ul/li/div/div/div/div/div/div[2]/div[2]/div/p")).getText().contains("Ячейка : ОГРН (1127028000199) не совпал со значением в базе данных (1127028000165)."));
        Assert.assertTrue(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[6]/div/ul/li/div/div/div/div/div/div[2]/div[2]/div/p")).getText().contains("Найденный оператор: НИД12ТЕСТ_Г."));
        Assert.assertTrue(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/ul[1]/li[4]/div/span")).getText().contains("Ошибка данных"));
        chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[5]/div/ul/li/div/div/div/div/div/div[3]/div[2]/textarea")).sendKeys("ok");
        chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[6]/div/ul/li/div/div/div/div/div/div[3]/div[2]/textarea")).sendKeys("ok");
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[6]/div/ul/li/div/div/div/div/div/div[3]/div[2]/textarea")));
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[8]/div/div/div[1]/div[2]/button"))).click();//todo org.openqa.selenium.WebDriverException: unknown error: Element <button class="btn_s btn_submit_form" type="button">...</button> is not clickable at point (454, 850). Other element would receive the click: <html class="">...</html>
//        chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[7]/div/div/div[1]/div[2]/button")).click();
        wait.until(ExpectedConditions.textToBe(By.xpath("//*[@id=\"submitting_forms\"]/div/div[2]/div/button[2]/span"), "Подписать ЭП"));
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"submitting_forms\"]/div/div[2]/div/button[2]"))).click();//todo:org.openqa.selenium.WebDriverException: unknown error: Element <button class="ln_start">...</button> is not clickable at point (601, 567). Other element would receive the click: <div class="popups_desc">...</div>
//        wait.until(ExpectedConditions.textToBe(By.xpath("//*[@id=\"submitting_forms_details\"]/div/div[2]/div/div[2]/div/div[2]/div/a"),"Отправка формы"));
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"submitting_forms_details\"]/div/div[2]/div/div[2]/div/div[2]/div"))).click();//todo: Caused by: org.openqa.selenium.NoSuchElementException: Cannot locate an element using By.xpath: //*[@id="submitting_forms_details"]/div/div[2]/div/div[2]/div/div[2]/div
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"submitting_forms_details\"]/div/div[2]/div/div[2]/div/div[2]/div/div/ul/li[2]"))).click();
//        chromeDriver.findElement(By.xpath("//*[@id=\"submitting_forms_details\"]/div/div[2]/div/div[2]/div/div[2]/div")).sendKeys(Keys.ARROW_DOWN);
//        chromeDriver.findElement(By.xpath("//*[@id=\"submitting_forms_details\"]/div/div[2]/div/div[2]/div/div[2]/div")).sendKeys(Keys.ENTER);
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"submitting_forms_details\"]/div/div[3]/button[1]"))).click();
        wait.until(ExpectedConditions.textToBe(By.xpath("//*[@id=\"submitting_forms_details\"]/div/div[2]/div/div[3]/ul/li/span"), "Подпись успешно сформирована"));
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"submitting_forms_details\"]/div/div[3]/button[1]"))).click();
        wait.until(ExpectedConditions.textToBe(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/ul[1]/li[4]/div/span"), "Отправлен"));
    }

    @Test(dependsOnMethods = "sendWithWarnings", description = "Принятие формы в УШ")
    public void acceptInUSH() {
        chromeDriver.get(config.get("urlArmada"));
        chromeDriver.manage().timeouts().implicitlyWait(1000, TimeUnit.MILLISECONDS);
        if ((chromeDriver.findElements(By.xpath("/html/body/div[4]/div/div/div[1]/div[1]/h2")).size() > 0)) {
            if (!(chromeDriver.findElement(By.xpath("/html/body/div[4]/div/div/div[1]/div[1]/h2"))).getText().equals("Календарь")) {
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"username\"]"))).sendKeys(config.get("armadaLog"));
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"password\"]"))).sendKeys(config.get("armadaPass"));
                chromeDriver.manage().timeouts().implicitlyWait(300, TimeUnit.MILLISECONDS);
                chromeDriver.findElement(By.xpath("/html/body/div[3]/div/div/form/fieldset/div[3]/div[2]/button")).click();
            }
        }
        wait.until(ExpectedConditions.textToBe(By.xpath("/html/body/div[4]/div/div/div[1]/div[1]/h2"), "Календарь"));
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"navbar-collapse\"]/ul[1]/li[4]/a"))).click();
        WebElement row = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/div[4]/div/div/div/div[2]/div/div[3]/div/table/tbody[1]/tr[1]")));
        //проверка значений первой формы в таблице
        Assert.assertEquals("05.033", row.findElement(By.xpath("td[1]/div")).getText());
        Assert.assertEquals(userConfig.get("mail"), row.findElement(By.xpath("td[2]/div")).getText());
        Assert.assertEquals("На ручной обработке", row.findElement(By.xpath("td[3]/div")).getText());
        Assert.assertTrue(row.findElement(By.xpath("td[4]/div")).getText().contains("Есть предупреждения, Есть ошибки"));//todo: contains или equals? , Отчёт просрочен
        Assert.assertEquals(new SimpleDateFormat("dd.MM.yyyy").format(new Date()), row.findElement(By.xpath("td[5]/div")).getText());
        Assert.assertEquals(new SimpleDateFormat("dd.MM.yyyy").format(new Date()), row.findElement(By.xpath("td[6]/div")).getText());
        Assert.assertEquals(userConfig.get("operator"), row.findElement(By.xpath("td[7]/div")).getText());
        Assert.assertEquals("2016 год, месяц 3", row.findElement(By.xpath("td[8]/div")).getText());
        Assert.assertTrue(!row.findElement(By.xpath("td[9]/div/input")).isSelected());
        Assert.assertTrue(row.findElement(By.xpath("td[10]/div/input")).isSelected());
        //Переход к первой форме
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("/html/body/div[4]/div/div/div/div[2]/div/div[3]/div/table/tbody[1]/tr[1]/td[1]"))).click();
        ////*[@id="titulCollapse"]/div/form/div/div[5]/div/div[1]/div[1]/button
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("/html/body/div[4]/div/div/div/div[2]/div/div[3]/div/div[1]"))).click();
        //Проверка наличия кнопки
        Assert.assertTrue(chromeDriver.findElements(By.xpath("//*[@id=\"titulCollapse\"]/div/form/div/div[5]/div/div[1]/div[1]/button")).size() > 0);
        Assert.assertTrue(chromeDriver.findElement(By.xpath("//*[@id=\"titulCollapse\"]/div/form/div/div[5]/div/div[1]/div[1]/button")).getAttribute("title").contains("Применить различие?"));
        Assert.assertTrue(chromeDriver.findElement(By.xpath("//*[@id=\"titulCollapse\"]/div/form/div/div[5]/div/div[1]/div[1]/button")).isDisplayed());
        chromeDriver.findElement(By.xpath("/html/body/div[4]/div/div/div/div[2]/div/div[7]/div/form/div/div/textarea")).sendKeys("Комментарий");
        chromeDriver.manage().timeouts().implicitlyWait(300, TimeUnit.MILLISECONDS);
        chromeDriver.findElement(By.xpath("/html/body/div[4]/div/div/div/div[2]/div/div[7]/div/form/div/table/tbody/tr/td[1]/button")).click();
        chromeDriver.manage().timeouts().implicitlyWait(300, TimeUnit.MILLISECONDS);
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"navbar-collapse\"]/ul[1]/li[4]/a"))).click();
        chromeDriver.navigate().refresh();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/div[4]/div/div/div/div[2]/div/div[3]/div/table/tbody[1]/tr[1]/td[3]/div")));
        //Проверка статуса в Армаде
        Assert.assertEquals("Принят", chromeDriver.findElement(By.xpath("/html/body/div[4]/div/div/div/div[2]/div/div[3]/div/table/tbody[1]/tr[1]/td[3]/div")).getText());
        //Переход в ЛК
        chromeDriver.get(config.get("url"));
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"header\"]/div/div/div/div[2]/a[1]"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/div/div[1]/div[2]/div[2]/div/p/a"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[2]/div/div[3]/div[2]/div/p/a"))).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]")));
        Assert.assertEquals("Комментарий", chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div/p[2]")).getText());
        Assert.assertEquals("Принят", chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/ul[1]/li[4]/div/span")).getText());
        String mailContent = MailClient.getContent("mail." + userConfig.get("mail").split("@")[1], userConfig.get("mail").split("@")[0], userConfig.get("mailpassword"));
        Assert.assertTrue(mailContent.contains("Форма принята."));
        Assert.assertTrue(mailContent.contains("Комментарий"));
        System.out.println();
    }

    @Test(dependsOnMethods = "acceptInUSH", description = "Импорт УШ формы из Excel")
    public void importToExcel() throws AWTException {
        //Ожидание и нажатие на кнопку "Редактировать"
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[4]/div[6]/div/div/div/div/button"))).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"editing_shape_form\"]/div/div[2]/p")));
        //Появляется Предупреждение "Ваша форма принята, при подтверждении дальнейшего редактирования предоставленные ранее данные аннулируются, форма будет считаться не сданной.
        //Продолжить?"  с кнопками "Редактировать", "Отмена"
        Assert.assertTrue(chromeDriver.findElement(By.xpath("//*[@id=\"editing_shape_form\"]/div/div[2]/p")).getText().contains("Ваша форма принята, при подтверждении дальнейшего редактирования"));
        Assert.assertTrue(chromeDriver.findElement(By.xpath("//*[@id=\"editing_shape_form\"]/div/div[2]/p")).getText().contains("предоставленные ранее данные аннулируются, форма будет считаться не сданной."));
        wait.until(ExpectedConditions.textToBe(By.xpath("//*[@id=\"editing_shape_form\"]/div/div[2]/div/button[1]/span"), "Редактировать"));
        wait.until(ExpectedConditions.textToBe(By.xpath("//*[@id=\"editing_shape_form\"]/div/div[2]/div/button[2]/span"), "Отмена"));
        //Нажимаем "Редактировать"
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"editing_shape_form\"]/div/div[2]/div/button[1]"))).click();
        chromeDriver.manage().timeouts().implicitlyWait(300, TimeUnit.MILLISECONDS);
        //Проверяем, что статус формы "В работе"
        wait.until(ExpectedConditions.textToBe(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/ul[1]/li[4]/div/span"), "В работе"));
        //Нажимаем "Импортировать из универсального шаблона"
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/ul[2]/li[2]/a"))).click();

        //Появляется окно "Импортировать из универсального шаблона"
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"fancy_import\"]/h3")));
        Assert.assertTrue(chromeDriver.findElement(By.xpath("//*[@id=\"fancy_import\"]/h3")).getText().contains("Импортировать"));
        Assert.assertTrue(chromeDriver.findElement(By.xpath("//*[@id=\"fancy_import\"]/h3")).getText().contains("из универсального шаблона"));
        //Нажимаем "Импортировать"
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"drop\"]/button"))).click(); //todo: провдник открываться не будет!
//        chromeDriver.
//        MainUtil.closeWindow();
//        chromeDriver.manage().timeouts().implicitlyWait(1,TimeUnit.SECONDS);
        //Выбираем файл "F1-05.033 1.9.0 - Москва.xls"
//        MainUtil.typeAndEnter(new File("data/forms/F1-05.033 1.9.0M.xls").getAbsolutePath());

        chromeDriver.findElement(By.xpath("//*[@id=\"drop\"]/input")).sendKeys(new File("data/forms/F1-05.033 1.9.0M.xls").getAbsolutePath());
        chromeDriver.manage().timeouts().implicitlyWait(1,TimeUnit.SECONDS);


        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"iff\"]"))).click();
        //Парсинг значений показателей на странице
        List<WebElement> rows = chromeDriver.findElements(By.xpath("//*[@id=\"gl_form2\"]/div[2]/table/tbody/tr[.]"));
        HashMap<String,String> siteMap= new HashMap<>();
        for (WebElement webElement : rows) {
            WebElement mainE = webElement.findElement(By.xpath("td[5]/input"));
            if(mainE==null){
                continue;
            }
            siteMap.put(webElement.findElement(By.xpath("td[1]/p")).getText()+"."+webElement.findElement(By.xpath("td[2]/p")).getText(),
                    webElement.findElement(By.xpath("td[5]/input")).getAttribute("ng-reflect-model"));
        }
        MainUtil.compareMaps(siteMap,ExcelParser.getValues("data/forms/F1-05.033 1.9.0M.xls"));

        //


    }

    @AfterClass
    public void closeDriver() {
        if (chromeDriver != null) {
            chromeDriver.close();
            chromeDriver.quit();
        }
    }

}
