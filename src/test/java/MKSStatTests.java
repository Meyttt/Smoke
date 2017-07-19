import ExcelFun.ExcelParser;
import mail.MailClient;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.ComparisonFailure;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import sql.SqlManager;
import utils.Config;
import utils.MainUtil;

import javax.mail.MessagingException;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * Created by a.chebotareva on 11.05.2017.
 */
public class MKSStatTests {

    ChromeDriver chromeDriver;
    Config config = new Config("config.properties");
    Config userConfig = new Config("userInfo.properties");
    Config databaseConfig = new Config("database.properties");
    WebDriverWait wait;
    File file = new File("data/docs");
    Date lastDate;
    Logger logger;
    static HashMap<String,String> months=new HashMap<>();

    public MKSStatTests() throws IOException {
    }


    @BeforeClass(groups = {"smoke", "regress"})
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
        wait = new WebDriverWait(chromeDriver, 40);
        wait.pollingEvery(100, TimeUnit.MILLISECONDS);
        logger = Logger.getLogger(MKSStatTests.class);
        logger.info("Тестирование от " + new Date());
    }
    @BeforeClass
    void initMonths(){
        months.put("янв","01");
        months.put("фев","02");
        months.put("мар","03");
        months.put("апр","04");
        months.put("май","05");
        months.put("июн","06");
        months.put("июл","07");
        months.put("авг","08");
        months.put("сен","09");
        months.put("окт","10");
        months.put("ноя","11");
        months.put("дек","12");
    }

    @BeforeClass(groups = {"smoke", "regress"})
    @AfterClass(groups = {"smoke", "regress"})
    void cleanDirectory() {
        if (!file.exists()) {
            file.mkdir();
            return;
        }
        File[] files = file.listFiles();
        if (files.length == 0) {
            return;
        }
        for (int i = 0; i < files.length; i++) {
            files[i].delete();
        }
    }

    @Test(groups = {"smoke"}, description = "Просмотр страниц и ссылок в открытой части портала")
    void PageAndLinks() {
        logger.info("Открываем главную страницу " + config.get("url"));
        chromeDriver.get(config.get("url"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"wrapper\"]/ng-component/div[1]/div[1]/div[2]/div/div/div/div/div/div/p")));
        logger.info("Наличие на странице строки \"Данный ресурс предназначен для операторов связи\"");
        Assert.assertTrue(Pattern.matches("Данный ресурс предназначен[\\w\\s]+для операторов связи", chromeDriver.findElementByXPath("//*[@id=\"wrapper\"]/ng-component/div[1]/div[1]/div[2]/div/div/div/div/div/div/p").getText()));
        logger.info("Открывается ссылка 46 ФЗ “О связи”, открывается всплывающее окно с заголовком \"Обязанности операторов связи\"");
        chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/div[2]/div/div[3]/div/div/p/button[1]")).click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"on_communication\"]/div/div[1]/h3")));
        Assert.assertTrue(Pattern.matches("Обязанности операторов[\\w\\s]+связи", chromeDriver.findElementByXPath("//*[@id=\"on_communication\"]/div/div[1]/h3").getText()));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a[title='Close']")));
        chromeDriver.findElement(By.cssSelector("a[title='Close']")).click();
        chromeDriver.manage().timeouts().implicitlyWait(100, TimeUnit.MILLISECONDS);
        logger.info("Открывается ссылка 13.19 КоАП РФ, открывается всплывающее окно с заголовком \"Ответственность за непредоставление сведений\"");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"wrapper\"]/div[2]/div/div[3]/div/div/p/button[2]")));
        chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/div[2]/div/div[3]/div/div/p/button[2]")).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"provide_info\"]/div/div[1]/h3")));
        Assert.assertTrue(Pattern.matches("Ответственность за[\\w\\s]+непредоставление сведений", chromeDriver.findElementByXPath("//*[@id=\"provide_info\"]/div/div[1]/h3").getText()));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a[title='Close']")));
        chromeDriver.findElement(By.cssSelector("a[title='Close']")).click();
        chromeDriver.manage().timeouts().implicitlyWait(100, TimeUnit.MILLISECONDS);
        logger.info("Переходим на страницу \"Статистика\"");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#header > div > div > div > div.blc_main_menu > div.navbar-collapse.collapse > ul > li:nth-child(2) > a")));
        chromeDriver.findElement(By.cssSelector("#header > div > div > div > div.blc_main_menu > div.navbar-collapse.collapse > ul > li:nth-child(2) > a")).click();
        logger.info("Скачивается файл abonentyi-shpd-0616.xlsx по ссылке Абоненты ШПД (II квартал 2016 года)");
        WebElement webElement = chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/div[2]/div[4]/ul[2]/li[1]/div/a"));
        Assert.assertEquals( "Абоненты ШПД (I квартал 2016 года)",webElement.findElement(By.tagName("h4")).getText());
        webElement.click();
        logger.info("Переходим на страницу \"События и документы\"");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#header > div > div > div > div.blc_main_menu > div.navbar-collapse.collapse > ul > li:nth-child(3) > a")));
        chromeDriver.findElement(By.cssSelector("#header > div > div > div > div.blc_main_menu > div.navbar-collapse.collapse > ul > li:nth-child(3) > a")).click();
        logger.info("На странице присутствуют строки \"События\", \"Письмо Минкомсвязи России о порядке предоставления статистической отчетности\"");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.partialLinkText("События")));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"wrapper\"]/ng-component/div[2]/div[2]/ul/li[1]/div/div[2]/p")));
        Assert.assertTrue(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/div[2]/div[2]/ul/li[1]/div/div[2]/p")).getText().contains("Минкомсвязь России уведомляет о недопустимости использования операторами связи посреднических услуг."));
        logger.info("Переходим на вкладку \"Документы\"");
        chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/div[2]/div[1]/div/div/div/ul/li[2]")).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"wrapper\"]/ng-component/div[2]/div[3]/ul/li[1]/div/a")));
        logger.info("На странице присутствуют строки \"Документы\", \"Письмо Минкомсвязи России о порядке предоставления статистической отчетности\"");
        Assert.assertEquals("Документы",chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/div[2]/div[1]/div/div/div/ul/li[2]/span")).getText());
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"wrapper\"]/ng-component/div[2]/div[1]/div/div/div/ul/li[2]")));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"wrapper\"]/ng-component/div[2]/div[3]/ul/li[1]/div/a/h4")));
        Assert.assertTrue(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/div[2]/div[3]/ul/li[1]/div/a/h4")).getText().contains("Письмо Минкомсвязи России от 29.12.2016 №НН-П14-062-28741 «О порядке"));
        Assert.assertTrue(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/div[2]/div[3]/ul/li[1]/div/a/h4")).getText().contains("представления статистической отчетности»"));
        logger.info("Скачивается файл по ссылке Письмо Минкомсвязи России от 29.12.2016 №НН-П14-062-28741 «О порядке представления статистической отчетности»");
        chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/div[2]/div[3]/ul/li[1]/div/a")).click();
        logger.info("Переходим на страницу \"Вопросы и ответы\"");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#header > div > div > div > div.blc_main_menu > div.navbar-collapse.collapse > ul > li:nth-child(4) > a")));
        chromeDriver.findElement(By.cssSelector("#header > div > div > div > div.blc_main_menu > div.navbar-collapse.collapse > ul > li:nth-child(4) > a")).click();
        logger.info("На странице присутствуют строки \"Часто задаваемые вопросы\", \"Где можно ознакомиться с порядком предоставления статистической отчетности в сфере связи?\"");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("blc_frequently_questions_ttl")));
        Assert.assertTrue(chromeDriver.findElement(By.className("blc_frequently_questions_ttl")).findElement(By.tagName("h3")).getText().equals("Часто задаваемые вопросы"));
        Assert.assertTrue(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ul/li[2]/div[1]/h4")).getText().contains("Где можно ознакомиться с порядком предоставления статистической отчетности в сфере связи?"));
        logger.info("Переходим на страницу \"Контакты\"");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#header > div > div > div > div.blc_main_menu > div.navbar-collapse.collapse > ul > li:nth-child(5) > a")));
        chromeDriver.findElement(By.cssSelector("#header > div > div > div > div.blc_main_menu > div.navbar-collapse.collapse > ul > li:nth-child(5) > a")).click();
        logger.info("На странице присутствуют строки \"Адрес 125375, г. Москва, ул. Тверская, д. 7\"");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"wrapper\"]/ng-component/div[2]/div/div/ul/li[1]/div/p")));
        Assert.assertTrue(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/div[2]/div/div/ul/li[1]/div/p")).getText().contains("Адрес"));
        Assert.assertTrue(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/div[2]/div/div/ul/li[1]/div/span[1]")).getText().contains("125375, г. Москва,"));
        Assert.assertTrue(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/div[2]/div/div/ul/li[1]/div/span[2]")).getText().contains("ул. Тверская, д. 7"));
        try {
            Thread.sleep(20000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.info("Проверяем, что скачано 2 файла");
        Assert.assertEquals(file.listFiles().length, 2);
    }

    @Test(groups = "smoke", description = "Просмотр форм для сдачи при вводе лицензии")
    void numberOfForms() throws InterruptedException {
        logger.info("Открываем главную страницу "+config.get("url"));
        chromeDriver.get(config.get("url"));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("licNumber")));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("selectService")));
        logger.info("В поле \"Номер лицензии\" вводим \""+config.get("licenseNumber")+"\"");
        chromeDriver.findElement(By.xpath("//*[@id=\"licNumber\"]")).sendKeys(config.get("licenseNumber"));
        while (chromeDriver.findElements(By.xpath("//*[@id=\"selectService_chosen\"]/div/ul/li[.]")).size() == 0) {
            clickButton("//*[@id=\"selectService_chosen\"]/a");
            try {
                sleep(500);
                while (!chromeDriver.findElement(By.xpath("//*[@id=\"selectService_chosen\"]/div/ul/li[.]")).isDisplayed()) {
                    clickButtonJS("//*[@id=\"selectService_chosen\"]/a/div/b");
                    chromeDriver.manage().timeouts().implicitlyWait(100, TimeUnit.MILLISECONDS);
                }
            } catch (StaleElementReferenceException e) {
                e.printStackTrace();
            }
        }

        logger.info("В поле \"Наименование услуги связи выбираем \""+config.get("serviceName")+"\"");
        circle:
        while (true) {
            try {
                List<WebElement> services = chromeDriver.findElements(By.xpath("//*[@id=\"selectService_chosen\"]/div/ul/li[.]"));
                for (WebElement webElement : services) {
                    if (webElement.getText().equals(config.get("serviceName"))) {
                        clicking:
                        while (true) {
                            try {
                                clickButton(webElement);
                                wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"wrapper\"]/ng-component/home-form/div[1]/div/div/div/div/button[1]")));
                                wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"wrapper\"]/ng-component/home-form/div[1]/div/div/div/div/button[1]")));
                                break circle;
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
        logger.info("Нажимаем \"Узнать\"");
        clickButtonJS("//*[@id=\"wrapper\"]/ng-component/home-form/div[1]/div/div/div/div/button[1]");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"wrapper\"]/ng-component/home-form/div[2]/div[1]/h3")));
        logger.info("Проверяем наличие всех отчетов");
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//*[@id=\"wrapper\"]/ng-component/home-form/div[2]/div[.]/div[2]/span")));
        Assert.assertEquals(11, chromeDriver.findElements(By.xpath("//*[@id=\"wrapper\"]/ng-component/home-form/div[2]/div[.]/div[2]/span")).size());
        logger.info("проверяем по названиям и именно в том порядке, который сейчас есть");
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//*[@id=\"wrapper\"]/ng-component/home-form/div[2]/div[.]/div[2]/span")));
        sleep(500);
        Assert.assertEquals("«СВЕДЕНИЯ О РАЗВИТИИ ТЕЛЕМАТИЧЕСКИХ УСЛУГ И УСЛУГ СЕТИ ПЕРЕДАЧИ ДАННЫХ»",chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/home-form/div[2]/div[2]/div[2]/span")).getText());
        Assert.assertEquals("«СВЕДЕНИЯ ОБ ОБМЕНЕ (ТРАФИКЕ) НА СЕТЯХ ЭЛЕКТРОСВЯЗИ»",chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/home-form/div[2]/div[3]/div[2]/span")).getText());
        Assert.assertEquals("«СВЕДЕНИЯ О ДОХОДАХ ОТ УСЛУГ СВЯЗИ»",chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/home-form/div[2]/div[4]/div[2]/span")).getText());
        Assert.assertEquals("«Численность и начисленная заработная плата работников»",chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/home-form/div[2]/div[5]/div[2]/span")).getText());
        Assert.assertEquals("«СВЕДЕНИЯ ОБ ОРГАНИЗАЦИЯХ СВЯЗИ»",chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/home-form/div[2]/div[7]/div[2]/span")).getText());
        Assert.assertEquals("«СВЕДЕНИЯ О ТЕХНИЧЕСКИХ СРЕДСТВАХ СПУТНИКОВОЙ СВЯЗИ И ВЕЩАНИЯ»",chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/home-form/div[2]/div[8]/div[2]/span")).getText());
        Assert.assertEquals("«Сведения о численности и заработной плате работников»",chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/home-form/div[2]/div[9]/div[2]/span")).getText());
        Assert.assertEquals("«Сведения о кадровом составе»",chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/home-form/div[2]/div[10]/div[2]/span")).getText());
        Assert.assertEquals("«СВЕДЕНИЯ О ТРАВМАТИЗМЕ НА ПРОИЗВОДСТВЕ И ПРОФЕССИОНАЛЬНЫХ ЗАБОЛЕВАНИЯХ»",chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/home-form/div[2]/div[11]/div[2]/span")).getText());
        Assert.assertEquals("«Сведения об инвестиционной деятельности»",chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/home-form/div[2]/div[12]/div[2]/span")).getText());
        Assert.assertEquals("«Макет консолидированных форм»",chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/home-form/div[2]/div[13]/div[2]/span")).getText());
    }

    @Test(groups = "smoke", description = "")
    public void userActivation() {
        logger.info("Авторизуемся в АРМАДу ");
        chromeDriver.get(
                config.get("urlArmada"));
        chromeDriver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
        if (chromeDriver.findElements(By.xpath("/html/body/div[3]/div/div/form/fieldset/div[3]/div[2]/button")).size() > 0) {
            try {
                chromeDriver.findElement(By.xpath("//*[@id=\"username\"]")).sendKeys(config.get("armadaLog"));
                chromeDriver.findElement(By.xpath("//*[@id=\"password\"]")).sendKeys(config.get("armadaPass"));
                chromeDriver.findElement(By.xpath("/html/body/div[3]/div/div/form/fieldset/div[3]/div[2]/button")).click();
            } catch (WebDriverException e) {
            }
        }
        logger.info("Переходим в Справочники->Пользователи");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("body > div.container-fa.container-fluid > div > div > div:nth-child(1) > div.col-lg-7 > h2")));
        chromeDriver.findElement(By.xpath("//*[@id=\"navbar-collapse\"]/ul[1]/li[2]/a")).click();
        List<WebElement> dictionaries = chromeDriver.findElements(By.xpath("//*[@id=\"navbar-collapse\"]/ul[1]/li[2]/ul/li[.]/a"));
        for (WebElement webElement : dictionaries) {
            if (webElement.getText().equals("Пользователи")) {
                webElement.click();
//
                wait.until(ExpectedConditions.elementToBeClickable(By.xpath("/html/body/div[4]/div/div/div[2]/div/div[1]/div/div[1]/div/div[1]/button[1]")));
                break;
            }
        }
        chromeDriver.manage().window().maximize();
        clickButton("/html/body/div[4]/div/div/div[2]/div/div[1]/div/div[1]/div/div[1]/button[1]");
        logger.info("Удаляем данные о пользователях с тем же email");
        clearHistory();
        logger.info("Переходим к созданию пользователя");
        wait.until(ExpectedConditions.textToBePresentInElement(chromeDriver.findElement(By.xpath("/html/body/div[5]/div/div/div[1]/h4")), "Создание пользователя"));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/div[5]/div/div/div[2]/form/fieldset/div[1]/div/input")));
        logger.info("В форме создания пользователя регистрируем нового пользователя (указываем реальный почтовый адрес)");
        chromeDriver.findElement(By.xpath("/html/body/div[5]/div/div/div[2]/form/fieldset/div[1]/div/input")).sendKeys(userConfig.get("login"));
        chromeDriver.findElement(By.xpath("/html/body/div[5]/div/div/div[2]/form/fieldset/div[2]/div/input")).sendKeys(userConfig.get("name"));
        chromeDriver.findElement(By.xpath("/html/body/div[5]/div/div/div[2]/form/fieldset/div[3]/div/input")).sendKeys(userConfig.get("mail"));
        chromeDriver.findElement(By.xpath("/html/body/div[5]/div/div/div[2]/form/fieldset/div[4]/div/input")).sendKeys(userConfig.get("snils"));
        chromeDriver.findElement(By.xpath("/html/body/div[5]/div/div/div[2]/form/fieldset/div[6]/div/input")).sendKeys(userConfig.get("appointment"));
        chromeDriver.findElement(By.xpath("/html/body/div[5]/div/div/div[2]/form/fieldset/div[7]/div/input")).sendKeys(userConfig.get("phone"));
        sleep(300);
        logger.info("Привязываем к оператору "+userConfig.get("operator"));
        chromeDriver.findElement(By.xpath("/html/body/div[5]/div/div/div[2]/form/fieldset/div[9]/div/span[1]/input[2]")).click();
        sleep(300);
        chromeDriver.findElement(By.xpath("/html/body/div[5]/div/div/div[2]/form/fieldset/div[9]/div/span[1]/input[2]")).click();
        sleep(300);
        while (true) {
            chromeDriver.findElement(By.xpath("/html/body/div[5]/div/div/div[2]/form/fieldset/div[9]/div/span[1]/input[2]")).clear();
            sendKeysToInput("/html/body/div[5]/div/div/div[2]/form/fieldset/div[9]/div/span[1]/input[2]", userConfig.get("operator"));
            if (chromeDriver.findElements(By.xpath("/html/body/div[5]/div/div/div[2]/form/fieldset/div[9]/div/span[1]/input[2]")).size() > 0) {
                if (chromeDriver.findElement(By.xpath("/html/body/div[5]/div/div/div[2]/form/fieldset/div[9]/div/span[1]/input[2]")).getCssValue("display").equals("block")) {
                    break;
                }
            }

        }
        sleep();
        chromeDriver.findElement(By.xpath("/html/body/div[5]/div/div/div[2]/form/fieldset/div[9]/div/span[1]/input[2]")).sendKeys(Keys.DOWN);
        sleep(300);
        chromeDriver.findElement(By.xpath("/html/body/div[5]/div/div/div[2]/form/fieldset/div[9]/div/span[1]/input[2]")).sendKeys(Keys.DOWN);
        sleep(300);
        chromeDriver.findElement(By.xpath("/html/body/div[5]/div/div/div[2]/form/fieldset/div[9]/div/span[1]/input[2]")).sendKeys(Keys.ENTER);
        sleep(300);
        chromeDriver.findElement(By.xpath("/html/body/div[5]/div/div/div[3]/button[1]")).click();
        logger.info("Проверяем письмо на почте");
        MailClient.checkEmail("mail." + userConfig.get("mail").split("@")[1], userConfig.get("mail").split("@")[0], userConfig.get("mailpassword"));
        try {
            chromeDriver.get(MailClient.getUrl());
        } catch (NullPointerException e) {
            sleep(10000);
            MailClient.checkEmail("mail." + userConfig.get("mail").split("@")[1], userConfig.get("mail").split("@")[0], userConfig.get("mailpassword"));
            chromeDriver.get(MailClient.getUrl());
        }
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"password\"]")));
        logger.info("Переходим по ссылке из письма");
        chromeDriver.findElement(By.id("password")).sendKeys(userConfig.get("password"));
        chromeDriver.findElement(By.id("repassword")).sendKeys(userConfig.get("password"));
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"bg_create_pass\"]/div[2]/div/div/div/div/form/div[2]/button")));
        chromeDriver.findElement(By.xpath("//*[@id=\"bg_create_pass\"]/div[2]/div/div/div/div/form/div[2]/button")).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"bg_create_pass\"]/div[2]/div/div/div/div/div/a")));
        chromeDriver.findElement(By.xpath("//*[@id=\"bg_create_pass\"]/div[2]/div/div/div/div/div/a")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"email\"]")));
        logger.info("Авторизуемся под созданным пользователем.");
        chromeDriver.findElement(By.id("email")).sendKeys(userConfig.get("mail"));
        chromeDriver.findElement(By.id("password")).sendKeys(userConfig.get("password"));
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"bg_authorization\"]/div[1]/div/div/div[3]/div/form/div[4]/button")));
        chromeDriver.findElement(By.xpath("//*[@id=\"bg_authorization\"]/div[1]/div/div/div[3]/div/form/div[4]/button")).click();
        wait.until(ExpectedConditions.textToBePresentInElement(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/h2")), "Мои формы"));
    }

    @Test(groups = "smoke", description = "Просмотр списка форм ЛК", dependsOnMethods = "userActivation")
    public void reviewList() {
        logger.info("Авторизуемся в ЛК под созданным пользователем");
        chromeDriver.get(config.get("urlLK"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")));
        chromeDriver.findElement(By.id("email")).sendKeys(userConfig.get("mail"));
        chromeDriver.findElement(By.id("password")).sendKeys(userConfig.get("password"));
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"bg_authorization\"]/div[1]/div/div/div[3]/div/form/div[4]/button")));
        chromeDriver.findElement(By.xpath("//*[@id=\"bg_authorization\"]/div[1]/div/div/div[3]/div/form/div[4]/button")).click();
        logger.info("Проверка заголовока страницы \"Мои формы\", На вкладке \"2016\" присутствует текст \"Формы за 1 квартал\", \"Формы за 2 кварта\"\", \"Формы за 3 квартал\", \"Формы за 4 квартал\", \"Формы за 2016 год\"");
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/div/div[.]/div[1]/h3")));
        Assert.assertEquals("Мои формы",chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/h2")).getText());
        Assert.assertEquals("Формы за 1 квартал",chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/div/div[1]/div[1]/h3")).getText());
        Assert.assertEquals("Формы за 2 квартал",chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/div/div[2]/div[1]/h3")).getText());
        Assert.assertEquals("Формы за 3 квартал",chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/div/div[3]/div[1]/h3")).getText());
        Assert.assertEquals("Формы за 4 квартал",chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/div/div[4]/div[1]/h3")).getText());
        Assert.assertEquals("Формы за 2016 год",chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/div/div[5]/div[1]/h3")).getText());
        logger.info("Проверка, что общее количество форм - 41");
        Assert.assertEquals(41,chromeDriver.findElements(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/div/div[.]/div[.]/div[2]/div")).size());
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/ul/li[2]")));
        chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/ul/li[2]")).click();
        logger.info("Загрузка форм 2017г");
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/div/div[.]/div[1]/h3")));
        logger.info("Проверка заголовока страницы \"Мои формы\", На вкладке \"2017\" присутствует текст \"Формы за 1 квартал\", \"Формы за 2 кварта\"\", \"Формы за 3 квартал\", \"Формы за 4 квартал\", \"Формы за 2017 год\"");
        Assert.assertEquals("Мои формы",chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/h2")).getText());
        Assert.assertEquals("Формы за 1 квартал",chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/div/div[1]/div[1]/h3")).getText() );
        Assert.assertEquals("Формы за 2 квартал",chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/div/div[2]/div[1]/h3")).getText() );
        Assert.assertEquals("Формы за 3 квартал",chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/div/div[3]/div[1]/h3")).getText());
        Assert.assertEquals("Формы за 4 квартал",chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/div/div[4]/div[1]/h3")).getText() );
        Assert.assertEquals("Формы за 2017 год",chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/div/div[5]/div[1]/h3")).getText());
        logger.info("Проверка, что общее количество форм - 41");
        Assert.assertEquals(chromeDriver.findElements(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/div/div[.]/div[.]/div[2]/div")).size(), 41);
        logger.info("Проверка, что 3 блока не подсвечено");
        List<WebElement> grey = chromeDriver.findElements(By.xpath("//*[@class=\"bg_colors empty\"]"));
        Assert.assertEquals(3,grey.size());
        logger.info("проверка, что отчеты за 3-4 квартал и 2017 год не подсвечены");
        Assert.assertTrue(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/div/div[3]/div[1]/h3")).findElement(By.xpath(".//../..")).getAttribute("class").equals("bg_colors empty"));
        Assert.assertTrue(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/div/div[4]/div[1]/h3")).findElement(By.xpath(".//../..")).getAttribute("class").equals("bg_colors empty"));
        Assert.assertTrue(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/div/div[5]/div[1]/h3")).findElement(By.xpath(".//../..")).getAttribute("class").equals("bg_colors empty"));
        logger.info("Переход на страницу форм 2016");
        chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/ul/li[1]")).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/div/div[1]/div[2]/div[2]/div/p/a")));
        chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/div/div[1]/div[2]/div[2]/div/p/a")).click();
        wait.until(ExpectedConditions.textToBePresentInElement(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/div/div/div/div/div/h3")), "Форма ФФСН № 5-связь"));
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//*[@class=\"clearfix global_table\"]")));
        logger.info("В  списке 3 отчета");
        Assert.assertEquals(chromeDriver.findElements(By.xpath("//*[@class=\"clearfix global_table\"]")).size(), 3);
        logger.info("Проверк первых колонок списка");
        Assert.assertEquals("Сводный отчет по организации",chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[2]/div/div[1]/div[1]/div/p")).getText());
        Assert.assertEquals("Москва, Город",chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[2]/div/div[3]/div[1]/div/p")).getText());
        Assert.assertEquals("Адыгея, Республика",chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[2]/div/div[4]/div[1]/div/p")).getText() );

    }

    @Test(groups = "smoke", description = "", dependsOnMethods = "reviewList")
    public void sendEmptyForm() {
        logger.info("Проваливаемся в самый нижний отчет ФФСН № 5-связь");
        chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[2]/div/div[4]/div[2]/div/p/a")).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/div/div/div/div/div/h3")));
        logger.info("На странице есть текст \"Шаг 1\", \"Данные об организации\",  \"Наименование предприятия (структурного подразделения)\", \"Контактные данные\", поля \"Исполнитель\", \"Email\" подсвечены красным, статус \"Надо сдать\"");
        Assert.assertEquals("Шаг 1",chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[1]/div/div/div/ul/li[1]/p")).getText());
        Assert.assertEquals("Данные об организации",chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[1]/div/div/div/ul/li[1]/span")).getText());
        Assert.assertEquals("Наименование предприятия (структурного подразделения)",chromeDriver.findElement(By.xpath("//*[@id=\"titul_form\"]/div[1]/div/div[1]/div/div")).getText());
        Assert.assertEquals("Контактные данные",chromeDriver.findElement(By.xpath("//*[@id=\"titul_form\"]/div[5]/div/h3")).getText());
        Assert.assertEquals("Надо сдать",chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/ul[1]/li[4]/div/span")).getText());
        Assert.assertEquals(chromeDriver.findElement(By.xpath("//*[@id=\"author\"]")).getCssValue("border-bottom-color"), "rgba(255, 0, 0, 1)");
        Assert.assertEquals(chromeDriver.findElement(By.xpath("//*[@id=\"authorEmail\"]")).getCssValue("border-bottom-color"), "rgba(255, 0, 0, 1)");
        logger.info("Заполняем поле \"Испольнитель\", \"Email\" корректными значенями");
        chromeDriver.findElement(By.id("author")).sendKeys("Исполнитель");
        chromeDriver.findElement(By.id("authorEmail")).sendKeys(userConfig.get("mail"));
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[6]/div/div/div[1]/div[1]/button")));
        lastDate = new Date();
        logger.info("Нажимаем \"Проверить\"");
        clickButtonJS("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[6]/div/div/div[1]/div[1]/button");
        logger.info("В правом верхнем углу появляется 2 статуса \"Форма проверена\", \"Форма сохранена\" (они потом исчезают)");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("ng2-toast > div > div.toast-text")));
        wait.until(ExpectedConditions.numberOfElementsToBe(By.cssSelector("ng2-toast"), 2));
        List<WebElement> popups = chromeDriver.findElements(By.cssSelector("ng2-toast"));
        String sum = popups.get(0).getText() + popups.get(1).getText();
        Assert.assertTrue(sum.contains("Сохранение"));
        Assert.assertTrue(sum.contains("Проверка формы"));
        Assert.assertTrue(sum.contains("проверена"));
        wait.until(ExpectedConditions.numberOfElementsToBe(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[4]/div[2]/ul/li[.]"), 2));
        logger.info("В Истории 2 записи \"Сохранен черновик\", \"Обновлен черновик\"");
        Assert.assertEquals(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[4]/div[2]/ul/li[1]/div[1]/div[2]/p")).getText(), "Обновлен черновик");
        Assert.assertEquals(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[4]/div[2]/ul/li[2]/div[1]/div[2]/p")).getText(), "Создан черновик");
        logger.info("Проверяем статус формы - \"В работе\"");
        Assert.assertEquals("В работе", chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/ul[1]/li[4]/div/span")).getText());
        logger.info("Проверяем, что дата \"Последняя операция\" обновлена");
        String updateDate = null;
        try {
            updateDate = replaceMonth(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/ul[1]/li[3]/div/span")).getText()).replace(" ", "");
        } catch (Exception e) {
            e.printStackTrace();
        }
        SimpleDateFormat siteDate = new SimpleDateFormat("ddMMyyyyHH:mm");
        try {
            Assert.assertTrue("Текущая дата и дата обновления формы на сайте не совпадают", checkDates(siteDate.parse(updateDate)));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        logger.info("Переходим во вкладку \"Отчетные данные\"");
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[1]/div/div/div/ul/li[2]/span")));
        chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[1]/div/div/div/ul/li[2]/span")).click();
        logger.info("На этой странице проверяем количество строк для заполнения");
        Assert.assertEquals(78, chromeDriver.findElements(By.xpath("//*[@id=\"gl_form2\"]/div[2]/table/tbody/tr[.]/td[5]/input")).size());
        logger.info("Проверяем, что в ячейках значения \"0\"");
        wait.until(ExpectedConditions.textToBePresentInElement(chromeDriver.findElement(By.xpath("//*[@id=\"gl_form2\"]/div[2]/table/tbody/tr[2]/td[4]/p")), "Величина показателя"));
        List<WebElement> inputs = chromeDriver.findElements(By.xpath("//*[@id=\"gl_form2\"]/div[2]/table/tbody/tr[.]/td/input"));
        int i = 0;
        for (WebElement webElement : inputs) {
            Assert.assertEquals(webElement.getAttribute("value"), "0");
        }
        logger.info("Нажимаем \"Отправить форму\"");
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[6]/div/div/div[1]/div[2]/button")));
        chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[6]/div/div/div[1]/div[2]/button")).click();
        logger.info("В появившемся уведомлении нажимаем \"Отправить без ЭП\"");
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"submitting_forms\"]/div/div[2]/div/button[1]")));
        chromeDriver.findElement(By.xpath("//*[@id=\"submitting_forms\"]/div/div[2]/div/button[1]")).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/ul[1]/li[4]/div/span")));
        logger.info("Проверяем статус формы \"Принят\"");
        wait.until(ExpectedConditions.textToBe(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/ul[1]/li[4]/div/span"), "Принят"));
        Assert.assertEquals(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/ul[1]/li[4]/div/span")).getText(), "Принят");

    }

    @Test(groups = "smoke", dependsOnMethods = "sendEmptyForm")
    public void sendWithWarnings() {
        logger.info("Переходим в форму головной компании (Москва)");
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/div/div/div/div/div/div")));
        chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/div/div/div/div/div/div")).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[2]/div/div[3]/div[2]/div/p/a")));
        chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[2]/div/div[3]/div[2]/div/p/a")).click();
        logger.info("Проверка, что на странице есть текст \"Шаг 1\", \"Данные об организации\",  \"Наименование предприятия (структурного подразделения)\", \"Контактные данные\"");
        wait.until(ExpectedConditions.textToBe(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[1]/div/div/div/ul/li[1]/p"), "Шаг 1"));
        wait.until(ExpectedConditions.textToBe(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[1]/div/div/div/ul/li[1]/span"), "Данные об организации"));
        Assert.assertTrue(chromeDriver.findElement(By.xpath("//*[@id=\"titul_form\"]/div[1]/div/div[1]/div/div")).getText().contains("Наименование предприятия (структурного подразделения)"));
        Assert.assertEquals(chromeDriver.findElement(By.xpath("//*[@id=\"titul_form\"]/div[5]/div/h3")).getText(), "Контактные данные");
        logger.info("Поля \"Исполнитель\", \"Email\" подсвечены красным, статус \"Надо сдать\"");
        Assert.assertEquals("rgba(255, 0, 0, 1)",chromeDriver.findElement(By.xpath("//*[@id=\"author\"]")).getCssValue("border-bottom-color"));
        Assert.assertEquals( "rgba(255, 0, 0, 1)",chromeDriver.findElement(By.xpath("//*[@id=\"authorEmail\"]")).getCssValue("border-bottom-color"));
        Assert.assertEquals("Надо сдать",chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/ul[1]/li[4]/div/span")).getText() );
        logger.info("Заполняем обязательные поля");
        chromeDriver.findElement(By.id("author")).sendKeys("Исполнитель");
        chromeDriver.findElement(By.id("authorEmail")).sendKeys(userConfig.get("mail"));
        logger.info("На вкладке \"Данные об обрганизации\" изменяем ОГРН");
        chromeDriver.findElement(By.xpath("//*[@id=\"ogrn\"]")).clear();
        chromeDriver.findElement(By.xpath("//*[@id=\"ogrn\"]")).sendKeys("1127028000199");
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[1]/div/div/div/ul/li[2]")));
        chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[1]/div/div/div/ul/li[2]")).click();
        logger.info("На вкладке \"Отчетные данные\" в 1-м поле (Исходящая – всего (сумма строк 103, 111)) вводим \"100\"");
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"0101.03\"]")));
        chromeDriver.findElement(By.xpath("//*[@id=\"0101.03\"]")).sendKeys("100");
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[6]/div/div/div[1]/div[1]/button")));
        logger.info("Нажимаем \"Проверить\"");
        chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[6]/div/div/div[1]/div[1]/button")).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[5]/h3")));
        logger.info("На странице присутствует блок \"Ошибки при заполнении\", \n" +
                "\"Правило не пройдено: Сумма значений ячеек по графе 03 строк 0103, 0111 должна быть равна значению ячейки по графе 03 по строке 0101\"");
        Assert.assertTrue(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[5]/h3")).getText().contains("Ошибки при заполнении"));
        Assert.assertTrue(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[5]/div/ul/li/div/div/div/div/div/div[2]/div[2]/div/p")).getText().
                contains("Правило не пройдено: Сумма значений ячеек по графе 03 строк 0103, 0111 должна быть равна значению ячейки по графе 03 по строке 0101"));
        logger.info("На странице присутствует блок \"Предупреждение\", \"Ячейка : ОГРН (...) не совпал со значением в базе данных (...). Найденный оператор: "+userConfig.get("operator")+".");
        Assert.assertTrue(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[6]/h3")).getText().contains("Предупреждение"));
        Assert.assertTrue(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[6]/div/ul/li/div/div/div/div/div/div[2]/div[2]/div/p")).getText().contains("Ячейка : ОГРН (1127028000199) не совпал со значением в базе данных (1127028000164)."));
        Assert.assertTrue(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[6]/div/ul/li/div/div/div/div/div/div[2]/div[2]/div/p")).getText().contains("Найденный оператор: "+userConfig.get("operator")+"."));
        logger.info("Статус формы \"Ошибка данных\"");
        Assert.assertTrue(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/ul[1]/li[4]/div/span")).getText().contains("Ошибка данных"));
        logger.info("Вводим комментарии к ошибкам и предупреждениям");
        chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[5]/div/ul/li/div/div/div/div/div/div[3]/div[2]/textarea")).sendKeys("ok");
        chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[6]/div/ul/li/div/div/div/div/div/div[3]/div[2]/textarea")).sendKeys("ok");
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[6]/div/ul/li/div/div/div/div/div/div[3]/div[2]/textarea")));
        logger.info("Отправляем с подписью ЭП");
        WebElement button = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[8]/div/div/div[1]/div[2]/button")));
        while (true) {
            try {
                {
                    button.click();
                    break;
                }
            } catch (WebDriverException e) {
                continue;
            }
        }

        wait.until(ExpectedConditions.textToBe(By.xpath("//*[@id=\"submitting_forms\"]/div/div[2]/div/button[2]/span"), "Подписать ЭП"));
        WebElement webElement = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"submitting_forms\"]/div/div[2]/div/button[2]")));
        while (true) {
            try {
                webElement.click();
                break;
            } catch (WebDriverException e) {
                continue;
            }
        }
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"submitting_forms_details\"]/div/div[2]/div/div[2]/div/div[2]/div"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"submitting_forms_details\"]/div/div[2]/div/div[2]/div/div[2]/div/div/ul/li[2]"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"submitting_forms_details\"]/div/div[3]/button[1]"))).click();
        logger.info("После подписания проверяем наличие текста \"Подпись успешно сформирована ");
        wait.until(ExpectedConditions.textToBe(By.xpath("//*[@id=\"submitting_forms_details\"]/div/div[2]/div/div[3]/ul/li/span"), "Подпись успешно сформирована"));
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"submitting_forms_details\"]/div/div[3]/button[1]"))).click();
        logger.info("Проверяем статус формы \"Отправлен\"");
        wait.until(ExpectedConditions.textToBe(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/ul[1]/li[4]/div/span"), "Отправлен"));
        lastDate = new Date();
    }

    @Test(groups = "smoke", dependsOnMethods = "sendWithWarnings", description = "Принятие формы в УШ")
    public void acceptInUSH() {
        logger.info("Открываем административный интерфейс, авторизуемся");
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
        logger.info("Открывается страница Календарь");
        wait.until(ExpectedConditions.textToBe(By.xpath("/html/body/div[4]/div/div/div[1]/div[1]/h2"), "Календарь"));
        logger.info("Переходим во вкладку Обработка форм (УШ)");
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"navbar-collapse\"]/ul[1]/li[4]/a"))).click();
        WebElement row = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[text()='" + userConfig.get("mail") + "']/../..")));
        logger.info("Проверяем 1-ю строку в списке: форма -  05.033");
        Assert.assertEquals("05.033", row.findElement(By.xpath("td[1]/div")).getText());
        logger.info("email  - введенный в форме email");
        Assert.assertEquals(userConfig.get("mail"), row.findElement(By.xpath("td[2]/div")).getText());
        logger.info("статус - \"На ручной обработке\"");
        Assert.assertEquals("На ручной обработке", row.findElement(By.xpath("td[3]/div")).getText());
        logger.info("Состояние - \"Есть предупреждения, Есть ошибки\"");
        Assert.assertTrue(row.findElement(By.xpath("td[4]/div")).getText().contains("Есть предупреждения, Есть ошибки"));
        logger.info("Предоставлен, Обновлен - текущая дата");
        try {
            Assert.assertTrue(checkDates(new SimpleDateFormat("dd.MM.yyyy HH:mm").parse(row.findElement(By.xpath("td[5]/div")).getText())));
            Assert.assertTrue(checkDates(new SimpleDateFormat("dd.MM.yyyy HH:mm").parse(row.findElement(By.xpath("td[6]/div")).getText())));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        logger.info("Оператор - название оператора");
        Assert.assertEquals(userConfig.get("operator"), row.findElement(By.xpath("td[7]/div")).getText());
        logger.info("Отчетный период - \"2016 год, месяц 3\"");
        Assert.assertEquals("2016 год, месяц 3", row.findElement(By.xpath("td[8]/div")).getText());
        logger.info("Историческая - Нет");
        Assert.assertTrue(!row.findElement(By.xpath("td[9]/div/input")).isSelected());
        logger.info("Подписано ЭП - Да");
        Assert.assertTrue(row.findElement(By.xpath("td[10]/div/input")).isSelected());
        logger.info("Переходим в эту форму");
        clickButtonJS(row.findElement(By.xpath("td[1]/div")));
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("/html/body/div[4]/div/div/div/div[2]/div/div[3]/div/div[1]"))).click();
        logger.info("Проверяем предупреждения в блоке Предупреждения, проверяем галочку рядом с ОГРН");
        clickButtonJS("/html/body/div[4]/div/div/div/div[2]/div/div[2]/div/div[1]");
        Assert.assertEquals("Ячейка : ОГРН (1127028000199) не совпал со значением в базе данных (1127028000164)." +
                " Найденный оператор: "+userConfig.get("operator")+"." +
                " Комментарий пользователя: ok",chromeDriver.findElement(By.xpath("//*[@id=\"formWarnings\"]/div/div[1]")).getText());
        Assert.assertEquals("Правило не пройдено: Сумма значений ячеек по графе 03 строк 0103, 0111 должна быть равна значению ячейки по графе 03 по строке 0101 Комментарий пользователя: ok",chromeDriver.findElement(By.xpath("//*[@id=\"formWarnings\"]/div/div[2]")).getText());
//todo: чекбокс!
// System.out.println(chromeDriver.findElement(By.xpath("//*[@id=\"titulCollapse\"]/div/form/div/div[5]/div/div[1]/div[1]/button")).getAttribute("value"));
//        Assert.assertTrue(chromeDriver.findElement(By.xpath("//*[@id=\"titulCollapse\"]/div/form/div/div[5]/div/div[1]/div[1]/button/i")).getAttribute("checked").equals("true"));

        Assert.assertTrue(chromeDriver.findElements(By.xpath("//*[@id=\"titulCollapse\"]/div/form/div/div[5]/div/div[1]/div[1]/button")).size() > 0);
        Assert.assertTrue(chromeDriver.findElement(By.xpath("//*[@id=\"titulCollapse\"]/div/form/div/div[5]/div/div[1]/div[1]/button")).getAttribute("title").contains("Применить различие?"));
        Assert.assertTrue(chromeDriver.findElement(By.xpath("//*[@id=\"titulCollapse\"]/div/form/div/div[5]/div/div[1]/div[1]/button")).isDisplayed());
        logger.info("Вводим комментарий");
        chromeDriver.findElement(By.xpath("/html/body/div[4]/div/div/div/div[2]/div/div[7]/div/form/div/div/textarea")).sendKeys("Комментарий");
        chromeDriver.manage().timeouts().implicitlyWait(300, TimeUnit.MILLISECONDS);
        logger.info("Нажимяем \"Принять\" с комментарием");
        chromeDriver.findElement(By.xpath("/html/body/div[4]/div/div/div/div[2]/div/div[7]/div/form/div/table/tbody/tr/td[1]/button")).click();
        sleep(500);
        chromeDriver.manage().timeouts().implicitlyWait(300, TimeUnit.MILLISECONDS);
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"navbar-collapse\"]/ul[1]/li[4]/a"))).click();
        chromeDriver.navigate().refresh();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/div[4]/div/div/div/div[2]/div/div[3]/div/table/tbody[1]/tr[1]/td[3]/div")));
        logger.info("Проверяем статус формы \"Принят\" в Армаде");
        row = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[text()='" + userConfig.get("mail") + "']/../..")));
        Assert.assertEquals("Принят", row.findElement(By.xpath("td[3]/div")).getText());
        logger.info("Проверяем стаус Принят и комментарий в ЛК");
        chromeDriver.get(config.get("url"));
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"header\"]/div/div/div/div[2]/a[1]"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/div/div[1]/div[2]/div[2]/div/p/a"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[2]/div/div[3]/div[2]/div/p/a"))).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]")));
        Assert.assertEquals("Комментарий", chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div/p[2]")).getText());
        Assert.assertEquals("Принят", chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/ul[1]/li[4]/div/span")).getText());
        logger.info("Проверяем письмо на почте");
        String mailContent = MailClient.getContent("mail." + userConfig.get("mail").split("@")[1], userConfig.get("mail").split("@")[0], userConfig.get("mailpassword"));
        Assert.assertTrue(mailContent.contains("Форма принята."));
        Assert.assertTrue(mailContent.contains("Комментарий"));
    }

    @Test(groups = "smoke", dependsOnMethods = "acceptInUSH", description = "Импорт УШ формы из Excel")
    public void importFromExcel() throws AWTException {
        logger.info("Возвращаемся в ЛК, переходим в 2016/Формы за 1 квартал/ФФСН № 5-связь/форма головной компании (Москва)");
        logger.info("Внизу формы есть кнопка \"Редактировать\"");
        logger.info("Нажимаем кнопку \"Редактировать\"");
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[4]/div[6]/div/div/div/div/button"))).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"editing_shape_form\"]/div/div[2]/p")));
        wait.until(ExpectedConditions.textToBe(By.xpath("//*[@id=\"editing_shape_form\"]/div/div[2]/div/button[1]/span"), "Редактировать"));
        logger.info("Появляется Предупреждение \"Ваша форма принята, при подтверждении дальнейшего редактирования\n" +
                "предоставленные ранее данные аннулируются, форма будет считаться не сданной.\n" +
                "Продолжить?\"  с кнопками \"Редактировать\", \"Отмена\"");
        Assert.assertTrue(chromeDriver.findElement(By.xpath("//*[@id=\"editing_shape_form\"]/div/div[2]/p")).getText().contains("Ваша форма принята, при подтверждении дальнейшего редактирования"));
        Assert.assertTrue(chromeDriver.findElement(By.xpath("//*[@id=\"editing_shape_form\"]/div/div[2]/p")).getText().contains("предоставленные ранее данные аннулируются, форма будет считаться не сданной."));
        wait.until(ExpectedConditions.textToBe(By.xpath("//*[@id=\"editing_shape_form\"]/div/div[2]/div/button[2]/span"), "Отмена"));
        wait.until(ExpectedConditions.textToBe(By.xpath("//*[@id=\"editing_shape_form\"]/div/div[2]/div/button[1]"), "Редактировать"));
        logger.info("Нажимаем \"Редактировать\"");
        clickButtonJS("//*[@id=\"editing_shape_form\"]/div/div[2]/div/button[1]");
        chromeDriver.manage().timeouts().implicitlyWait(300, TimeUnit.MILLISECONDS);
        logger.info("Статус формы \"В работе\"");
        wait.until(ExpectedConditions.textToBe(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/ul[1]/li[4]/div/span"), "В работе"));
        logger.info("Нажимаем \"Импортировать из универсального шаблона\"");
        clickButtonJS("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/ul[2]/li[2]/a");
        logger.info("Появляется окно \"Импортировать из универсального шаблона\"");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"fancy_import\"]/h3")));
        Assert.assertTrue(chromeDriver.findElement(By.xpath("//*[@id=\"fancy_import\"]/h3")).getText().contains("Импортировать"));
        Assert.assertTrue(chromeDriver.findElement(By.xpath("//*[@id=\"fancy_import\"]/h3")).getText().contains("из универсального шаблона"));
        ((JavascriptExecutor) chromeDriver).executeScript(
                "HTMLInputElement.prototype.click = function() {                     " +
                        "  if(this.type !== 'file') HTMLElement.prototype.click.call(this);  " +
                        "};                                                                  ");
        logger.info("Нажимаем \"Импортировать\"");
        clickButton("//*[@id=\"drop\"]/button/span");
        logger.info("Выбираем файл "+new File(config.get("moscowForm")).getAbsolutePath());
        chromeDriver.findElement(By.xpath("//*[@id=\"drop\"]/input")).sendKeys(new File(config.get("moscowForm")).getAbsolutePath());
        chromeDriver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
        WebElement webElement0 = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[1]/div/div/div/ul/li[2]/span")));
        while (true) {
            try {
                webElement0.click();
                break;
            } catch (WebDriverException e) {
                continue;
            }
        }

        logger.info("Окно закрывается, данные формы заполнены данными из файла");
        List<WebElement> rows = chromeDriver.findElements(By.xpath("//*[@id=\"gl_form2\"]/div[2]/table/tbody/tr[.]"));
        HashMap<String, String> siteMap = new HashMap<>();
        for (WebElement webElement : rows) {
            if (webElement.findElements(By.xpath("td[5]/input")).size() > 0) {
                siteMap.put(String.valueOf(Integer.parseInt(webElement.findElement(By.xpath("td[1]/p")).getText())) + "." + webElement.findElement(By.xpath("td[2]/p")).getText(),
                        webElement.findElement(By.xpath("td[5]/input")).getAttribute("value"));
            }
        }
        Assert.assertTrue(MainUtil.compareMaps(siteMap, ExcelParser.getValues(new File(config.get("moscowForm")).getAbsolutePath())));
        clickButton("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[1]/div/div/div/ul/li[1]/span");
        logger.info("Заполняем поле \"Испольнитель\", \"Email\" корректными значенями и нажимаем \"Отправить\", отправляем без ЭП");
        sendKeysToInput("//*[@id=\"author\"]", "Исполнитель");
        sendKeysToInput("//*[@id=\"authorEmail\"]", userConfig.get("mail"));
        sendKeysToInput("//*[@id=\"ogrn\"]", userConfig.get("gogrn"));
        clickButton("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[6]/div/div/div[1]/div[2]/button");
        chromeDriver.manage().timeouts().implicitlyWait(300, TimeUnit.MILLISECONDS);
        clickButton("//*[@id=\"submitting_forms\"]/div/div[2]/div/button[1]/span");
        logger.info("Проверяем статус \"Принят\"");
        wait.until(ExpectedConditions.textToBe(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/ul[1]/li[4]/div/span"), "Принят"));
        logger.info("Переходим в 2016/Формы за 1 квартал/ФФСН № 5-связь/Сводный отчет по организации");
        clickButton("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/div/div/div/div/div/div/a");
        clickButton("//*[@id=\"wrapper\"]/ng-component/ng-component/div[2]/div/div[1]/div[2]/div/p/a");
        logger.info("Появляется окно Уведомление");
        Assert.assertTrue(chromeDriver.findElements(By.xpath("/html/body/div")).size() > 0);
        wait.until(ExpectedConditions.textToBe(By.xpath("//*[@id=\"summary_accepted\"]/div/div[1]/h3"), "Уведомление"));
        logger.info("Нажимаем \"Сдать сводный отчет\"");
        clickButton("//*[@id=\"summary_accepted\"]/div/div[2]/div/div/button[1]/span");
        logger.info("Открывается форма, Признак сводного отчета = true");
        Assert.assertTrue(chromeDriver.findElements(By.xpath("//*[@id=\"checkboxG1\"]")).size() > 0);
        Assert.assertTrue(chromeDriver.findElement(By.xpath("//*[@id=\"checkboxG1\"]")).isSelected());
        logger.info("Нажимаем \"Импортировать из универсального шаблона\"");
        clickButton("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/ul[2]/li[2]/a");
        logger.info("Появляется окно \"Импортировать\n" +
                "из универсального шаблона\"");
        Assert.assertTrue(chromeDriver.findElement(By.xpath("//*[@id=\"fancy_import\"]/h3")).getText().contains("Импортировать"));
        Assert.assertTrue(chromeDriver.findElement(By.xpath("//*[@id=\"fancy_import\"]/h3")).getText().contains("из универсального шаблона"));
        clickButton("//*[@id=\"drop\"]/button/span");
        chromeDriver.manage().timeouts().implicitlyWait(300, TimeUnit.MILLISECONDS);
        logger.info("Выбираем файл \"F1-05.033 1.9.0 - сводный.xls\"");
        ((JavascriptExecutor) chromeDriver).executeScript(
                "HTMLInputElement.prototype.click = function() {                     " +
                        "  if(this.type !== 'file') HTMLElement.prototype.click.call(this);  " +
                        "};                                                                  ");
        clickButton("//*[@id=\"drop\"]/button/span");
        chromeDriver.findElement(By.xpath("//*[@id=\"drop\"]/input")).sendKeys(new File(config.get("summaryForm")).getAbsolutePath());
        chromeDriver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
        logger.info("Окно закрывается, данные формы заполнены данными из файла");
        clickButtonJS("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[1]/div/div/div/ul/li[2]/span");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        List<WebElement> rows2 = chromeDriver.findElements(By.xpath("//*[@id=\"gl_form2\"]/div[2]/table/tbody/tr[.]"));
        HashMap<String, String> siteMap2 = new HashMap<>();
        for (WebElement webElement : rows2) {
            if (webElement.findElements(By.xpath("td[5]/input")).size() > 0) {
                siteMap2.put(String.valueOf(Integer.parseInt(webElement.findElement(By.xpath("td[1]/p")).getText())) + "." + webElement.findElement(By.xpath("td[2]/p")).getText(),
                        webElement.findElement(By.xpath("td[5]/input")).getAttribute("value"));
            }
        }

        Assert.assertTrue(MainUtil.compareMaps(siteMap2, ExcelParser.getValues(new File(config.get("summaryForm")).getAbsolutePath())));
        logger.info("Заполняем поле \"Испольнитель\", \"Email\" корректными значенями и нажимаем \"Отправить\", отправляем без ЭП");
        clickButton("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[1]/div/div/div/ul/li[1]/span");
        sendKeysToInput("//*[@id=\"author\"]", "Исполнитель");
        sendKeysToInput("//*[@id=\"authorEmail\"]", userConfig.get("mail"));
        sendKeysToInput("//*[@id=\"ogrn\"]", userConfig.get("gogrn"));
        clickButton("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[6]/div/div/div[1]/div[2]/button");
        chromeDriver.manage().timeouts().implicitlyWait(300, TimeUnit.MILLISECONDS);
        clickButtonCSS("#submitting_forms > div > div.popups_desc > div > button:nth-child(1)");
        logger.info("Проверяем статус \"Принят\"");
        wait.until(ExpectedConditions.textToBe(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/ul[1]/li[4]/div/span"), "Принят"));
        lastDate = new Date();
        logger.info("Переходим на уровень выше");
        clickButton("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/div/div/div/div/div/div/a");
        logger.info("Статус по форме \"ФФСН № 5-связь\" - \"Принят\"");
        wait.until(ExpectedConditions.textToBe(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[2]/div/div[1]/div[4]/div/p"), "Принят"));
        logger.info("Переходим в административный интерфейс");
        chromeDriver.navigate().to(config.get("urlArmada"));
        chromeDriver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
        logger.info("Переходим во вкладку Обработка форм (УШ)");
        clickButton("//*[@id=\"navbar-collapse\"]/ul[1]/li[4]/a");
        chromeDriver.manage().timeouts().implicitlyWait(1000, TimeUnit.MILLISECONDS);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/div[4]/div/div/div/div[2]/div/div[3]/div/table/tbody[1]/tr[1]/td[4]/div")));
        WebElement row = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[text()='" + userConfig.get("mail") + "']/../..")));
        logger.info("Проверяем 1-ю строку в списке: форма -  05.033");
        Assert.assertEquals("05.033", row.findElement(By.xpath("td[1]/div")).getText());
        logger.info("email  - введенный в форме email");
        Assert.assertEquals(userConfig.get("mail"), row.findElement(By.xpath("td[2]/div")).getText());
        logger.info("статус - \"Принят\"");
        Assert.assertEquals("Принят", row.findElement(By.xpath("td[3]/div")).getText());
        Assert.assertEquals("Это сводный отчёт",row.findElement(By.xpath("td[4]/div")).getText());
        logger.info("Предоставлен, Обновлен - текущая дата");
        try {
            Assert.assertTrue(checkDates(new SimpleDateFormat("dd.MM.yyyy HH:mm").parse(row.findElement(By.xpath("td[5]/div")).getText())));
            Assert.assertTrue(checkDates(new SimpleDateFormat("dd.MM.yyyy HH:mm").parse(row.findElement(By.xpath("td[6]/div")).getText())));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        logger.info("Оператор - название оператора");
        Assert.assertEquals(userConfig.get("operator"), row.findElement(By.xpath("td[7]/div")).getText());
        logger.info("Отчетный период - \"2016 год, месяц 3\"");
        Assert.assertEquals("2016 год, месяц 3", row.findElement(By.xpath("td[8]/div")).getText());
        logger.info("Историческая - Нет");
        Assert.assertTrue(!row.findElement(By.xpath("td[9]/div/input")).isSelected());
        logger.info("Подписано ЭП - Нет");
        Assert.assertTrue(!row.findElement(By.xpath("td[10]/div/input")).isSelected());
    }

    @Test(groups = "smoke", dependsOnMethods = "acceptInUSH", description = "Импорт УШ формы из Excel")
    public void sendViaMail() throws UnsupportedEncodingException, MessagingException {
        logger.info("Отправляем письмо на адрес "+config.get("mailTo")+" с адреса " +config.get("mailFrom")+" с вложением "+ new File(config.get("sendForm")).getAbsolutePath());
        MailClient.sendMessage(config.get("mailTo"), config.get("mailFrom"), new File(config.get("sendForm")));
        lastDate = new Date();
        logger.info("Дожидаемся ответа на письмо в ящике statmks.test.user@12.voskhod.local");
        String content = MailClient.getContentFrom(config.get("mailFrom"), config.get("passwordFrom"), config.get("mailTo"));
        logger.info("Проверяем в ответном письме \"из них сохранено в базе данных: 1\"");
        Assert.assertTrue(content.contains("из них сохранено в базе данных: \t1"));
        logger.info("Переходим в административный интерфейс");
        chromeDriver.navigate().to(config.get("urlArmada"));
        logger.info("Переходим во вкладку Обработка форм (УШ)");
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"navbar-collapse\"]/ul[1]/li[4]/a")));
        clickButton("//*[@id=\"navbar-collapse\"]/ul[1]/li[4]/a");
        WebElement row = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[text()='" + userConfig.get("mail") + "']/../..")));
        logger.info("Проверяем 1-ю строку в списке: форма -  05.033");
        Assert.assertEquals("03.033", row.findElement(By.xpath("td[1]/div")).getText());
        logger.info("email  - введенный в форме email");
        Assert.assertEquals(userConfig.get("mail"), row.findElement(By.xpath("td[2]/div")).getText());
        logger.info("статус - \"Принят\"");
        Assert.assertEquals("На ручной обработке", row.findElement(By.xpath("td[3]/div")).getText());
        logger.info("Состояние - \"Это сводный отчет\"");
        Assert.assertTrue(row.findElement(By.xpath("td[4]/div")).getText().contains("Есть предупреждения"));
        logger.info("Предоставлен, Обновлен - текущая дата");
        try {
            Assert.assertTrue(checkDates(new SimpleDateFormat("dd.MM.yyyy HH:mm").parse(row.findElement(By.xpath("td[5]/div")).getText())));
            Assert.assertTrue(checkDates(new SimpleDateFormat("dd.MM.yyyy HH:mm").parse(row.findElement(By.xpath("td[6]/div")).getText())));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        logger.info("Оператор - название оператора");
        Assert.assertEquals(config.get("mailOperator"), row.findElement(By.xpath("td[7]/div")).getText());
        logger.info("Отчетный период - \"2016 год, месяц 3\"");
        Assert.assertEquals("2016 год, месяц 3", row.findElement(By.xpath("td[8]/div")).getText());
        logger.info("Историческая - Нет");
        Assert.assertTrue(!row.findElement(By.xpath("td[9]/div/input")).isSelected());
        logger.info("Подписано ЭП - Нет");
        Assert.assertTrue(!row.findElement(By.xpath("td[10]/div/input")).isSelected());
    }

    @Test(groups = "smoke", dependsOnMethods = "sendViaMail")
    public void manualInputArmada() {
        logger.info("В администратоивном интерфейсе переходим на вкладку \"Ввод форм\", в фильтре \"Операторы связи\" вводим \""+config.get("manualOperator")+"\" и нажимаем кнопку поиска");
        chromeDriver.get(config.get("urlArmada"));
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"navbar-collapse\"]/ul[1]/li[5]/a")));
        clickButton("//*[@id=\"navbar-collapse\"]/ul[1]/li[5]/a");
        chromeDriver.manage().timeouts().implicitlyWait(300, TimeUnit.MILLISECONDS);
        sendKeysToInput("/html/body/div[4]/div/div/div[2]/div[1]/div/div[1]/div/div[1]/div/div/input", config.get("manualOperator"));
        clickButton("/html/body/div[4]/div/div/div[2]/div[1]/div/div[1]/div/div[1]/div/div/span[2]/button[2]");
        chromeDriver.manage().timeouts().implicitlyWait(300, TimeUnit.MILLISECONDS);
        List<WebElement> operators = chromeDriver.findElements(By.xpath("/html/body/div[4]/div/div/div[2]/div[1]/div/div[1]/div/div[3]/div/div/div[3]/div/div[2]/div/div/table/tbody/tr[.]/td[1]/div"));
        chromeDriver.manage().timeouts().implicitlyWait(300, TimeUnit.MILLISECONDS);
        logger.info("Выбираем найденного оператора");
        circle:
        while (true) {
            for (WebElement operatorElement : operators) {
                chromeDriver.manage().timeouts().implicitlyWait(300, TimeUnit.MILLISECONDS);
                try {
                    if (operatorElement.getText().equals(config.get("manualOperator"))) {
                        operatorElement.click();
                        break circle;
                    }
                } catch (StaleElementReferenceException e) {
                    operators = chromeDriver.findElements(By.xpath("/html/body/div[4]/div/div/div[2]/div[1]/div/div[1]/div/div[3]/div/div/div[3]/div/div[2]/div/div/table/tbody/tr[.]/td[1]/div"));
                    continue circle;
                }
            }
        }
        logger.info("Проверка, что установлен нужный оператор");
        wait.until(ExpectedConditions.textToBe(By.xpath("/html/body/div[4]/div/div/div[2]/div[2]/h4"), userConfig.get("operatorFil")));
        Assert.assertEquals(userConfig.get("operatorFil"),chromeDriver.findElement(By.xpath("/html/body/div[4]/div/div/div[2]/div[2]/h4")).getText());
        logger.info("В фильтре выбираем "+config.get("manualType"));
        while (true) {
            try {
                String attribute = chromeDriver.findElement(By.xpath("/html/body/div[4]/div/div/div[2]/div[2]/div[1]/nav/form[1]/div/div/button")).getAttribute("aria-expanded");
                if (attribute == null) {
                    clickButtonJS("/html/body/div[4]/div/div/div[2]/div[2]/div[1]/nav/form[1]/div/div/button");
                    continue;
                }
                if (attribute.equals("false")) {
                    clickButtonJS("/html/body/div[4]/div/div/div[2]/div[2]/div[1]/nav/form[1]/div/div/button");
                } else {
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        List<WebElement> options = chromeDriver.findElements(By.xpath("/html/body/div[4]/div/div/div[2]/div[2]/div[1]/nav/form[1]/div/div/div/ul/li[.]/a/span[1]"));
        for (WebElement option : options) {
            if (option.getText().equals(config.get("manualType"))) {
                option.click();
                break;
            }
        }
        logger.info("Выбираем "+config.get("manualPeriod"));
        while (true) {
            try {
                String attribute = chromeDriver.findElement(By.xpath("/html/body/div[4]/div/div/div[2]/div[2]/div[1]/nav/form[2]/div/div/button")).getAttribute("aria-expanded");
                if (attribute == null) {
                    clickButton("/html/body/div[4]/div/div/div[2]/div[2]/div[1]/nav/form[2]/div/div/button");
                    continue;
                }
                if (attribute.equals("false")) {
                    clickButton("/html/body/div[4]/div/div/div[2]/div[2]/div[1]/nav/form[2]/div/div/button");
                } else {
                    break;
                }
            } catch (Exception e) {
            }

        }
        options = chromeDriver.findElements(By.xpath("/html/body/div[4]/div/div/div[2]/div[2]/div[1]/nav/form[2]/div/div/div/ul/li[.]/a/span[1]"));
        for (WebElement option : options) {
            if (option.getText().equals(config.get("manualPeriod"))) {
                option.click();
                break;
            }
        }
        logger.info("Выбираем в фильтре "+config.get("manualYear"));
        while (true) {
            try {
                String attribute = chromeDriver.findElement(By.xpath("/html/body/div[4]/div/div/div[2]/div[2]/div[1]/nav/form[3]/div/div/button")).getAttribute("aria-expanded");
                if (attribute == null) {
                    clickButton("/html/body/div[4]/div/div/div[2]/div[2]/div[1]/nav/form[3]/div/div/button");
                    continue;
                }
                if (attribute.equals("false")) {
                    clickButton("/html/body/div[4]/div/div/div[2]/div[2]/div[1]/nav/form[3]/div/div/button");
                } else {
                    break;
                }
            } catch (Exception e) {
            }

        }

        options = chromeDriver.findElements(By.xpath("/html/body/div[4]/div/div/div[2]/div[2]/div[1]/nav/form[3]/div/div/div/ul/li[.]/a/span[1]"));
        for (WebElement option : options) {
            if (option.getText().equals(config.get("manualYear"))) {
                option.click();
                break;
            }
        }
        chromeDriver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
        logger.info("Открывается форма (если открывается окно \"Найдены ранее созданные отчеты:\"");
        logger.info("Нажимаем \"Создать новый отчет\"");
        clickButton("/html/body/div[4]/div/div/div[2]/div[2]/div[1]/nav/a[1]");
        sleep(1000);
        try {
            if(chromeDriver.findElements(By.xpath("/html/body/div[5]/div/div/div[3]/button[1]")).size()>0){
                clickButton("/html/body/div[5]/div/div/div[3]/button[1]");
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        chromeDriver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
        logger.info("На титульной части формы заполняем обязательные поля");
        try {
            sendKeysToInput("//*[@id=\"titleTab\"]/div/form/div/div[11]/div/div[2]/div[1]/div/div[2]/div/input", "Исполнитель");
        } catch (InvalidElementStateException e) {
            e.printStackTrace();
        }
        sendKeysToInput("//*[@id=\"titleTab\"]/div/form/div/div[11]/div/div[2]/div[2]/div[1]/div[2]/div/input", userConfig.get("mail"));
        sendKeysToInput("//*[@id=\"titleTab\"]/div/form/div/div[11]/div/div[2]/div[2]/div[2]/div[2]/div/input", userConfig.get("phone"));
        sendKeysToInput("//*[@id=\"titleTab\"]/div/form/div/div[9]/div/div[2]/div[2]/div/input", userConfig.get("zip"));
        logger.info("В табличной части формы заполняем данные: 1, 1 в первые два поля");
        clickButtonCSS("body > div.container-fa.container-fluid > div > div > div:nth-child(2) > div.col-md-9 > div:nth-child(4) > ul > li:nth-child(2) > a");
        sendKeysToInput("//*[@id=\"tableTab\"]/div/div[1]/input", "1");
        sendKeysToInput("//*[@id=\"tableTab\"]/div/div[2]/input", "1");
        logger.info("Нажимаем \"Проверить\"");
        clickButton("/html/body/div[4]/div/div/div[2]/div[2]/div[3]/a[1]/i");
        logger.info("Появляется предупреждение \"Зафиксировано опоздание. Рекомендуется сдавать отчётность не позднее: \"");
        chromeDriver.manage().timeouts().implicitlyWait(300, TimeUnit.MILLISECONDS);
        wait.until(ExpectedConditions.textToBe(By.xpath("//*[@id=\"warningsTab\"]/div/div/div[1]"), "Предупреждение:"));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"warningsTab\"]/div/div/div[2]/div")));
        Assert.assertTrue(chromeDriver.findElement(By.xpath("//*[@id=\"warningsTab\"]/div/div/div[2]/div")).getText().contains("Зафиксировано опоздание. Рекомендуется сдавать отчётность не позднее:"));
        sleep(8000);
        logger.info("Вводим комментарий к предупреждению");
        sendKeysToInput("//*[@id=\"warningsTab\"]/div/div/div[3]/textarea", "ок");
        logger.info("Нажимаем \"Отправить форму\"");
        clickButton("/html/body/div[4]/div/div/div[2]/div[2]/div[3]/a[2]/i");
        logger.info("Всплывает окошко \"Отправка формы: Форма отправлена\"");
        sleep(500);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/div[5]/span[3]")));
        Assert.assertEquals("Форма отправлена",chromeDriver.findElement(By.xpath("/html/body/div[5]/span[3]")).getText());
        Assert.assertEquals("Отправка формы:",chromeDriver.findElement(By.xpath("/html/body/div[5]/span[2]")).getText());
        logger.info("Переходим в административный интерфейс, переходим во вкладку Обработка форм (УШ)");
        clickButtonCSS("#navbar-collapse > ul:nth-child(1) > li:nth-child(4) > a");
        lastDate = new Date();
        chromeDriver.manage().timeouts().implicitlyWait(300, TimeUnit.MILLISECONDS);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/div[4]/div/div/div/div[2]/div/div[3]/div/table/tbody[1]/tr[1]/td[2]")));
        WebElement row = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[text()='" + userConfig.get("mail") + "']/../..")));
        logger.info("Проверяем значения первой формы в таблице");
        logger.info("Проверяем 1-ю строку в списке: форма -  "+config.get("manualCode"));
        Assert.assertEquals(config.get("manualCode"), row.findElement(By.xpath("td[1]/div")).getText());
        logger.info("email  - введенный в форме email");
        Assert.assertEquals(userConfig.get("mail"), row.findElement(By.xpath("td[2]/div")).getText());
        logger.info("статус - \"На ручной обработке\"");
        Assert.assertEquals("На ручной обработке", row.findElement(By.xpath("td[3]/div")).getText());
        logger.info("Состояние - \"Есть предупреждения\"");
        Assert.assertTrue(row.findElement(By.xpath("td[4]/div")).getText().contains("Есть предупреждения"));
        Assert.assertTrue(row.findElement(By.xpath("td[4]/div")).getText().contains("Отчёт просрочен"));
        logger.info("Предоставлен, Обновлен - текущая дата");
        try {
            Assert.assertTrue(checkDates(new SimpleDateFormat("dd.MM.yyyy HH:mm").parse(row.findElement(By.xpath("td[5]/div")).getText())));
            Assert.assertTrue(checkDates(new SimpleDateFormat("dd.MM.yyyy HH:mm").parse(row.findElement(By.xpath("td[6]/div")).getText())));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        logger.info("Оператор - название оператора");
        Assert.assertEquals(config.get("manualOperator"), row.findElement(By.xpath("td[7]/div")).getText());
        logger.info("Отчетный период: "+config.get("manualYear")+", "+config.get("manualMonth"));
        Assert.assertEquals(config.get("manualYear")+" год, "+config.get("manualMonth"), row.findElement(By.xpath("td[8]/div")).getText());
        logger.info("Историческая - Нет");
        Assert.assertTrue(!row.findElement(By.xpath("td[9]/div/input")).isSelected());
        logger.info("Подписано ЭП - Нет");
        Assert.assertTrue(!row.findElement(By.xpath("td[10]/div/input")).isSelected());

    }

    @Test(groups = "smoke", dependsOnMethods = "manualInputArmada")
    void generateInArmada() {
        logger.info("В Административном интерфейсе переходим на вкладку \"Исходящие отчеты\"->Шаблоны");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"navbar-collapse\"]/ul[1]/li[3]/a")));
        chromeDriver.findElement(By.xpath("//*[@id=\"navbar-collapse\"]/ul[1]/li[3]/a")).click();
        List<WebElement> dictionaries = chromeDriver.findElements(By.xpath("//*[@id=\"navbar-collapse\"]/ul[1]/li[3]/ul/li[.]/a"));
        for (WebElement webElement : dictionaries) {
            if (webElement.getText().equals("Шаблоны")) {
                webElement.click();
                logger.info("Открывается список, в котором присутствует файл отчета year_form");
                wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[text()='year_form']")));
                break;
            }
        }
        clickButton("//*[text()='year_form']");
        chromeDriver.manage().timeouts().implicitlyWait(300, TimeUnit.MILLISECONDS);
        logger.info("Выбираем строку с файлом отчета year_form, нажимаем  кнопку Выполнить");
        clickButton("/html/body/div[4]/div/div/div[2]/button[4]/i");
        logger.info("Откывается окошко \"Параметры отчета Годовой бланк\"");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/div[5]/div/div/div[1]/h4")));
        logger.info("В поле \"Код формы\" вводим \"05.033\", в поле \"Год\" - 2016, нажимаем \"Сформировать\"");
        sendKeysToInput("//*[@id=\"form\"]", config.get("generateCode"));
        sendKeysToInput("//*[@id=\"year\"]", config.get("generateYear"));
        clickButton("/html/body/div[5]/div/div/div[3]/button[1]");
        logger.info("Всплывает окошко \"Задача генерации отчета успешно создана\"");
        wait.until(ExpectedConditions.textToBe(By.xpath("/html/body/div[5]/span[3]"), "Задача генерации отчета успешно создана"));
        logger.info("Дожидаемся появления окошка \"Отчет успешно сгенерирован\", переходим по ссылке");
        wait.until(ExpectedConditions.textToBe(By.xpath("/html/body/div[5]/span[2]"), "Отчет успешно сгенерирован"));
        clickButton("/html/body/div[5]/a");
        logger.info("По ссылке скачивается отчет");
        logger.info("Открываем отчет");
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        File report = lastModified("data/docs");
        ExcelParser excelParser = new ExcelParser(report);
        logger.info("На 1-м листе присутствует текст \"С В О Д Н Ы Й   О Т Ч Е Т \", \"По форме ФФСН № 3-связь на конец 2016 года\"");
        Assert.assertTrue(excelParser.rowEqualsValue(0, "С В О Д Н Ы Й   О Т Ч Е Т    "));
        Assert.assertTrue(excelParser.rowEqualsValue(4, "По форме ФФСН № 3-связь на конец 2016 года"));
    }

    @Test(groups = "smoke", dependsOnMethods = "generateInArmada")
    void reportViewArmada() {
        logger.info("\"В Административном интерфейсе переходим на вкладку \"\"Исходящие отчеты\"\"->Архив отчетов");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"navbar-collapse\"]/ul[1]/li[3]/a")));
        clickButtonJS("//*[@id=\"navbar-collapse\"]/ul[1]/li[3]/a");
        List<WebElement> reports = chromeDriver.findElements(By.xpath("//*[@id=\"navbar-collapse\"]/ul[1]/li[3]/ul/li[.]/a"));
        for (WebElement webElement : reports) {
            if (webElement.getText().equals("Архив отчетов")) {
                webElement.click();
                logger.info("Открывается список, в котором присутствует файл отчета year_form");
                wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/div[4]/div/div/div[2]/div/div[1]/div/div[1]/div/button")));
                break;
            }
        }

        clickButtonJS("/html/body/div[4]/div/div/div[2]/div/div[1]/div/div[1]/div/button");
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("/html/body/div[4]/div/div/div[2]/div/div[1]/div/div[1]/div/button")));
        logger.info("В фильтре \"\"Код формы\"\" вводим \"\"03.033\"\"");
        clickButtonJS("//*[@id=\"filterCollapse\"]/div/div[1]/div[2]/div/div/button");
        List<WebElement> forms = chromeDriver.findElements(By.xpath("//*[@id=\"filterCollapse\"]/div/div[1]/div[2]/div/div/div/ul/li[.]/a"));
        for (WebElement webElement : forms) {
            if (webElement.getText().equals(config.get("viewCode"))) {
                webElement.click();
                wait.until(ExpectedConditions.textToBe(By.xpath("//*[@id=\"filterCollapse\"]/div/div[1]/div[2]/div/div/button/span[1]"), config.get("viewCode")));
                break;
            }
        }
        logger.info("в фильтре \"\"Наименование отчета вводим \"\"Годовой отчет\"\" (название нужно вынести в параметр)");
        clickButtonJS("//*[@id=\"filterCollapse\"]/div/div[2]/div[2]/div/div/button");
        List<WebElement> templates = chromeDriver.findElements(By.xpath("//*[@id=\"filterCollapse\"]/div/div[2]/div[2]/div/div/div/ul/li[.]/a"));
        for (WebElement webElement : templates) {
            if (webElement.getText().equals(config.get("viewTemplate"))) {
                webElement.click();
                wait.until(ExpectedConditions.textToBe(By.xpath("//*[@id=\"filterCollapse\"]/div/div[2]/div[2]/div/div/button/span[1]"), config.get("viewTemplate")));
                break;
            }
        }
        logger.info("нажимаем \"\"Применить фильтр\"\"\"");
        clickButton("//*[@id=\"filterCollapse\"]/div/div[1]/div[1]/div");
        logger.info("Для 1-го найденного отчета нажимаем \"Скачать отчет\"");
        chromeDriver.manage().timeouts().implicitlyWait(200, TimeUnit.MILLISECONDS);
        sleep(10000);
        clickButtonJS("/html/body/div[4]/div/div/div[3]/div/div[1]/div[3]/div/div[2]/div/div/table/tbody/tr[1]/td[10]/div/button");
        logger.info("Открываем отчет");
        sleep(10000);
        Assert.assertEquals(file.listFiles().length, 4);
        File report = lastModified("data/docs");
        ExcelParser excelParser = new ExcelParser(report);
        logger.info("На 1-м листе присутствует текст \"С В О Д Н Ы Й   О Т Ч Е Т \", \"По форме ФФСН № 3-связь на конец 2016 года\"");
        Assert.assertTrue(excelParser.rowEqualsValue(0, "С В О Д Н Ы Й   О Т Ч Е Т    "));
        Assert.assertTrue(excelParser.rowEqualsValue(4, "По форме ФФСН № 3-связь на конец 2016 года"));
    }

    @Test(groups = {"regress"}, priority = 10, dependsOnGroups = "smoke")
    public void correctFormCheck() {
        logger.info("ПРЕДЕЙСТВИЯ (очистка истории, создание пользователя)");
        userActivation();
        logger.info("Авторизуемся в ЛК под созданным пользователем");
//        chromeDriver.get(config.get("url"));
        logger.info("Проверяем заголовока страницы \"Мои формы\", На вкладке \"2016\" присутствует текст \"Формы за 1 квартал\", \"Формы за 2 кварта\"\", \"Формы за 3 квартал\", \"Формы за 4 квартал\", \"Формы за 2016 год\"");
        Assert.assertEquals(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/h2")).getText(), "Мои формы");
        Assert.assertEquals(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/div/div[1]/div[1]/h3")).getText(), "Формы за 1 квартал");
        Assert.assertEquals(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/div/div[2]/div[1]/h3")).getText(), "Формы за 2 квартал");
        Assert.assertEquals(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/div/div[3]/div[1]/h3")).getText(), "Формы за 3 квартал");
        Assert.assertEquals(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/div/div[4]/div[1]/h3")).getText(), "Формы за 4 квартал");
        Assert.assertEquals(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/div/div[5]/div[1]/h3")).getText(), "Формы за 2016 год");
        logger.info("Проверяем, что общее количество форм - 41");
        Assert.assertEquals(chromeDriver.findElements(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/div/div[.]/div[.]/div[2]/div")).size(), 41);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/ul/li[2]")));
        chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/ul/li[2]")).click();
        logger.info("Загружаем формы 2017г");
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/div/div[.]/div[1]/h3")));
        logger.info("Проверяем заголовока страницы \"Мои формы\", На вкладке \"2017\" присутствует текст \"Формы за 1 квартал\", \"Формы за 2 кварта\"\", \"Формы за 3 квартал\", \"Формы за 4 квартал\", \"Формы за 2017 год\"");
        Assert.assertEquals(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/h2")).getText(), "Мои формы");
        Assert.assertEquals(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/div/div[1]/div[1]/h3")).getText(), "Формы за 1 квартал");
        Assert.assertEquals(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/div/div[2]/div[1]/h3")).getText(), "Формы за 2 квартал");
        Assert.assertEquals(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/div/div[3]/div[1]/h3")).getText(), "Формы за 3 квартал");
        Assert.assertEquals(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/div/div[4]/div[1]/h3")).getText(), "Формы за 4 квартал");
        Assert.assertEquals(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/div/div[5]/div[1]/h3")).getText(), "Формы за 2017 год");
        logger.info("Проверяем, что общее количество форм - 41");
        Assert.assertEquals(chromeDriver.findElements(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/div/div[.]/div[.]/div[2]/div")).size(), 41);
        logger.info("Проверяем, что 3 блока не подсвечено");
        List<WebElement> grey = chromeDriver.findElements(By.xpath("//*[@class=\"bg_colors empty\"]"));
        Assert.assertEquals(grey.size(), 3);
        logger.info("Проверяем, что отчеты за 3-4 квартал и 2017 год не подсвечены");
//        Assert.assertTrue(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/div/div[2]/div[1]/h3")).findElement(By.xpath(".//../..")).getAttribute("class").equals("bg_colors empty"));
        Assert.assertTrue(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/div/div[3]/div[1]/h3")).findElement(By.xpath(".//../..")).getAttribute("class").equals("bg_colors empty"));
        Assert.assertTrue(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/div/div[4]/div[1]/h3")).findElement(By.xpath(".//../..")).getAttribute("class").equals("bg_colors empty"));
        Assert.assertTrue(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/div/div[5]/div[1]/h3")).findElement(By.xpath(".//../..")).getAttribute("class").equals("bg_colors empty"));
        //Переходим в 2017/Формы за 1 квартал/ФФСН № 5-связь
        clickButtonJS("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/div/div[1]/div[2]/div[2]/div/p/a");
        //В  списке 3 отчета, проверить текст в 1-й колонке (как сейчас - Сводный отчет по организации, и т.д.)
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[2]/div//*[@class=\"clearfix global_table\"]")));
        Assert.assertEquals(3, chromeDriver.findElements(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[2]/div//*[@class=\"clearfix global_table\"]")).size());
        Assert.assertEquals("Сводный отчет по организации", chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[2]/div/div[1]/div[1]/div/p")).getText());
        Assert.assertEquals("Москва, Город", chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[2]/div/div[3]/div[1]/div/p")).getText());
        Assert.assertEquals("Адыгея, Республика", chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[2]/div/div[4]/div[1]/div/p")).getText());
        logger.info("Переходим в форму филиала (Адыгея)");
        clickButtonJS("//*[@id=\"wrapper\"]/ng-component/ng-component/div[2]/div/div[4]/div[2]/div/p/a");
        logger.info("Проверяем, что на странице есть текст \"Шаг 1\", \"Данные об организации\",  \"Наименование предприятия (структурного подразделения)\", \"Контактные данные\", поля \"Исполнитель\", \"Email\" подсвечены красным, статус \"Надо сдать\"");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/div/div/div/div/div/h3")));
        Assert.assertEquals("Шаг 1", chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[1]/div/div/div/ul/li[1]/p")).getText());
        Assert.assertEquals("Данные об организации", chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[1]/div/div/div/ul/li[1]/span")).getText());
        Assert.assertTrue(chromeDriver.findElement(By.xpath("//*[@id=\"titul_form\"]/div[1]/div/div[1]/div/div")).getText().contains("Наименование предприятия (структурного подразделения)"));
        Assert.assertEquals("Контактные данные", chromeDriver.findElement(By.xpath("//*[@id=\"titul_form\"]/div[5]/div/h3")).getText());
        Assert.assertEquals("rgba(255, 0, 0, 1)",chromeDriver.findElement(By.xpath("//*[@id=\"author\"]")).getCssValue("border-bottom-color"));
        Assert.assertEquals("rgba(255, 0, 0, 1)",chromeDriver.findElement(By.xpath("//*[@id=\"authorEmail\"]")).getCssValue("border-bottom-color"));
        Assert.assertEquals("Надо сдать", chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/ul[1]/li[4]/div/span")).getText());
        logger.info("Заполняем все поля титульной страницы и данные формы корректными значенями (для формы можно взять данные из файла) и нажимаем \"Проверить\"");
        sendKeysToInput("//*[@id=\"author\"]", "Исполнитель");
        sendKeysToInput("//*[@id=\"authorEmail\"]", userConfig.get("mail"));
        chromeDriver.manage().timeouts().implicitlyWait(300, TimeUnit.MILLISECONDS);
        clickButtonJS("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[6]/div/div/div[1]/div[1]/button");
        logger.info("В правом верхнем углу появляются 2 статуса \"Форма проверена\", \"Форма сохранена\" (они потом исчезают), статус формы \"В работе\", в Истории 2 записи \"Сохранен черновик\", \"Обновлен черновик\", дата \"Последняя операция\" обновлена");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("ng2-toast > div > div.toast-text")));
        wait.until(ExpectedConditions.numberOfElementsToBe(By.cssSelector("ng2-toast"), 2));
        List<WebElement> popups = chromeDriver.findElements(By.cssSelector("ng2-toast"));
        String sum = popups.get(0).getText() + popups.get(1).getText();
        Assert.assertTrue(sum.contains("Сохранение"));
        Assert.assertTrue(sum.contains("Проверка формы"));
        lastDate = new Date();
        wait.until(ExpectedConditions.numberOfElementsToBe(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[4]/div[2]/ul/li[.]"), 2));
        Assert.assertEquals(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[4]/div[2]/ul/li[1]/div[1]/div[2]/p")).getText(), "Обновлен черновик");
        Assert.assertEquals(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[4]/div[2]/ul/li[2]/div[1]/div[2]/p")).getText(), "Создан черновик");
        Assert.assertTrue(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/ul[1]/li[3]/div/span")).getText().contains(new SimpleDateFormat("dd").format(lastDate)));

        try {
            Assert.assertTrue(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/ul[1]/li[3]/div/span")).getText().contains(getCurrentMonth()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        Assert.assertTrue(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/ul[1]/li[3]/div/span")).getText().contains(new SimpleDateFormat("yyyy HH:mm").format(lastDate)));
        logger.info("Очищаем значения во всех необязательных полях: \"Адрес\", \"Индекс\", \"ОКВЭД\", \"ОКАТО\", \"Контактный телефон\", \"Дополнительное сообщение\" и нажимаем \"Проверить\"");
        clearInput("//*[@id=\"address\"]");
        clearInput("//*[@id=\"zip\"]");
        clearInput("//*[@id=\"okved\"]");
        clearInput("//*[@id=\"okato\"]");
        clearInput("//*[@id=\"authorPhone\"]");
        clearInput("//*[@id=\"form_comment\"]");
        clickButtonJS("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[6]/div/div/div[1]/div[1]/button");
        logger.info("В правом верхнем углу появляются 2 статуса \"Форма проверена\", \"Форма сохранена\" (они потом исчезают), статус формы \"В работе\", в Истории 2 записи \"Сохранен черновик\", \"Обновлен черновик\", дата \"Последняя операция\" обновлена");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("ng2-toast > div > div.toast-text")));
        wait.until(ExpectedConditions.numberOfElementsToBe(By.cssSelector("ng2-toast"), 2));
        List<WebElement> popups2 = chromeDriver.findElements(By.cssSelector("ng2-toast"));
        String sum2 = popups2.get(0).getText() + popups2.get(1).getText();
        Assert.assertTrue(sum2.contains("Сохранение"));
        Assert.assertTrue(sum2.contains("Проверка формы"));
        lastDate = new Date();

        wait.until(ExpectedConditions.numberOfElementsToBe(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[4]/div[2]/ul/li[.]"), 2));
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Assert.assertEquals(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[4]/div[2]/ul/li[1]/div[1]/div[2]/p")).getText(), "Обновлен черновик");
        Assert.assertEquals(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[4]/div[2]/ul/li[2]/div[1]/div[2]/p")).getText(), "Создан черновик");
        Assert.assertTrue(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/ul[1]/li[3]/div/span")).getText().contains(new SimpleDateFormat("dd").format(lastDate)));
        try {
            Assert.assertTrue(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/ul[1]/li[3]/div/span")).getText().contains(getCurrentMonth()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        Assert.assertTrue(chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/ul[1]/li[3]/div/span")).getText().contains(new SimpleDateFormat("yyyy HH:mm").format(lastDate)));
    }

    @Test(groups = {"regress"}, dependsOnMethods = "correctFormCheck", priority = 10)
    void wrongFLKFormCheck() {
        logger.info("Очищаем значения в поле \"ОГРН\" и нажимаем \"Проверить\"");
        String ogrn = chromeDriver.findElement(By.xpath("//*[@id=\"ogrn\"]")).getAttribute("value");
        clearInput("//*[@id=\"ogrn\"]");
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        clickButtonJS("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[6]/div/div/div[1]/div[1]/button");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("ng2-toast > div > div.toast-text")));
        logger.info("\"В правом верхнем углу появляется 2 статуса \"\"Сохранение\"\", \"\" Проверка формы Обнаружены ошибки/предупреждения\"\"");
        wait.until(ExpectedConditions.numberOfElementsToBe(By.cssSelector("ng2-toast"), 2));
        List<WebElement> popups2 = chromeDriver.findElements(By.cssSelector("ng2-toast"));
        String sum2 = popups2.get(0).getText() + popups2.get(1).getText();
        Assert.assertTrue(sum2.contains("Сохранение"));
        Assert.assertTrue(sum2.contains("Обнаружены ошибки/предупреждения"));
        logger.info("На странице присутствует блок \"\"Предупреждение\"\"");
        Assert.assertEquals("Предупреждение", chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[5]/h3")).getText());
        logger.info("\"\"Ячейка огрн: Внимание, не указан ОГРН!\"\"");
        Assert.assertEquals("Ячейка огрн: Внимание, не указан ОГРН!", chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[5]/div/ul/li/div/div/div/div/div/div[3]/div[2]/div/p")).getText());
        logger.info("Статус формы \"\"В работе\"\"\"");
        Assert.assertEquals("В работе", chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/ul[1]/li[4]/div/span")).getText());
        logger.info("Возвращаем значение \"ОГРН\", очистить значение \"ОКТМО\" и нажимаем \"Проверить\"");
        sendKeysToInput("//*[@id=\"ogrn\"]", ogrn);
        String oktmo = chromeDriver.findElement(By.xpath("//*[@id=\"oktmo\"]")).getAttribute("value");
        clearInput("//*[@id=\"oktmo\"]");
        clickButtonJS("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[7]/div/div/div[1]/div[1]/button");
        logger.info("В правом верхнем углу появляются 2 статуса \"\"Сохранение\"\", \"\" Проверка формы Обнаружены ошибки/предупреждения\"");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("ng2-toast > div > div.toast-text")));
        wait.until(ExpectedConditions.numberOfElementsToBe(By.cssSelector("ng2-toast"), 2));
        List<WebElement> popups3 = chromeDriver.findElements(By.cssSelector("ng2-toast"));
        String sum3 = popups3.get(0).getText() + popups3.get(1).getText();
        Assert.assertTrue(sum3.contains("Сохранение"));
        Assert.assertTrue(sum3.contains("Обнаружены ошибки/предупреждения"));
        sleep();
        logger.info("На странице присутствует блок \"\"Предупреждение\"\"");
        Assert.assertEquals("Предупреждение", chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[5]/h3")).getText());
        logger.info("\"\"Ячейка октмо: Внимание, не указан ОКТМО!\"\"");
        Assert.assertEquals("Ячейка октмо: Внимание, не указан ОКТМО!", chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[5]/div/ul/li/div/div/div/div/div/div[3]/div[2]/div/p")).getText());
        logger.info("Статус формы \"\"В работе\"\"\"");
        Assert.assertEquals("В работе", chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/ul[1]/li[4]/div/span")).getText());
        logger.info("Возвращаем значение \"ОКТМО\", очищаем значение \"Исполнитель\"");
        sendKeysToInput("//*[@id=\"oktmo\"]", oktmo);
        String author = chromeDriver.findElement(By.xpath("//*[@id=\"author\"]")).getAttribute("value");
        clearInput("//*[@id=\"author\"]");
        logger.info("Кнопка \"Проверить\" неактивна");
        Assert.assertFalse(isActive("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[7]/div/div/div[1]/div[1]/button"));
        logger.info("Возвращаем значение \"Исполнитель\", очищаем значение \"Email\"");
        sendKeysToInput("//*[@id=\"author\"]", author);
        String mail = chromeDriver.findElement(By.xpath("//*[@id=\"authorEmail\"]")).getAttribute("value");
        clearInput("//*[@id=\"authorEmail\"]");
        logger.info("Кнопка \"Проверить\" неактивна");
        Assert.assertFalse(isActive("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[7]/div/div/div[1]/div[1]/button"));
        logger.info("Возвращаем значение \"Email\", в поле \"Индекс\" вводим \"а\" и нажимаем \"Проверить\"");
        sendKeysToInput("//*[@id=\"authorEmail\"]", mail);
        sendKeysToInput("//*[@id=\"zip\"]", "a");
        clickButtonJS("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[7]/div/div/div[1]/div[1]/button");
        logger.info("В правом верхнем углу появляется 2 статуса \"\"Сохранение\"");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("ng2-toast > div > div.toast-text")));
        logger.info("Проверка формы Обнаружены ошибки/предупреждения");
        wait.until(ExpectedConditions.numberOfElementsToBe(By.cssSelector("ng2-toast"), 2));
        List<WebElement> popups4 = chromeDriver.findElements(By.cssSelector("ng2-toast"));
        String sum4 = popups4.get(0).getText() + popups4.get(1).getText();
        Assert.assertTrue(sum4.contains("Сохранение"));
        Assert.assertTrue(sum4.contains("Обнаружены ошибки/предупреждения"));
        sleep();
        logger.info("На странице присутствует блок \"Статус: Неверные данные\"");
        Assert.assertEquals("Неверные данные", chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[5]/div/ul/li/div/div/div/div/div/div[1]/div[3]/p/span")).getText());
        Assert.assertEquals("Ошибки при заполнении", chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[5]/h3")).getText());
        logger.info("Содержание ошибки: Ячейка почтовый индекс: Ячейка почтовый индекс не прошла проверку по правилу: Только цифры. 6 знаков.");
        Assert.assertEquals("Ячейка почтовый индекс: Ячейка почтовый индекс не прошла проверку по правилу: Только цифры. 6 знаков.", chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[5]/div/ul/li/div/div/div/div/div/div[2]/div[2]/div/p")).getText());
        logger.info("Статус формы \"Ошибка данных\",  в Истории самая верхняя запись - \"Ошибка данных\"");
        Assert.assertEquals("Ошибка данных", chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/ul[1]/li[4]/div/span")).getText());
        logger.info("Возвращаем значение \"Индекс\", в поле \"Код по ОКПО\" вводим \"а\" и нажимаем \"Проверить\" ");
        sendKeysToInput("//*[@id=\"zip\"]", userConfig.get("zip"));
        String okpo = chromeDriver.findElement(By.xpath("//*[@id=\"okpo\"]")).getAttribute("value");
        sendKeysToInput("//*[@id=\"okpo\"]", "a");
        clickButtonJS("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[7]/div/div/div[1]/div[1]/button");
        logger.info("В правом верхнем углу появляется 2 статуса \"Сохранение\", \"Проверка формы Обнаружены ошибки/предупреждения\"");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("ng2-toast > div > div.toast-text")));
        wait.until(ExpectedConditions.numberOfElementsToBe(By.cssSelector("ng2-toast"), 2));
        List<WebElement> popups5 = chromeDriver.findElements(By.cssSelector("ng2-toast"));
        String sum5 = popups5.get(0).getText() + popups5.get(1).getText();
        Assert.assertTrue(sum5.contains("Сохранение"));
        Assert.assertTrue(sum5.contains("Обнаружены ошибки/предупреждения"));
        sleep();
        logger.info("На странице присутствует блок \"Статус: Неверные данные\"");
        Assert.assertEquals("Неверные данные", chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[5]/div/ul/li/div/div/div/div/div/div[1]/div[3]/p/span")).getText());
        Assert.assertEquals("Ошибки при заполнении", chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[5]/h3")).getText());
        logger.info("Содержание ошибки: Ячейка окпо: Ячейка окпо не прошла проверку по правилу: Длина от 8 до 14 символов. Только числа.");
        Assert.assertEquals("Ячейка окпо: Ячейка окпо не прошла проверку по правилу: Длина от 8 до 14 символов. Только числа.", chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[5]/div/ul/li/div/div/div/div/div/div[2]/div[2]/div/p")).getText());
        Assert.assertEquals("Предупреждение", chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[6]/h3")).getText());
        logger.info("Присутствует блок \"Предупреждение: Ячейка окпо: Длина ОКПО меньше 8! Код дополнен спереди нолями.\"");
        Assert.assertEquals("Ячейка окпо: Длина ОКПО меньше 8! Код дополнен спереди нолями.", chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[6]/div/ul/li/div/div/div/div/div/div[3]/div[2]/div/p")).getText());
        logger.info("Статус формы \"Ошибка данных\",  в Истории самая верхняя запись - \"Ошибка данных\"");
        Assert.assertEquals("Ошибка данных", chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/ul[1]/li[4]/div/span")).getText());
        Assert.assertEquals("Ошибка данных", chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[4]/div[2]/ul/li[1]/div[1]/div[2]/p")).getText());
        logger.info("В поле \"Код по ОКПО\" вводим \"1\" и нажимаем \"Проверить\"");
        sendKeysToInput("//*[@id=\"okpo\"]", "1");
        sleep(300);
        clickButtonJS("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[8]/div/div/div[1]/div[1]/button");
        logger.info("В правом верхнем углу появляется 2 статуса \"Сохранение\", \"Проверка формы Обнаружены ошибки/предупреждения\"");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("ng2-toast > div > div.toast-text")));
        wait.until(ExpectedConditions.numberOfElementsToBe(By.cssSelector("ng2-toast"), 2));
        List<WebElement> popups6 = chromeDriver.findElements(By.cssSelector("ng2-toast"));
        String sum6 = popups6.get(0).getText() + popups6.get(1).getText();
        Assert.assertTrue(sum6.contains("Сохранение"));
        Assert.assertTrue(sum6.contains("Обнаружены ошибки/предупреждения"));
        sleep();
        Assert.assertEquals("Предупреждение", chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[5]/h3")).getText());
        logger.info("На странице присутствует блок \"Предупреждение: Ячейка окпо: Длина ОКПО меньше 8! Код дополнен спереди нолями.\"");
        Assert.assertEquals("Ячейка окпо: Длина ОКПО меньше 8! Код дополнен спереди нолями.", chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[5]/div/ul/li/div/div/div/div/div/div[3]/div[2]/div/p")).getText());
        logger.info("Статус формы \"В работе\",  в Истории самая верхняя запись - \"Обновлен черновик\"");
        Assert.assertEquals("В работе", chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/ul[1]/li[4]/div/span")).getText());
        Assert.assertEquals("Обновлен черновик", chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[4]/div[2]/ul/li[1]/div[1]/div[2]/p")).getText());
        logger.info("Возвращаем корректное значение в поле \"Код по ОКПО\", в поле \"Код по ОКПО структурного подразделения\" вводим \"а\" и нажимаем \"Проверить\"");
        sendKeysToInput("//*[@id=\"okpo\"]", okpo);
        String okpoSub = chromeDriver.findElement(By.xpath("//*[@id=\"okpoSub\"]")).getAttribute("value");
        sendKeysToInput("//*[@id=\"okpoSub\"]", "a");
        clickButtonJS("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[7]/div/div/div[1]/div[1]/button");
        logger.info("В правом верхнем углу появляется 2 статуса \"Сохранение\", \"Проверка формы Обнаружены ошибки/предупреждения\"");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("ng2-toast > div > div.toast-text")));
        wait.until(ExpectedConditions.numberOfElementsToBe(By.cssSelector("ng2-toast"), 2));
        List<WebElement> popups7 = chromeDriver.findElements(By.cssSelector("ng2-toast"));
        String sum7 = popups7.get(0).getText() + popups7.get(1).getText();
        Assert.assertTrue(sum7.contains("Сохранение"));
        Assert.assertTrue(sum7.contains("Обнаружены ошибки/предупреждения"));
        sleep();
        logger.info("На странице присутствует блок \"Статус: Неверные данные\"");
        Assert.assertEquals("Неверные данные", chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[5]/div/ul/li/div/div/div/div/div/div[1]/div[3]/p/span")).getText());
        logger.info("Содержание ошибки: Ячейка окпо структурного подразделения: Ячейка окпо структурного подразделения не прошла проверку по правилу: Длина от 8 до 14 символов. Только числа.");
        Assert.assertEquals("Ошибки при заполнении", chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[5]/h3")).getText());
        Assert.assertEquals("Ячейка окпо структурного подразделения: Ячейка окпо структурного подразделения не прошла проверку по правилу: Длина от 8 до 14 символов. Только числа.", chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[5]/div/ul/li/div/div/div/div/div/div[2]/div[2]/div/p")).getText());
        logger.info("Присутствует блок \"Ячейка : ОКПО (0000000a) не совпал со значением в базе данных (\" + okpoSub + \"). Найденный оператор: \" + userConfig.get(\"operatorFil\") + \".\"");
        Assert.assertEquals("Предупреждение", chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[6]/h3")).getText());
        Assert.assertEquals("Ячейка : ОКПО (0000000a) не совпал со значением в базе данных (" + okpoSub + "). Найденный оператор: " + userConfig.get("operatorFil") + ".", chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[6]/div/ul/li[1]/div/div/div/div/div/div[2]/div[2]/div/p")).getText());
        logger.info("Статус формы \"Ошибка данных\",  в Истории самая верхняя запись - \"Ошибка данных\"");
        Assert.assertEquals("Ошибка данных", chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/ul[1]/li[4]/div/span")).getText());
        Assert.assertEquals("Ошибка данных", chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[4]/div[2]/ul/li[1]/div[1]/div[2]/p")).getText());
        logger.info("В поле \"Код по ОКПО структурного подразделения\" вводим \"1\" и нажимаем \"Проверить\"");
        sendKeysToInput("//*[@id=\"okpoSub\"]", "1");
        clickButtonJS("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[8]/div/div/div[1]/div[1]/button");
        logger.info("В правом верхнем углу появляется 2 статуса \"Сохранение\", \" Проверка формы Обнаружены ошибки/предупреждения\"");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("ng2-toast > div > div.toast-text")));
        wait.until(ExpectedConditions.numberOfElementsToBe(By.cssSelector("ng2-toast"), 2));
        List<WebElement> popups8 = chromeDriver.findElements(By.cssSelector("ng2-toast"));
        String sum8 = popups8.get(0).getText() + popups8.get(1).getText();
        Assert.assertTrue(sum8.contains("Сохранение"));
        Assert.assertTrue(sum8.contains("Обнаружены ошибки/предупреждения"));
        sleep();
        logger.info("На странице присутствует блок \"Ячейка : ОКПО (00000001) не совпал со значением в базе данных (...). Найденный оператор: ...\"");
        Assert.assertEquals("Предупреждение", chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[5]/h3")).getText());
        logger.info("На странице присутствует блок \"Предупреждение: Ячейка окпо: Длина ОКПО структурного подразделения меньше 8! Код дополнен спереди нолями.\"");
        Assert.assertEquals("Ячейка : ОКПО (00000001) не совпал со значением в базе данных (" + okpoSub + "). Найденный оператор: " + userConfig.get("operatorFil") + ".", chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[5]/div/ul/li[1]/div/div/div/div/div/div[2]/div[2]/div/p")).getText());
        Assert.assertEquals("Ячейка окпо структурного подразделения: Длина ОКПО структурного подразделения меньше 8! Код дополнен спереди нолями.", chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[5]/div/ul/li[2]/div/div/div/div/div/div[3]/div[2]/div/p")).getText());
        logger.info("Статус формы \"В работе\",  в Истории самая верхняя запись - \"Обновлен черновик\"");
        Assert.assertEquals("В работе", chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/ul[1]/li[4]/div/span")).getText());
        Assert.assertEquals("Обновлен черновик", chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[4]/div[2]/ul/li[1]/div[1]/div[2]/p")).getText());
        logger.info("Возвращаем корректное значение в поле \"Код по ОКПО структурного подразделения\", в поле \"ОКВЭД\" вводим \"а\" и нажимаем \"Проверить\"");
        sendKeysToInput("//*[@id=\"okpoSub\"]", okpoSub);
        sendKeysToInput("//*[@id=\"okved\"]", "a");
        clickButtonJS("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[7]/div/div/div[1]/div[1]/button");
        logger.info("В правом верхнем углу появляется 2 статуса \"Сохранение\", \"Проверка формы Обнаружены ошибки/предупреждения\"");
        wait.until(ExpectedConditions.numberOfElementsToBe(By.cssSelector("ng2-toast"), 2));
        List<WebElement> popups9 = chromeDriver.findElements(By.cssSelector("ng2-toast"));
        String sum9 = popups9.get(0).getText() + popups9.get(1).getText();
        Assert.assertTrue(sum9.contains("Сохранение"));
        Assert.assertTrue(sum9.contains("Обнаружены ошибки/предупреждения"));
        sleep();
        logger.info("На странице присутствует блок \"Предупреждение: Ячейка оквэд: Внимание, для кода ОКВЭД не выполнено правило \"Числа, разделенные точкой\".\"");
        Assert.assertEquals("Предупреждение", chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[5]/h3")).getText());
        Assert.assertEquals("Ячейка оквэд: Внимание, для кода ОКВЭД не выполнено правило \"Числа, разделенные точкой\".", chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[5]/div/ul/li/div/div/div/div/div/div[3]/div[2]/div/p")).getText());
        logger.info("Статус формы \"В работе\",  в Истории самая верхняя запись - \"Обновлен черновик\"");
        Assert.assertEquals("В работе", chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/ul[1]/li[4]/div/span")).getText());
        Assert.assertEquals("Обновлен черновик", chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[4]/div[2]/ul/li[1]/div[1]/div[2]/p")).getText());
        logger.info("Возвращаем корректное значение в поле \"ОКВЭД\", в поле \"ОКАТО\" вводим \"а\" и нажимаем \"Проверить\"");
        sendKeysToInput("//*[@id=\"okved\"]", userConfig.get("okvedFil"));
        sendKeysToInput("//*[@id=\"okato\"]", "a");
        clickButtonJS("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[7]/div/div/div[1]/div[1]/button");
        logger.info("В правом верхнем углу появляется 2 статуса \"Сохранение\", \"Проверка формы Обнаружены ошибки/предупреждения\"");
        wait.until(ExpectedConditions.numberOfElementsToBe(By.cssSelector("ng2-toast"), 2));
        List<WebElement> popups10 = chromeDriver.findElements(By.cssSelector("ng2-toast"));
        String sum10 = popups10.get(0).getText() + popups10.get(1).getText();
        Assert.assertTrue(sum10.contains("Сохранение"));
        Assert.assertTrue(sum10.contains("Обнаружены ошибки/предупреждения"));
        sleep();
        logger.info("На странице присутствует блок \"Статус: Неверные данные\", \"Содержание ошибки: Ячейка окато: Ячейка окато не прошла проверку по правилу: Код ОКАТО. Только числа. В случае отсутствия ОКАТО вводим 9999999999\"");
        Assert.assertEquals("Неверные данные", chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[5]/div/ul/li/div/div/div/div/div/div[1]/div[3]/p/span")).getText());
        Assert.assertEquals("Ошибки при заполнении", chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[5]/h3")).getText());
        Assert.assertEquals("Ячейка окато: Ячейка окато не прошла проверку по правилу: Код ОКАТО. Только числа. В случае отсутствия ОКАТО ввести 9999999999", chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[5]/div/ul/li/div/div/div/div/div/div[2]/div[2]/div/p")).getText());
        logger.info("Статус формы \"Ошибка данных\",  в Истории самая верхняя запись - \"Ошибка данных\"");
        Assert.assertEquals("Ошибка данных", chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/ul[1]/li[4]/div/span")).getText());
        Assert.assertEquals("Ошибка данных", chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[4]/div[2]/ul/li[1]/div[1]/div[2]/p")).getText());
        logger.info("Возвращаем корректное значение в поле \"ОКАТО\", в поле \"ОГРН\" вводим \"а\" и нажимаем \"Проверить\"");
        sendKeysToInput("//*[@id=\"okato\"]", userConfig.get("okatoFil"));
        sendKeysToInput("//*[@id=\"ogrn\"]", "a");
        clickButtonJS("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[7]/div/div/div[1]/div[1]/button");
        logger.info("В правом верхнем углу появляется 2 статуса \"Сохранение\", \"Проверка формы Обнаружены ошибки/предупреждения\"");
        wait.until(ExpectedConditions.numberOfElementsToBe(By.cssSelector("ng2-toast"), 2));
        List<WebElement> popups11 = chromeDriver.findElements(By.cssSelector("ng2-toast"));
        String sum11 = popups11.get(0).getText() + popups11.get(1).getText();
        Assert.assertTrue(sum11.contains("Сохранение"));
        Assert.assertTrue(sum11.contains("Обнаружены ошибки/предупреждения"));
        sleep();
        logger.info("На странице присутствует блок \"Статус: Неверные данные\", \"Содержание ошибки: Ячейка огрн: Ячейка огрн не прошла проверку по правилу: Код ОГРН. Только числа. 13 или 15 знаков. Если ОГРН не известен, вводим 9999999999999.\"");
        Assert.assertEquals("Неверные данные", chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[5]/div/ul/li/div/div/div/div/div/div[1]/div[3]/p/span")).getText());
        Assert.assertEquals("Ошибки при заполнении", chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[5]/h3")).getText());
        Assert.assertEquals("Ячейка огрн: Ячейка огрн не прошла проверку по правилу: Код ОГРН. Только числа. 13 или 15 знаков. Если ОГРН не известен, ввести 9999999999999.", chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[5]/div/ul/li/div/div/div/div/div/div[2]/div[2]/div/p")).getText());
        logger.info("Статус формы \"Ошибка данных\",  в Истории самая верхняя запись - \"Ошибка данных\"");
        Assert.assertEquals("Ошибка данных", chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/ul[1]/li[4]/div/span")).getText());
        Assert.assertEquals("Ошибка данных", chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[4]/div[2]/ul/li[1]/div[1]/div[2]/p")).getText());
        logger.info("Возвращаем корректное значение в поле \"ОГРН\", в поле \"ОКТМО\" вводим \"а\" и нажимаем \"Проверить\"");
        sendKeysToInput("//*[@id=\"ogrn\"]", ogrn);
        sendKeysToInput("//*[@id=\"oktmo\"]", "a");
        clickButtonJS("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[8]/div/div/div[1]/div[1]/button");
        logger.info("В правом верхнем углу появляется 2 статуса \"Сохранение\", \"Проверка формы Обнаружены ошибки/предупреждения\"");
        wait.until(ExpectedConditions.numberOfElementsToBe(By.cssSelector("ng2-toast"), 2));
        List<WebElement> popups12 = chromeDriver.findElements(By.cssSelector("ng2-toast"));
        String sum12 = popups12.get(0).getText() + popups12.get(1).getText();
        Assert.assertTrue(sum12.contains("Сохранение"));
        Assert.assertTrue(sum12.contains("Обнаружены ошибки/предупреждения"));
        sleep();
        logger.info("На странице присутствует блок \"Предупреждение: Ячейка октмо: Внимание, для кода ОКТМО правило \"Длина 8 или 11 символов. Только числа.\" не выполнено.\"");
        Assert.assertEquals("Предупреждение", chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[5]/h3")).getText());
        Assert.assertEquals("Ячейка октмо: Внимание, для кода ОКТМО правило \"Длина 8 или 11 символов. Только числа.\" не выполнено.", chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[3]/div[5]/div/ul/li/div/div/div/div/div/div[3]/div[2]/div/p")).getText());
        logger.info("Статус формы \"В работе\",  в Истории самая верхняя запись - \"Обновлен черновик\"");
        Assert.assertEquals("Обновлен черновик", chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[4]/div[2]/ul/li[1]/div[1]/div[2]/p")).getText());
        Assert.assertEquals("В работе", chromeDriver.findElement(By.xpath("//*[@id=\"wrapper\"]/ng-component/ng-component/div[1]/ul[1]/li[4]/div/span")).getText());


    }


    @AfterClass(groups = {"smoke", "regress"})
    public void closeDriver() {
        if (chromeDriver != null) {
            chromeDriver.close();
            chromeDriver.quit();
        }
        clearHistory();

    }

    public void clickButton(String buttonXPath) {
        WebElement button = null;
        click:
        while (true) {
            try {
                button = chromeDriver.findElementByXPath(buttonXPath);
                Actions actions = new Actions(chromeDriver);
                actions.moveToElement(button);
                actions.perform();
                button = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(buttonXPath)));
                button.click();
                chromeDriver.manage().timeouts().implicitlyWait(300, TimeUnit.MILLISECONDS);
                break click;
            } catch (WebDriverException e) {
                try {
                    button.click();
                    chromeDriver.manage().timeouts().implicitlyWait(300, TimeUnit.MILLISECONDS);
                    break click;
                } catch (WebDriverException e1) {
                    e.printStackTrace();
                    continue click;
                }

            }
        }

    }

    public void clickButton(WebElement button) {

        click:
        while (true) {
            try {

                Actions actions = new Actions(chromeDriver);
                actions.moveToElement(button);
                actions.perform();
                button.click();
                chromeDriver.manage().timeouts().implicitlyWait(300, TimeUnit.MILLISECONDS);
                break click;
            } catch (WebDriverException e) {
                try {
                    button.click();
                    chromeDriver.manage().timeouts().implicitlyWait(300, TimeUnit.MILLISECONDS);
                    break click;
                } catch (WebDriverException e1) {
                    e.printStackTrace();
                    continue click;
                }

            }
        }

    }

    public void clickButtonCSS(String CSS) {
        WebElement button = null;
        boolean staleElement = true;
        click:
        while (staleElement) {
            try {
                button = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(CSS)));
                String scrollElementIntoMiddle = "var viewPortHeight = Math.max(document.documentElement.clientHeight, window.innerHeight || 0);"
                        + "var elementTop = arguments[0].getBoundingClientRect().top;"
                        + "window.scrollBy(0, elementTop-(viewPortHeight/2));";

                ((JavascriptExecutor) chromeDriver).executeScript(scrollElementIntoMiddle, button);
                button.click();
                chromeDriver.manage().timeouts().implicitlyWait(1000, TimeUnit.MILLISECONDS);
                staleElement = false;
            } catch (Exception e) {
                e.printStackTrace();
                staleElement = true;


            }
        }
    }

    void clickButtonJS(String xpath) {
        WebElement element = chromeDriver.findElement(By.xpath(xpath));
        JavascriptExecutor executor = (JavascriptExecutor) chromeDriver;
        executor.executeScript("arguments[0].click();", element);
    }

    void clickButtonJS(WebElement webElement) {
        JavascriptExecutor executor = (JavascriptExecutor) chromeDriver;
        executor.executeScript("arguments[0].click();", webElement);
    }

    public void sendKeysToInput(String inputXPath, String keys) {
        circle:
        while (true) {
            WebElement input = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(inputXPath)));
            String scrollElementIntoMiddle = "var viewPortHeight = Math.max(document.documentElement.clientHeight, window.innerHeight || 0);"
                    + "var elementTop = arguments[0].getBoundingClientRect().top;"
                    + "window.scrollBy(0, elementTop-(viewPortHeight/2));";

            ((JavascriptExecutor) chromeDriver).executeScript(scrollElementIntoMiddle, input);
            input.clear();
            input.sendKeys(keys);
            chromeDriver.manage().timeouts().implicitlyWait(500, TimeUnit.MILLISECONDS);
            if (input.getAttribute("value").equals(keys)) {
                break circle;
            } else {
                continue circle;
            }
        }

    }

    public void clearInput(String inputXPath) {
        WebElement input = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(inputXPath)));
        String scrollElementIntoMiddle = "var viewPortHeight = Math.max(document.documentElement.clientHeight, window.innerHeight || 0);"
                + "var elementTop = arguments[0].getBoundingClientRect().top;"
                + "window.scrollBy(0, elementTop-(viewPortHeight/2));";

        ((JavascriptExecutor) chromeDriver).executeScript(scrollElementIntoMiddle, input);
        input.clear();
        input.sendKeys(" ");
        input.sendKeys(Keys.BACK_SPACE);
        input.sendKeys(Keys.ENTER);


    }

    public void scrollTo(WebElement webElement) {
        String scrollElementIntoMiddle = "var viewPortHeight = Math.max(document.documentElement.clientHeight, window.innerHeight || 0);"
                + "var elementTop = arguments[0].getBoundingClientRect().top;"
                + "window.scrollBy(0, elementTop-(viewPortHeight/2));";

        ((JavascriptExecutor) chromeDriver).executeScript(scrollElementIntoMiddle, webElement);
    }

    void typeIntoFileInput(File file) {
        StringSelection s = new StringSelection(file.getAbsolutePath());
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(s, null);
        Robot robot = null;
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }

        robot.keyPress(java.awt.event.KeyEvent.VK_CONTROL);
        robot.keyPress(java.awt.event.KeyEvent.VK_V);
        chromeDriver.manage().timeouts().implicitlyWait(1000, TimeUnit.MILLISECONDS);

        robot.keyRelease(java.awt.event.KeyEvent.VK_V);
        robot.keyRelease(java.awt.event.KeyEvent.VK_CONTROL);
        robot.keyPress(java.awt.event.KeyEvent.VK_ENTER);
        robot.keyRelease(java.awt.event.KeyEvent.VK_ENTER);
    }

    File lastModified(String directory) {
        File files[] = new File(directory).listFiles();
        long max = Long.MIN_VALUE;
        File res = null;
        for (File file : files) {
            if (file.lastModified() > max) {
                res = file;
                max = file.lastModified();
            }
        }
        return res;
    }
    public void  clearHistory(){
        SqlManager sqlManager = new SqlManager(databaseConfig.get("url"), databaseConfig.get("name"), databaseConfig.get("password"));
        sqlManager.deleteFinallyOperatorHistory(userConfig.get("mail"));
        sqlManager.deleteUser(userConfig.get("mail"));

    }
    public boolean checkDates(Date parsedDate) {
        long difference = Math.abs(parsedDate.getTime() - lastDate.getTime());
        long maxDelay = Long.parseLong(config.get("maxDelay"));
        if (difference < maxDelay) {
            return true;
        }
        return false;

    }

    boolean isActive(String xpath) {
        WebElement webElement = chromeDriver.findElement(By.xpath(xpath));
        String scrollElementIntoMiddle = "var viewPortHeight = Math.max(document.documentElement.clientHeight, window.innerHeight || 0);"
                + "var elementTop = arguments[0].getBoundingClientRect().top;"
                + "window.scrollBy(0, elementTop-(viewPortHeight/2));";

        ((JavascriptExecutor) chromeDriver).executeScript(scrollElementIntoMiddle, webElement);
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return webElement.isEnabled();
    }

    void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    void sleep() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private  static String replaceMonth(String textMonth) throws Exception {
        for(String month:months.keySet()){
            if(textMonth.contains(month)){
                textMonth.replace(month,months.get(month));
                return textMonth;
            }
        }
        throw new Exception("Не удалось обработать текстовое представление даты");
    }
    private String getCurrentMonth() throws Exception {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM");
        String monthNum = simpleDateFormat.format(lastDate);
        for (String key : months.keySet()) {
            if (months.get(key).equals(monthNum)) {
                return key;
            }
        }
        throw new Exception("Не удалось распознать месяц");
    }
    int greyBlocksCounter(){
        SimpleDateFormat simpleDateFormat= new SimpleDateFormat("MM");
        String month=simpleDateFormat.format(lastDate);
        return (0);
    }


}
