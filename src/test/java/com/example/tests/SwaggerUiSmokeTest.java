package com.example.tests;

import com.example.util.Config;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.ScreenshotType;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.junit.jupiter.api.*;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class SwaggerUiSmokeTest {

    private static Playwright playwright;
    private static Browser browser;
    private BrowserContext context;
    private Page page;
    private String swaggerUrl;

    @BeforeAll
    static void beforeAll() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
        
      
        try {
            Files.createDirectories(Paths.get("target/screenshots"));
        } catch (Exception e) {
            System.err.println("Could not create screenshots directory: " + e.getMessage());
        }
    }

    @AfterAll
    static void afterAll() {
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }

    @BeforeEach
    void setUp() {
        context = browser.newContext();
        page = context.newPage();
        
        String baseUrl = Config.getBaseUrl();
        Assumptions.assumeTrue(baseUrl != null && !baseUrl.isBlank(), "BASE_URL required for UI smoke");
        swaggerUrl = baseUrl + "/swagger-ui/index.html";
    }

    @AfterEach
    void tearDown() {
        if (context != null) context.close();
    }

    @Test
    void swaggerUiLoadsAndShowsTitle() {
        try {
            System.out.println("Navigating to: " + swaggerUrl);
            page.navigate(swaggerUrl);
            
            // Wait for page to load completely
            page.waitForLoadState(LoadState.NETWORKIDLE);
            
            // Wait for the Swagger UI to load completely
            page.waitForSelector(".swagger-ui", new Page.WaitForSelectorOptions().setTimeout(10000));
            
            // Additional wait for content to render
            page.waitForTimeout(2000);
            
            // Verify the page title
            String title = page.title();
            System.out.println("Page title: " + title);
            assertTrue(title.contains("Swagger") || title.contains("API"), 
                "Page title should contain 'Swagger' or 'API', but was: " + title);
            
            // Verify key Swagger UI elements are present
            assertTrue(page.locator(".swagger-ui").isVisible(), "Swagger UI container should be visible");
            
        } catch (Exception e) {
            System.err.println("Test failed, capturing screenshot. Error: " + e.getMessage());
            System.err.println("Current URL: " + page.url());
            captureScreenshot("swagger-ui-title-failure");
            throw new AssertionError("Swagger UI failed to load properly: " + e.getMessage(), e);
        }
    }

    @Test
    void swaggerUiShowsApiDocumentation() {
        try {
            page.navigate(swaggerUrl);
            
            // Wait for the API documentation to load
            page.waitForSelector(".info", new Page.WaitForSelectorOptions().setTimeout(15000));
            
            // Verify API info section is present
            assertTrue(page.locator(".info").isVisible(), "API info section should be visible");
            
            // Check if there are any API endpoints displayed
            page.waitForSelector(".opblock", new Page.WaitForSelectorOptions()
                .setTimeout(10000)
                .setState(WaitForSelectorState.VISIBLE));
            
            int endpointCount = page.locator(".opblock").count();
            assertTrue(endpointCount > 0, "At least one API endpoint should be documented");
            
        } catch (Exception e) {
            captureScreenshot("swagger-ui-documentation-failure");
            throw new AssertionError("API documentation failed to load: " + e.getMessage(), e);
        }
    }

    @Test
    void swaggerUiCanExpandFirstEndpoint() {
        try {
            page.navigate(swaggerUrl);
            
            // Wait for endpoints to load
            page.waitForSelector(".opblock", new Page.WaitForSelectorOptions().setTimeout(15000));
            
            // Find the first endpoint and click to expand it
            Locator firstEndpoint = page.locator(".opblock").first();
            assertTrue(firstEndpoint.isVisible(), "First API endpoint should be visible");
            
            // Click on the first endpoint to expand it
            firstEndpoint.click();
            
            // Wait for the endpoint details to expand
            page.waitForSelector(".opblock.is-open", new Page.WaitForSelectorOptions().setTimeout(5000));
            
            // Verify the endpoint expanded (shows additional details)
            assertTrue(page.locator(".opblock.is-open").isVisible(), 
                "First endpoint should expand and show details");
            
            // Check if "Try it out" button is available
            if (page.locator("button:has-text('Try it out')").count() > 0) {
                assertTrue(page.locator("button:has-text('Try it out')").first().isVisible(),
                    "Try it out button should be available for testing");
            }
            
        } catch (Exception e) {
            captureScreenshot("swagger-ui-endpoint-expansion-failure");
            throw new AssertionError("Failed to expand API endpoint: " + e.getMessage(), e);
        }
    }

    @Test
    void swaggerUiHandlesAuthentication() {
        try {
            page.navigate(swaggerUrl);
            
            // Wait for the page to load
            page.waitForSelector(".swagger-ui", new Page.WaitForSelectorOptions().setTimeout(10000));
            
            // Look for authorization/authentication elements
            if (page.locator("button:has-text('Authorize')").count() > 0) {
                Locator authorizeButton = page.locator("button:has-text('Authorize')");
                assertTrue(authorizeButton.isVisible(), "Authorize button should be visible");
                
                // Click authorize to see available auth methods
                authorizeButton.click();
                
                // Wait for auth modal to appear
                page.waitForSelector(".auth-container", new Page.WaitForSelectorOptions().setTimeout(5000));
                
                // Verify auth modal opened
                assertTrue(page.locator(".auth-container").isVisible(),
                    "Authentication modal should open when authorize is clicked");
                
                // Close the modal
                if (page.locator("button:has-text('Close')").count() > 0) {
                    page.locator("button:has-text('Close')").click();
                }
            } else {
                // If no auth button, verify the API is accessible without auth
                assertTrue(page.locator(".swagger-ui").isVisible(), 
                    "Swagger UI should be accessible (no authentication required)");
            }
            
        } catch (Exception e) {
            captureScreenshot("swagger-ui-auth-failure");
            throw new AssertionError("Authentication handling failed: " + e.getMessage(), e);
        }
    }

    /**
     * Captures a screenshot with timestamp for debugging failed tests
     */
    private void captureScreenshot(String testName) {
        try {
            // Wait for the page to be in a stable state before screenshot
            page.waitForLoadState(LoadState.NETWORKIDLE);
            
            // Add a small delay to ensure content is rendered
            page.waitForTimeout(1000);
            
            String timestamp = java.time.LocalDateTime.now().toString().replaceAll("[:.]", "-");
            String screenshotPath = String.format("screenshots/%s-%s.png", testName, timestamp);
            
            // Ensure screenshots directory exists
            Files.createDirectories(Paths.get("screenshots"));
            
            // Take screenshot with better options
            page.screenshot(new Page.ScreenshotOptions()
                .setPath(Paths.get(screenshotPath))
                .setFullPage(true)
                .setType(ScreenshotType.PNG));
            
            // Also capture viewport screenshot as backup
            String viewportPath = String.format("screenshots/%s-viewport-%s.png", testName, timestamp);
            page.screenshot(new Page.ScreenshotOptions()
                .setPath(Paths.get(viewportPath))
                .setFullPage(false));
            
            System.out.println("Screenshots captured:");
            System.out.println("  Full page: " + screenshotPath);
            System.out.println("  Viewport: " + viewportPath);
            System.out.println("Page URL at capture: " + page.url());
            System.out.println("Page title at capture: " + page.title());
        } catch (Exception e) {
            System.err.println("Failed to capture screenshot: " + e.getMessage());
            e.printStackTrace();
        }
    }
}


