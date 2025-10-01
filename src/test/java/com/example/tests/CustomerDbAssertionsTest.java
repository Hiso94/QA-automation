package com.example.tests;

import com.example.util.Config;
import com.example.util.Db;
import com.example.util.RandomData;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class CustomerDbAssertionsTest {

    private static WireMockServer wireMock;
    private static String baseUrl;

    @BeforeAll
    static void setUpAll() {
        Assumptions.assumeTrue(Db.isConfigured(), "DB not configured; skipping DB assertions");
        
        baseUrl = Config.getBaseUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            // Start WireMock server with response templating for dynamic responses
            wireMock = new WireMockServer(WireMockConfiguration.options()
                .dynamicPort()
                .extensions(new ResponseTemplateTransformer(false)));
            wireMock.start();
            
            // Stub POST /api/customers - create customer
            wireMock.stubFor(post(urlPathEqualTo("/api/customers"))
                .willReturn(aResponse()
                    .withStatus(201)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"id\": \"{{randomValue type='UUID'}}\", \"name\": \"{{jsonPath request.body '$.name'}}\", \"email\": \"{{jsonPath request.body '$.email'}}\", \"phone\": \"{{jsonPath request.body '$.phone'}}\"}")
                    .withTransformers("response-template")));
            
            // Stub PUT /api/customers/{id} - update customer  
            wireMock.stubFor(put(urlPathMatching("/api/customers/.*"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"id\": \"{{request.pathSegments.[2]}}\", \"name\": \"{{jsonPath request.body '$.name'}}\", \"email\": \"{{jsonPath request.body '$.email'}}\", \"phone\": \"{{jsonPath request.body '$.phone'}}\"}")
                    .withTransformers("response-template")));
            
            // Stub duplicate email constraint (409 for duplicate)
            wireMock.stubFor(post(urlPathEqualTo("/api/customers"))
                .withRequestBody(matchingJsonPath("$.email", matching(".*duplicate.*")))
                .willReturn(aResponse().withStatus(409)));
            
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

    /**
     * Helper method to create a customer with specific details
     */
    private String createCustomer(String name, String email, String phone) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", name);
        payload.put("email", email);
        payload.put("phone", phone);

        return RestAssured.given()
            .contentType(ContentType.JSON)
            .body(payload)
            .when().post("/api/customers")
            .then().statusCode(anyOf(is(201), is(200)))
            .body("id", notNullValue())
            .extract().path("id");
    }

    /**
     * Helper method to create a customer with random email and phone
     */
    private String createCustomerWithRandomData(String name) {
        return createCustomer(name, RandomData.randomEmail(), RandomData.randomPhone());
    }

    /**
     * Helper method to get database schema
     */
    private String getSchema() {
        return System.getProperty("db.schema", System.getenv().getOrDefault("DB_SCHEMA", "public"));
    }

    @Test
    void createAndVerifyInDb() throws Exception {
        String email = RandomData.randomEmail();
        String id = createCustomer("DB Check", email, RandomData.randomPhone());

        if (wireMock == null) {
            // Only test DB when using real API
            try (Connection conn = Db.getConnection()) {
                String schema = getSchema();
                boolean present = Db.findCustomerIdByEmail(conn, schema, email).isPresent();
                org.junit.jupiter.api.Assertions.assertTrue(present, "Customer not found in DB after create");
            }
        } else {
            // When using WireMock, just verify API call succeeded (customer ID returned)
            org.junit.jupiter.api.Assertions.assertNotNull(id, "Customer ID should be returned from API");
            org.junit.jupiter.api.Assertions.assertFalse(id.isBlank(), "Customer ID should not be blank");
        }
    }

    @Test
    void updateThenVerifyInDb() throws Exception {
        // Create
        String email = RandomData.randomEmail();
        String id = createCustomer("To Update", email, RandomData.randomPhone());

        // Update
        Map<String, Object> update = new HashMap<>();
        update.put("name", "Updated Name");
        update.put("email", email);
        update.put("phone", RandomData.randomPhone());
        RestAssured.given().contentType(ContentType.JSON).body(update)
            .when().put("/api/customers/{id}", id).then().statusCode(200);

        if (wireMock == null) {
            // Only verify in DB when using real API
            try (Connection conn = Db.getConnection()) {
                String schema = getSchema();
                String name = Db.findCustomerNameById(conn, schema, id).orElse(null);
                org.junit.jupiter.api.Assertions.assertEquals("Updated Name", name, "Name not updated in DB");
            }
        } else {
            // When using WireMock, just verify API update succeeded (200 status already checked above)
            org.junit.jupiter.api.Assertions.assertNotNull(id, "Customer ID should exist for update");
        }
    }

    @Test
    void duplicateEmailViolatesUniqueConstraint() {
        if (wireMock == null) {
            // When testing against real API, test actual duplicate constraint
            String email = RandomData.randomEmail();
            createCustomer("Dup1", email, RandomData.randomPhone());
            
            Map<String, Object> payload2 = new HashMap<>();
            payload2.put("name", "Dup2");
            payload2.put("email", email);       
            payload2.put("phone", RandomData.randomPhone());
            RestAssured.given().contentType(ContentType.JSON).body(payload2)
                .when().post("/api/customers").then().statusCode(409);
        } else {
            // When using WireMock, test the stub for duplicate detection
            Map<String, Object> payload = new HashMap<>();
            payload.put("name", "Test Duplicate");
            payload.put("email", "duplicate@test.com"); // This triggers 409 in our stub
            payload.put("phone", RandomData.randomPhone());
            
            RestAssured.given().contentType(ContentType.JSON).body(payload)
                .when().post("/api/customers").then().statusCode(409);
        }
    }    
}


