## QA Automation (Java 17 + JUnit 5 + REST Assured)

### What is this?
Minimal starter that runs a single API test against `/actuator/health` with a configurable base URL. If no base URL is provided, the test starts an embedded WireMock server so `mvn test` passes out-of-the-box.

### Prerequisites
- Java 17
- Maven 3.9+

### Quick start
1. Install dependencies and run tests:
   ```bash
   mvn -q test
   ```

2. Configure the target base URL (no hardcoding):
   - Edit `src/test/resources/config.properties` and set:
     ```
     baseUrl=http://localhost:8080
     ```
   - Or override at runtime:
     ```bash
     mvn -q test -DbaseUrl=http://localhost:8080
     ```
   - Or via environment variable:
     ```bash
     BASE_URL=http://localhost:8080 mvn -q test
     ```

If `baseUrl` is missing/empty, tests will spin up WireMock and stub `/actuator/health` → 200 OK.

### Project layout
```
src/
  test/
    java/
      com/example/util/Config.java
      com/example/tests/HealthCheckTest.java
    resources/
      config.properties
      config.example.properties
```

### How to add a new API test
1. Create a new test class under `src/test/java/com/example/tests`.
2. Use `Config.getBaseUrl()` for the base URI.
3. Write REST Assured assertions.

Example snippet:
```java
given()
  .baseUri(Config.getBaseUrl())
  .when()
  .get("/your-endpoint")
  .then()
  .statusCode(200);
```

### Git
Initialize the repo and make the first commit:
```bash
git init
git add .
git commit -m "chore: initial automation scaffold with healthcheck test"
```


