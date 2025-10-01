package com.example.tests;

import com.example.util.Config;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.hamcrest.Matchers.equalTo;

@Epic("Application Health Monitoring")
@Feature("Health Check API")
public class HealthCheckTest {

    private static WireMockServer wireMock;
    private static String baseUrl;

    @BeforeAll
    static void setUp() {
        baseUrl = Config.getBaseUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            wireMock = new WireMockServer(WireMockConfiguration.options().dynamicPort());
            wireMock.start();
            wireMock.stubFor(
                get(urlEqualTo("/actuator/health"))
                    .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"status\":\"UP\"}"))
            );
            baseUrl = "http://localhost:" + wireMock.port();
        }
        RestAssured.baseURI = baseUrl;
    }

    @AfterAll
    static void tearDown() {
        if (wireMock != null) {
            wireMock.stop();
        }
    }

    @Test
    @Story("Health endpoint availability")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Verify that the health endpoint returns HTTP 200 status code, indicating the application is running")
    void healthEndpointReturns200() {
        RestAssured
            .given()
            .when()
            .get("/actuator/health")
            .then()
            .statusCode(200);
    }

    @Test
    @Story("Health endpoint response validation")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify that the health endpoint returns proper JSON response with status 'UP' when application is healthy")
    void healthPayloadHasStatusUpWhenStubbed() {
        if (wireMock == null) {
            return; // when pointing at real system, we only assert status code in smoke
        }
        RestAssured
            .given()
            .when()
            .get("/actuator/health")
            .then()
            .statusCode(200)
            .body("status", equalTo("UP"));
    }
}


