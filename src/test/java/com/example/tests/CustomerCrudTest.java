package com.example.tests;

import com.example.util.Config;
import com.example.util.RandomData;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CustomerCrudTest {

    private static WireMockServer wireMock;
    private static String baseUrl;
    private static String createdId;
    private static Map<String, Object> customer;

    @BeforeAll
    static void setUpAll() {
        baseUrl = Config.getBaseUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            wireMock = new WireMockServer(
                WireMockConfiguration.options()
                    .dynamicPort()
                    .extensions(new ResponseTemplateTransformer(true))
            );
            wireMock.start();
            stubCrudEndpoints(wireMock);
            baseUrl = "http://localhost:" + wireMock.port();
        }
        RestAssured.baseURI = baseUrl;
    }

    @AfterAll
    static void tearDownAll() {
        if (wireMock != null) {
            wireMock.stop();
        }
    }

    @BeforeEach
    void initData() {
        customer = new HashMap<>();
        customer.put("name", "Customer " + RandomData.randomString(6));
        customer.put("email", RandomData.randomEmail());
        customer.put("phone", RandomData.randomPhone());
    }

    @Test
    @Order(1)
    void createCustomer() {
        createdId = RestAssured
            .given()
            .contentType(ContentType.JSON)
            .body(customer)
            .when()
            .post("/api/customers")
            .then()
            .statusCode(anyOf(is(201), is(200)))
            .body("id", notNullValue())
            .extract()
            .path("id");
    }

    @Test
    @Order(2)
    void getCustomerById() {
        Assumptions.assumeTrue(createdId != null && !createdId.isBlank(), "Create failed, id missing");
        RestAssured
            .given()
            .when()
            .get("/api/customers/{id}", createdId)
            .then()
            .statusCode(200)
            .body("id", equalTo(createdId));
    }

    @Test
    @Order(3)
    void listCustomers() {
        RestAssured
            .given()
            .when()
            .get("/api/customers")
            .then()
            .statusCode(200)
            .body("size()", greaterThanOrEqualTo(1));
    }

    @Test
    @Order(4)
    void updateCustomer() {
        Assumptions.assumeTrue(createdId != null && !createdId.isBlank(), "Create failed, id missing");
        Map<String, Object> update = new HashMap<>(customer);
        update.put("name", customer.get("name") + " Updated");

        RestAssured
            .given()
            .contentType(ContentType.JSON)
            .body(update)
            .when()
            .put("/api/customers/{id}", createdId)
            .then()
            .statusCode(200)
            .body("name", endsWith("Updated"));
    }

    @Test
    @Order(5)
    void deleteCustomer() {
        Assumptions.assumeTrue(createdId != null && !createdId.isBlank(), "Create failed, id missing");
        RestAssured
            .given()
            .when()
            .delete("/api/customers/{id}", createdId)
            .then()
            .statusCode(anyOf(is(200), is(204)));

        // When using WireMock, add a specific stub to return 404 for the deleted id and verify
        if (wireMock != null) {
            wireMock.stubFor(get(urlEqualTo("/api/customers/" + createdId))
                .willReturn(aResponse().withStatus(404)));

            RestAssured
                .given()
                .when()
                .get("/api/customers/{id}", createdId)
                .then()
                .statusCode(404);
        }
    }

    private static void stubCrudEndpoints(WireMockServer wm) {
        // Create
        wm.stubFor(post(urlEqualTo("/api/customers"))
            .willReturn(aResponse()
                .withStatus(201)
                .withHeader("Content-Type", "application/json")
                .withTransformers("response-template")
                .withBody("{\n  \"id\": \"{{randomValue length=8 type='ALPHANUMERIC'}}\",\n  \"name\": \"{{jsonPath request.body '$.name'}}\",\n  \"email\": \"{{jsonPath request.body '$.email'}}\",\n  \"phone\": \"{{jsonPath request.body '$.phone'}}\"\n}")));

        // List (always returns at least one)
        wm.stubFor(get(urlEqualTo("/api/customers"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("[{\"id\":\"abc12345\",\"name\":\"Seed\",\"email\":\"seed@example.test\",\"phone\":\"+10000000000\"}]")));

        // Get by id
        wm.stubFor(get(urlPathMatching("/api/customers/([A-Za-z0-9_-])+"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withTransformers("response-template")
                .withBody("{\n  \"id\": \"{{request.path.[2]}}\",\n  \"name\": \"Stub Name\",\n  \"email\": \"stub@example.test\",\n  \"phone\": \"+19999999999\"\n}")));

        // Update
        wm.stubFor(put(urlPathMatching("/api/customers/([A-Za-z0-9_-])+"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withTransformers("response-template")
                .withBody("{\n  \"id\": \"{{request.path.[2]}}\",\n  \"name\": \"{{jsonPath request.body '$.name'}}\",\n  \"email\": \"{{jsonPath request.body '$.email'}}\",\n  \"phone\": \"{{jsonPath request.body '$.phone'}}\"\n}")));

        // Delete
        wm.stubFor(delete(urlPathMatching("/api/customers/([A-Za-z0-9_-])+"))
            .willReturn(aResponse().withStatus(204)));

        // Note: 404 after delete is stubbed dynamically per id in the test
    }
}


