import com.jayway.restassured.RestAssured;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;


/**
 * Created by ms on 23-03-17.
 */
//Naming standards: http://osherove.com/blog/2005/4/3/naming-standards-for-unit-tests.html

//Question LAM: The statement 'You should never write tests that need to be executed in a specified order. That's  really bad practice. Every test should be able to run independent'.
//The suggested specified order in this assignment, is that ok when we're doing Senario testing?

public class UITest {

    private static WebDriver driver;
    private int timeout = 10;

    @BeforeClass
    public static void setUpClass() {
        System.setProperty("webdriver.chrome.driver", "/home/ms/seleniumDrivers/chromedriver");
        RestAssured.given().get("http://localhost:3000/reset");
        driver = new ChromeDriver();
        driver.get("http://localhost:3000/");
    }

    @AfterClass
    public static void tearDown() {
        driver.quit();
    }

    @After
    public void tearDownEach() {
        driver.navigate().refresh();
        resetDatabase();
    }

    private void resetDatabase() {
        RestAssured.given().get("http://localhost:3000/reset");
    }

    @Test
    public void load_dataIsLoaded_returnsFiveRows() {
        //Don't know why, the after fixture ain't working for this test case. Need to call refresh again.
        driver.navigate().refresh();

        (new WebDriverWait(driver, timeout)).until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver d) {
                List<WebElement> tableData = d.findElements(By.id("tbodycars tr"));
                MatcherAssert.assertThat(tableData.size(), Matchers.equalTo(5));
                return true;
            }
        });
    }

    @Test
    public void filter_filterByText_returnsTwoRows() {

        WebElement filterField = driver.findElement(By.id("filter"));
        filterField.sendKeys("2002");

        (new WebDriverWait(driver, timeout)).until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver d) {
                List<WebElement> tableData = d.findElements(By.id("tbodycars tr"));
                MatcherAssert.assertThat(tableData.size(), Matchers.is(2));
                driver.findElement(By.id("filter")).sendKeys("\b\b\b\b");
                return tableData.size() == 2;
            }
        });
    }

    @Test
    public void clearFilter_removeTextInFilter_returnsFiveRows() {
        WebElement filterField = driver.findElement(By.id("filter"));
        filterField.sendKeys("2002");

        (new WebDriverWait(driver, timeout)).until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver d) {
                List<WebElement> tableData = d.findElements(By.id("tbodycars tr"));
                MatcherAssert.assertThat(tableData.size(), Matchers.is(2));
                driver.findElement(By.id("filter")).sendKeys("\b\b\b\b");
                (new WebDriverWait(driver, timeout)).until(new ExpectedCondition<Boolean>() {
                    public Boolean apply(WebDriver d) {

                        List<WebElement> tableData = d.findElements(By.id("tbodycars tr"));
                        MatcherAssert.assertThat(tableData.size(), Matchers.is(5));
                        return true;
                    }
                });

                return true;
            }
        });
    }

    @Test
    public void sort_changeYearOnCars_expectedIdTopRow938LastRow940() {
        WebElement filterAction = driver.findElement(By.id("h_year"));
        filterAction.click();

        (new WebDriverWait(driver, timeout)).until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver d) {
                String firstRowId = d.findElement(By.cssSelector("#tbodycars > tr:nth-child(1) > td:nth-child(1)")).getText();
                MatcherAssert.assertThat(firstRowId, Matchers.is("938"));

                String lastRowId = d.findElement(By.cssSelector("#tbodycars > tr:nth-child(5) > td:nth-child(1)")).getText();
                MatcherAssert.assertThat(lastRowId, Matchers.is("940"));

                return true;
            }
        });
    }

    @Test
    public void editCar_changeDescriptionForCar938_expectedDescriptionCoolCar() {

        WebElement editBtn = driver.findElement(By.cssSelector("#tbodycars > tr:nth-child(2) > td:nth-child(8) > a:nth-child(1)"));
        editBtn.click();

        (new WebDriverWait(driver, timeout)).until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver d) {

                WebElement descriptionField = driver.findElement(By.id("description"));
                descriptionField.clear();
                descriptionField.sendKeys("Cool car");

                WebElement acceptBtn = driver.findElement(By.id("save"));
                acceptBtn.click();

                (new WebDriverWait(driver, timeout)).until(new ExpectedCondition<Boolean>() {
                    public Boolean apply(WebDriver d) {

                        String selectedRow = d.findElement(By.cssSelector("#tbodycars > tr:nth-child(2) > td:nth-child(6)")).getText();
                        MatcherAssert.assertThat(selectedRow, Matchers.is("Cool car"));


                        return true;
                    }
                });

                return true;
            }
        });

    }

    @Test
    public void createCar_notAllValuesGiven_exceptionThrown() {

        WebElement makeField = driver.findElement(By.id("make"));
        makeField.sendKeys("Voldsom volvo");

        WebElement createBtn = driver.findElement(By.id("save"));
        createBtn.click();

        (new WebDriverWait(driver, timeout)).until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver d) {

                String errorMsg = d.findElement(By.id("submiterr")).getText();
                MatcherAssert.assertThat(errorMsg, Matchers.is("All fields are required"));

                return true;
            }
        });
    }

    @Test
    public void createCar_allValuesGiven_returnsNewRow() {

        WebElement yearField = driver.findElement(By.id("year"));
        WebElement registeredField = driver.findElement(By.id("registered"));
        WebElement makeField = driver.findElement(By.id("make"));
        WebElement modelField = driver.findElement(By.id("model"));
        WebElement descriptionField = driver.findElement(By.id("description"));
        WebElement priceField = driver.findElement(By.id("price"));
        yearField.sendKeys("2008");
        registeredField.sendKeys("2002-5-5");
        makeField.sendKeys("Kia");
        modelField.sendKeys("Rio");
        descriptionField.sendKeys("As new");
        priceField.sendKeys("31000");

        WebElement createBtn = driver.findElement(By.id("save"));
        createBtn.click();

        (new WebDriverWait(driver, timeout)).until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver d) {

                String carId = d.findElement(By.cssSelector("#tbodycars > tr:nth-child(6) > td:nth-child(1)")).getText();
                MatcherAssert.assertThat(carId, Matchers.is("942"));

                return true;
            }
        });

    }

}
