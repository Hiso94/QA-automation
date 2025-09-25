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

    @Test
    void createAndVerifyInDb() throws Exception {
        String email = RandomData.randomEmail();
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", "DB Check");
        payload.put("email", email);
        payload.put("phone", "+10000000000");

        String id = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(payload)
            .when().post("/api/customers")
            .then().statusCode(anyOf(is(201), is(200))).body("id", notNullValue())
            .extract().path("id");

        try (Connection conn = Db.getConnection()) {
            String schema = System.getProperty("db.schema", System.getenv().getOrDefault("DB_SCHEMA", "public"));
            boolean present = Db.findCustomerIdByEmail(conn, schema, email).isPresent();
            org.junit.jupiter.api.Assertions.assertTrue(present, "Customer not found in DB after create");
        }
    }

    @Test
    void updateThenVerifyInDb() throws Exception {
        // Create
        String email = RandomData.randomEmail();
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", "To Update");
        payload.put("email", email);
        payload.put("phone", "+10000000000");
        String id = RestAssured.given().contentType(ContentType.JSON).body(payload)
            .when().post("/api/customers").then().statusCode(anyOf(is(201), is(200))).extract().path("id");

        // Update
        Map<String, Object> update = new HashMap<>(payload);
        update.put("name", "Updated Name");
        RestAssured.given().contentType(ContentType.JSON).body(update)
            .when().put("/api/customers/{id}", id).then().statusCode(200);

        // Verify
        try (Connection conn = Db.getConnection()) {
            String schema = System.getProperty("db.schema", System.getenv().getOrDefault("DB_SCHEMA", "public"));
            String name = Db.findCustomerNameById(conn, schema, id).orElse(null);
            org.junit.jupiter.api.Assertions.assertEquals("Updated Name", name, "Name not updated in DB");
        }
    }

    @Test
    void duplicateEmailViolatesUniqueConstraint() {
        // This assumes backend enforces unique email and returns 409
        String email = RandomData.randomEmail();
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", "Dup1");
        payload.put("email", email);
        payload.put("phone", "+10000000000");
        RestAssured.given().contentType(ContentType.JSON).body(payload)
            .when().post("/api/customers").then().statusCode(anyOf(is(201), is(200)));

        Map<String, Object> payload2 = new HashMap<>(payload);
        payload2.put("name", "Dup2");
        RestAssured.given().contentType(ContentType.JSON).body(payload2)
            .when().post("/api/customers").then().statusCode(409);
    }
}


