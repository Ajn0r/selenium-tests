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

public class FacebookPostTest {

    private WebDriver driver;
    private String message;
    private String title;
    private WebDriverWait wait;
    private Logger logger = LoggerFactory.getLogger(FacebookPostTest.class);
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
        // Set max size to screen to make sure either "Create" or "Skapa" is there instead of "Menu".
        Dimension dimension = new Dimension(1024, 900);
        driver.manage().window().setSize(dimension);

    }
    @Test
    public void testPostSomething() throws InterruptedException {
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

        // Click to create a new post
        try {
            WebElement createPostBtn = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div[aria-label*='Skapa'], div[aria-label*='Create']")));
            createPostBtn.click();
        } catch (Exception e) {
            logger.error("Could not find or click on the create element", e);
        }
        // Click on "inlägg" to see textbox
        try {
            WebElement inlaggButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[contains(text(), 'Inlägg') or contains(text(), 'Post')]")));
            inlaggButton.click();
        } catch (ElementNotVisibleException e) {
            logger.error("Could not find the post element", e);
        }
        catch (Exception e) {
            logger.error("Something went wrong when trying to create new post", e);
        }

        // Wait for the element to be clickable
        try {
            WebElement textBox = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("div[aria-label*='Vad'], div[aria-label*='What']")));
            //The message/post to be published
            message = "This is my test post using Selenium WebDriver";
            // Send text to the element
            textBox.sendKeys(message);
        } catch (InvalidSelectorException e) {
            logger.error("Could not find textarea, the selector seems to be incorrect", e);
        } catch (Exception e) {
            logger.error("Something went wrong when trying write the post", e);
        }

        //Click to publish, finds the element that contains "Publ" for both Swedish and English
        try {
            WebElement publish = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[contains(@aria-label, 'Publicera') or contains(@aria-label, 'Post')]")));
            publish.click();
        } catch (Exception e) {
            logger.error("Something went wrong when trying to publish the post", e);
        }
        // Wait 2 sec before getting the profile page
        Thread.sleep(2000);

        // Navigate to profile page
        driver.get("https://www.facebook.com/profile.php");

        // Verify that the post is successfully published and displayed on the user's timeline
        try {
            WebElement postText = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(text(), message)]")));
            Assert.assertTrue(postText.isDisplayed());
            logger.info("Status update is successfully posted on the user's timeline.");
        } catch (Exception e) {
            logger.error("Could not find the post", e);
        }

    }
    @AfterTest
    public void teardown() throws InterruptedException {
        //Remove post once test is finished
        try {
            // Removing post after 3 sec, only possible if the facebook page is Swedish
            Thread.sleep(3000);
            WebElement postText = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(text(), message)]")));
            // Find and click on the options/three dots
            WebElement threeDots = driver.findElement(By.cssSelector("div[aria-label*='Åtgärder'], div[aria-label*='Actions']"));
            threeDots.click();
            logger.info("Found post");
            // Locate the element with the text "Flytta till papperskorgen" or "Move to trash"
            WebElement flyttaTillPapperskorgen = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[text()='Flytta till papperskorg' or text()='Move to trash']")));
            // Click on the element with the text "Flytta till papperskorgen" or "Move to trash"
            flyttaTillPapperskorgen.click();
            logger.info("Removing it...");
            // Click the remove button
            WebElement remove = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[text()='Flytta' or text()='Move']")));
            remove.click();
            logger.info("Post removed.");

        } catch (ElementNotVisibleException e) {
            // If the post was not found
            logger.error("Could not find post", e);
        }
        catch (Exception e) {
            // If the post could not be removed.
            logger.error("Could not remove post, needs to be done manually", e);
        }
        // Closes the WebDriver
        if (driver != null) {
            driver.quit();
        }
    }
}
