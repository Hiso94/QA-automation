package com.example.tests;

import com.example.util.Config;
import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SwaggerUiSmokeTest {

    private static Playwright playwright;
    private static Browser browser;
    private BrowserContext context;
    private Page page;

    @BeforeAll
    static void beforeAll() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
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
    }

    @AfterEach
    void tearDown(TestInfo info) {
        if (info.getTags().contains("failed")) {
            // JUnit doesn't tag failures by default; capture on exception instead
        }
        if (context != null) context.close();
    }

    @Test
    void swaggerUiLoadsAndShowsTitle() {
        String baseUrl = Config.getBaseUrl();
        Assumptions.assumeTrue(baseUrl != null && !baseUrl.isBlank(), "BASE_URL required for UI smoke");
        String swaggerUrl = baseUrl + "/swagger-ui/index.html";
        try {
            page.navigate(swaggerUrl);
            // Wait for the Swagger UI title element
            page.waitForSelector("text=Swagger UI");
            String content = page.content();
            assertTrue(content.contains("Swagger UI"));
        } catch (Throwable t) {
            page.screenshot(new Page.ScreenshotOptions()
                .setPath(Paths.get("target/screenshots/swagger-ui-failure.png"))
                .setFullPage(true));
            throw t;
        }
    }
}


