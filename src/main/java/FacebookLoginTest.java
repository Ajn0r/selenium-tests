import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

public class FacebookLoginTest {
    private WebDriver driver;
    private WebDriverWait wait;
    private static final Logger logger = LoggerFactory.getLogger(FacebookLoginTest.class);

    static {
        System.setProperty("logback.configurationFile", "src/main/resources/logback.xml");
    }

    private String title;

    @BeforeTest
    public void setup() {
        // Set the path of the ChromeDriver executable file for local machine
        System.setProperty("webdriver.chrome.driver", "C:\\Users\\ÄGARE\\Downloads\\chromedriver.exe");
        // Set the path of the ChromeDriver executable file for AWS server
        //System.setProperty("webdriver.chrome.driver", "C:\\Users\\Administrator\\Downloads\\chromedriver.exe");

        // Create an instance of ChromeOptions and add the desired option
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--disable-notifications");
        // Create an instance of ChromeDriver with the options
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, 10);
    }

    @Test
    public void testNavigateToProfilePage() throws InterruptedException {

        // Navigate to Facebook login page
        driver.get("https://www.facebook.com/");
        // If there is a cookie box displayed

        // If there is a cookie box displayed
        try {
            // Locate and retrieve the cookie dialog element
            WebElement cookieDialog = driver.findElement(By.cssSelector("div[data-testid='cookie-policy-manage-dialog']"));

            // Check if the cookie dialog is displayed
            if (cookieDialog.isDisplayed()) {
                logger.info("Cookies dialog displayed");
                // Find and click on the accept button within the dialog
                cookieDialog.findElement(By.cssSelector("[data-testid='cookie-policy-manage-dialog-accept-button']")).click();
                logger.info("Cookies accepted");
            }
        } catch (NoSuchElementException ex) {
            logger.info("No cookies to accept", ex);
        } catch (Exception e) {
            // If the cookie dialog is not displayed or there is an exception, log a message
            logger.info("Something went wrong when trying to accept cookies", e);
        }

        // Code to retrieve login credentials
        File jsonFile = new File("C:\\temp\\facebook.json.txt");

        String email = null;
        String password = null;

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(jsonFile);

            email = jsonNode.get("facebookCredentials").get("email").asText();
            password = jsonNode.get("facebookCredentials").get("password").asText();

            System.out.println("Email: " + email);
            System.out.println("Password: " + password);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Enter email or phone and password
        try {
            Thread.sleep(1500);
            WebElement emailOrPhone = driver.findElement(By.id("email"));
            WebElement passwordInput = driver.findElement(By.id("pass"));
            emailOrPhone.sendKeys(email);
            passwordInput.sendKeys(password);
            // Click the login btn
            driver.findElement(By.name("login")).click();
        } catch (Exception e) {
            logger.error("An error occurred while trying to insert login credentials", e);
        }
        Thread.sleep(2000);
        try {
            // Get the title
            title = driver.getTitle();
            // Log that login is successful if the title does not contain "login"
            if (!title.toLowerCase().contains("logga in") && !title.toLowerCase().contains("login")) {
                logger.info("Successfully logged in");
            }
        } catch (Exception e) {
            logger.error("An error occurred when trying to log in", e);
        }
        // Test to verify that the title does not contain "login"
        Assert.assertFalse(title.toLowerCase().contains("logga in") || title.toLowerCase().contains("login"));

        // Navigate to profile page
        try {
            driver.get("https://www.facebook.com/profile.php");
            //Wait until page is finished loading
            wait.until(ExpectedConditions.titleContains("Maja Majsson"));
            // Verify that the profile page is displayed
            Assert.assertTrue(driver.getTitle().contains("Maja Majsson"));
            logger.info("Successfully loaded profile page");
        } catch (Exception e) {
            logger.error("Could not load profile page");
        }

        // Click on the account dropdown
        try {
            WebElement profilePic = driver.findElement(By.xpath("//*[@aria-label='Your profile']"));
            profilePic.click();
        } catch (Exception e) {
            logger.error("An error occurred while clicking the account dropdown", e);
        }

        // Click on the log-out button
        try {
            // Find the logout button element
            WebElement logOutBtn = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[contains(text(), 'Log')]")));
            logOutBtn.click();
            logger.info("Successfully clicked log out button");
        } catch (Exception e) {
            logger.error("An error occurred while trying to log out", e);
        }

        Thread.sleep(3000);
        // Check if user is redirected to login page
        try {
            // Get the title of the page
            title = driver.getTitle();
            // if the title contains the text login, the user is successfully redirected to login page
            if (title.toLowerCase().contains("logga in") || title.toLowerCase().contains("login")) {
                logger.info("Successfully redirected to login page");
            }
            else
                logger.info("Was not redirected to login page");
        } catch (Exception e) {
            logger.error("Was not redirected to login page", e);
        }
    }

    @AfterTest
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
