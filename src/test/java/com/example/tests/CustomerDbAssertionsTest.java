package com.example.tests;

import com.example.util.Config;
import com.example.util.Db;
import com.example.util.RandomData;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class CustomerDbAssertionsTest {

    @BeforeAll
    static void setUpAll() {
        Assumptions.assumeTrue(Db.isConfigured(), "DB not configured; skipping DB assertions");
        RestAssured.baseURI = Config.getBaseUrl();
        Assumptions.assumeTrue(RestAssured.baseURI != null && !RestAssured.baseURI.isBlank(), "Base URL required for DB assertions");
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

        try (Connection conn = Db.getConnection()) {
            String schema = getSchema();
            boolean present = Db.findCustomerIdByEmail(conn, schema, email).isPresent();
            org.junit.jupiter.api.Assertions.assertTrue(present, "Customer not found in DB after create");
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

        // Verify
        try (Connection conn = Db.getConnection()) {
            String schema = getSchema();
            String name = Db.findCustomerNameById(conn, schema, id).orElse(null);
            org.junit.jupiter.api.Assertions.assertEquals("Updated Name", name, "Name not updated in DB");
        }
    }

    @Test
    void duplicateEmailViolatesUniqueConstraint() {
        // This assumes backend enforces unique email and returns 409
        String email = RandomData.randomEmail();
        
        // Create first customer
        createCustomer("Dup1", email, RandomData.randomPhone());

        // Try to create second customer with same email - should fail
        Map<String, Object> payload2 = new HashMap<>();
        payload2.put("name", "Dup2");
        payload2.put("email", email);
        payload2.put("phone", RandomData.randomPhone());
        RestAssured.given().contentType(ContentType.JSON).body(payload2)
            .when().post("/api/customers").then().statusCode(409);
    }
}


