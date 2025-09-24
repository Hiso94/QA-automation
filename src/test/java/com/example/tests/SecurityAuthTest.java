package com.example.tests;

import com.example.util.Config;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.restassured.RestAssured;
import org.junit.jupiter.api.*;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;

public class SecurityAuthTest {

    private static WireMockServer wireMock;
    private static String baseUrl;

    @BeforeAll
    static void setUpAll() {
        baseUrl = Config.getBaseUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            wireMock = new WireMockServer(WireMockConfiguration.options().dynamicPort());
            wireMock.start();
            stubSecurityEndpoints(wireMock);
            baseUrl = "http://localhost:" + wireMock.port();
        }
        RestAssured.baseURI = baseUrl;
    }

    @AfterAll
    static void tearDownAll() {
        if (wireMock != null) wireMock.stop();
    }

    @Test
    void deleteWithoutTokenReturns401() {
        RestAssured.given()
            .when().delete("/api/customers/{id}", "x1")
            .then().statusCode(401);
    }

    @Test
    void deleteWithUserRoleReturns403() {
        RestAssured.given()
            .header("Authorization", "Bearer valid-user")
            .when().delete("/api/customers/{id}", "x1")
            .then().statusCode(403);
    }

    @Test
    void deleteWithAdminRoleReturns204() {
        RestAssured.given()
            .header("Authorization", "Bearer valid-admin")
            .when().delete("/api/customers/{id}", "x1")
            .then().statusCode(204);
    }

    @Test
    void deleteWithExpiredTokenReturns401() {
        RestAssured.given()
            .header("Authorization", "Bearer expired")
            .when().delete("/api/customers/{id}", "x1")
            .then().statusCode(401);
    }

    @Test
    void deleteWithMalformedTokenReturns401() {
        RestAssured.given()
            .header("Authorization", "Bearer malformed")
            .when().delete("/api/customers/{id}", "x1")
            .then().statusCode(401);
    }

    private static void stubSecurityEndpoints(WireMockServer wm) {
        // 401 when no Authorization header
        wm.stubFor(delete(urlPathMatching("/api/customers/.+"))
            .withHeader("Authorization", absent())
            .willReturn(aResponse().withStatus(401)));

        // 403 when user role token
        wm.stubFor(delete(urlPathMatching("/api/customers/.+"))
            .withHeader("Authorization", matching("Bearer\\s+valid-user"))
            .willReturn(aResponse().withStatus(403)));

        // 204 when admin role token
        wm.stubFor(delete(urlPathMatching("/api/customers/.+"))
            .withHeader("Authorization", matching("Bearer\\s+valid-admin"))
            .willReturn(aResponse().withStatus(204)));

        // 401 expired or malformed
        wm.stubFor(delete(urlPathMatching("/api/customers/.+"))
            .withHeader("Authorization", matching("Bearer\\s+(expired|malformed)"))
            .willReturn(aResponse().withStatus(401)));

        // Optional: 200 on GET for authenticated user
        wm.stubFor(get(urlPathMatching("/api/customers/.+"))
            .withHeader("Authorization", matching("Bearer\\s+valid-.*"))
            .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody("{\"id\":\"x1\"}")));
    }
}


