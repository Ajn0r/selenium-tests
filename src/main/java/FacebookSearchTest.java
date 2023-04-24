import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openqa.selenium.*;
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


public class FacebookSearchTest {

    private WebDriver driver;
    private WebDriverWait wait;
    private String title;
    private Logger logger = LoggerFactory.getLogger(FacebookSearchTest.class);
    static {
        System.setProperty("logback.configurationFile", "src/main/resources/logback.xml");
    }
    @BeforeTest
    public void setup() {
        // Set the path of the ChromeDriver executable file
        System.setProperty("webdriver.chrome.driver", "C:\\Users\\ÄGARE\\Downloads\\chromedriver.exe");
        // Set the path of the ChromeDriver executable file for AWS server
        //System.setProperty("webdriver.chrome.driver", "C:\\Users\\Administrator\\Downloads\\chromedriver.exe");
        // Create an instance of ChromeOptions and add the desired option
        ChromeOptions options = new ChromeOptions();
        // Add the "--remote-allow-origins=" argument to allow requests from any origin
        options.addArguments("--remote-allow-origins=*");
        // Add the "--disable-notifications" argument to disable notifications in Chrome
        options.addArguments("--disable-notifications");
        // Create an instance of ChromeDriver with the options
        driver = new ChromeDriver(options);
        // Create an instance of WebDriverWait to explicitly wait for elements to load
        wait = new WebDriverWait(driver, 10);
    }
    @Test
    public void testSearchFacebookPage() throws InterruptedException {
        // Navigate to Facebook login page
        driver.get("https://www.facebook.com/");

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

        // Enter email or phone and password
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
            WebElement emailOrPhone = driver.findElement(By.id("email"));
            WebElement passwordInput = driver.findElement(By.id("pass"));
            emailOrPhone.sendKeys(email);
            passwordInput.sendKeys(password);
        } catch (Exception e) {
            logger.error("An error occurred while trying to insert login credentials", e);
        }

        try {
            // Click the login btn
            driver.findElement(By.name("login")).click();
            // Get the title
            title = driver.getTitle();
            // Log that login is successful if the title does not contain "login"
            if (!title.toLowerCase().contains("logga in") && !title.toLowerCase().contains("login")) {
                logger.info("Successfully logged in");
            }
        } catch (Exception e) {
            logger.error("An error occurred when trying to log in", e);
        }

        try {
            // Locate and click the search box
            WebElement searchBox = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@placeholder='Sök på Facebook' or @placeholder='Search Facebook']")));
            searchBox.click();
            // Enter search query and submit search
            searchBox.sendKeys("Cats of facebook");
            searchBox.sendKeys(Keys.RETURN);
            logger.info("Input search keys successfully");
        } catch (Exception e) {
            // Log any errors that occur during the search test
            logger.error("Error occurred when trying search", e);
        }

        try {
            // Wait for search results to load
            wait.until(ExpectedConditions.textToBePresentInElementLocated(By.xpath("//div[@aria-label='Sökresultat' or @aria-label='Search results']"), "Cats of Facebook"));
            // Retrieve search results and verify they contain the expected text
            WebElement searchResultsDiv = driver.findElement(By.xpath("//div[@aria-label='Sökresultat' or @aria-label='Search results']"));
            String searchResultsText = searchResultsDiv.getText().toLowerCase();
            Assert.assertTrue(searchResultsText.contains("cats of facebook"));
            // Log successful search result retrieval
            logger.info("Search results for 'Cats of facebook' found successfully");
        } catch (Exception e) {
            logger.error("Error occurred when validating search result", e);
        }
    }
    @AfterTest
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
