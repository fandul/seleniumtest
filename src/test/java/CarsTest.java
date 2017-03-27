
import java.util.List;
import java.util.concurrent.TimeUnit;
import static org.hamcrest.CoreMatchers.is;
import org.junit.AfterClass;
import org.junit.Assert;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 *
 * @author Rihards
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CarsTest {

    private static final int WAIT_MAX = 6;
    static WebDriver driver;

    @BeforeClass
    public static void setup() {

        System.setProperty("webdriver.gecko.driver", "C:\\Users\\Rihards\\Documents\\drivers\\geckodriver-v0.14.0-win64\\geckodriver.exe");
        System.setProperty("webdriver.chrome.driver", "C:\\Users\\Rihards\\Documents\\drivers\\chromedriver_win32\\chromedriver.exe");

        //Reset Database
        com.jayway.restassured.RestAssured.given().get("http://localhost:3000/reset");
        driver = new ChromeDriver();
        driver.get("http://localhost:3000");
    }

    @AfterClass
    public static void tearDown() {
        driver.quit();
        //Reset Database 
        com.jayway.restassured.RestAssured.given().get("http://localhost:3000/reset");
    }

    @Test
    //Verify that page is loaded and all expected data are visible
    public void test1() throws Exception {
        (new WebDriverWait(driver, WAIT_MAX)).until((ExpectedCondition<Boolean>) (WebDriver d) -> {
            WebElement e = d.findElement(By.tagName("tbody"));
            List<WebElement> rows = e.findElements(By.tagName("tr"));
            Assert.assertThat(rows.size(), is(5));
            return true;
        });
    }

    @Test
    //Verify the filter functionality 
    public void test2() throws Exception {
        //No need to WAIT, since we are running test in a fixed order, we know the DOM is ready (because of the wait in test1)
        WebElement element = driver.findElement(By.id("filter"));
        element.clear();
        element.sendKeys("2002");
        WebElement table = driver.findElement(By.tagName("tbody"));
        List<WebElement> rows = table.findElements(By.tagName("tr"));
        Assert.assertThat(rows.size(), is(2));
    }

    @Test
    // Clear the text in the filter text and verify that we have the original five rows
    public void test3() throws Exception {
        WebElement element = driver.findElement(By.id("filter"));
        element.sendKeys(Keys.BACK_SPACE);
        WebElement table = driver.findElement(By.tagName("tbody"));
        List<WebElement> rows = table.findElements(By.tagName("tr"));
        Assert.assertThat(rows.size(), is(5));
    }

    @Test
    //Click the sort “button” for Year, and verify that the top row contains the car with id 938 and the last row the car with id = 940.
    public void test4() throws Exception {
        WebElement sort = driver.findElement(By.id("h_year"));
        sort.click();
        WebElement table = driver.findElement(By.tagName("tbody"));
        List<WebElement> sortedRows = table.findElements(By.tagName("tr"));
        String firstRow = sortedRows.get(0).findElements(By.tagName("td")).get(0).getText();
        String lastRow = sortedRows.get(4).findElements(By.tagName("td")).get(0).getText();
        Assert.assertThat(firstRow, is("938"));
        Assert.assertThat(lastRow, is("940"));
    }

    @Test
    /*Press the edit button for the car with the id 938. Change the Description to "Cool car", and save changes.
    Verify that the row for car with id 938 now contains "Cool car" in the Description column */
    public void test5() throws Exception {

        WebElement editButton = null;
        driver.get("http://localhost:3000");
        WebDriverWait wait = new WebDriverWait(driver, WAIT_MAX);
        wait.until(ExpectedConditions
                .visibilityOfElementLocated((By.tagName("thead"))));
        driver.switchTo().parentFrame();

        WebElement table = driver.findElement(By.tagName("tbody"));
        List<WebElement> rows = table.findElements(By.tagName("tr"));

        //loop through rows to find car with id "938"
        for (int i = 0; i < rows.size(); i++) {
            if (rows.get(i).findElements(By.tagName("td")).get(0).getText().equalsIgnoreCase("938")) {
                editButton = rows.get(i);
                break;
            }
        }

        editButton = editButton.findElements(By.tagName("td")).get(7).findElements(By.tagName("a")).get(0);
        //click the edit button
        editButton.click();

//        WebElement description = driver.findElement(By.id("description"));
//        description.sendKeys(Keys.BACK_SPACE); apparently this doesnt WORK 
        //clear description field
        driver.findElement(By.id("description")).clear();
        wait.until(ExpectedConditions.visibilityOfElementLocated((By.id("description"))));

        //edit description text field
        driver.findElement(By.id("description")).sendKeys("cool cars");
        //click save button
        driver.findElement(By.id("save")).click();

        (new WebDriverWait(driver, WAIT_MAX)).until((ExpectedCondition<Boolean>) (WebDriver d) -> {
            WebElement updatedTable = d.findElement(By.tagName("tbody"));
            List<WebElement> updatedRows = updatedTable.findElements(By.tagName("tr"));

            String editedRow = null;
            //loop through rows to find car with id "938"
            for (int i = 0; i < updatedRows.size(); i++) {
                if (updatedRows.get(i).findElements(By.tagName("td")).get(0).getText().equalsIgnoreCase("938")) {
                    editedRow = updatedRows.get(i).findElements(By.tagName("td")).get(5).getText();
                    break;
                }
            }
            assertThat(editedRow, is("cool cars"));
            return true;
        });
    }

    @Test
    /*Click the new “Car Button”, and click the “Save Car” button. 
     Verify that we have an error message with the text “All fields are required”
    and we still only have five rows in the all cars table.
     */
    public void test6() throws Exception {
        driver.findElement(By.id("new")).click();
        driver.findElement(By.id("save")).click();

        (new WebDriverWait(driver, WAIT_MAX)).until((ExpectedCondition<Boolean>) (WebDriver d) -> {
            String result = d.findElement(By.id("submiterr")).getText();
            Assert.assertThat(result, is("All fields are required"));
            return true;
        });
    }

    @Test
    //Click the new Car Button, and add the following values for a new car
    public void test7() throws Exception {
        WebDriverWait wait = new WebDriverWait(driver, WAIT_MAX);
        wait.until(ExpectedConditions.visibilityOfElementLocated((By.id("new"))));

        driver.findElement(By.id("new")).click();
        driver.findElement(By.id("year")).sendKeys("2008");
        driver.findElement(By.id("registered")).sendKeys("2002-5-5");
        driver.findElement(By.id("make")).sendKeys("Kia");
        driver.findElement(By.id("model")).sendKeys("Rio");
        driver.findElement(By.id("description")).sendKeys("As new");
        driver.findElement(By.id("price")).sendKeys("31000");

        //save the new car
        driver.findElement(By.id("save")).click();

        (new WebDriverWait(driver, WAIT_MAX)).until((ExpectedCondition<Boolean>) (WebDriver d) -> {
            WebElement updatedTable = d.findElement(By.tagName("tbody"));
            List<WebElement> updatedRows = updatedTable.findElements(By.tagName("tr"));
            //row count should be 6
            assertThat(updatedRows.size(), is(6));

            //new car should be on row 6, check year
            assertThat(updatedRows.get(5).findElements(By.tagName("td")).get(1).getText(), is("2008"));
            //model is Rio
            assertThat(updatedRows.get(5).findElements(By.tagName("td")).get(4).getText(), is("Rio"));

            return true;
        });
    }
    
}
