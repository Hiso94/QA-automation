## QA Automation (Java 17 + JUnit 5 + REST Assured + Playwright)

### What is this?
Comprehensive test automation suite featuring API testing, database validation, contract testing, security checks, and UI automation. Includes REST Assured for API tests, PostgreSQL integration for database assertions, OpenAPI contract validation, and Playwright for Swagger UI smoke testing. If no base URL is provided, tests use embedded WireMock server for out-of-the-box functionality.

### Prerequisites
- Java 17
- Maven 3.9+
- PostgreSQL (for database tests)
- Python 3 or Node.js (for Swagger UI server)

### Quick start
1. **Install dependencies and run all tests:**
   ```bash
   mvn test
   ```

2. **For Swagger UI tests, start the local server:**
   ```bash
   # Option 1: Using the provided script
   ./start-swagger-server.sh
   
   # Option 2: Manual Python server
   cd src/test/resources && python3 -m http.server 8080
   
   # Option 3: Custom Python server
   python3 serve-swagger.py
   ```
   Then open: http://localhost:8080/swagger-ui/index.html

3. **Configure the target base URL (no hardcoding):**
   - Edit `src/test/resources/config.properties`:
     ```properties
     baseUrl=http://localhost:8080
     ```
   - Or override at runtime:
     ```bash
     mvn test -DbaseUrl=http://localhost:8080
     ```
   - Or via environment variable:
     ```bash
     BASE_URL=http://localhost:8080 mvn test
     ```

4. **Configure database connection (for DB tests):**
   - Edit `src/test/resources/db.properties`:
     ```properties
     db.url=jdbc:postgresql://localhost:5432/yourdb
     db.username=your_username
     db.password=your_password
     ```

If `baseUrl` is missing/empty, tests will spin up WireMock and stub endpoints so the suite passes out-of-the-box.

### Project layout
```
src/
  test/
    java/
      com/example/
        tests/
          HealthCheckTest.java          # API health endpoint testing
          CustomerCrudTest.java         # Full CRUD operations testing
          CustomerValidationTest.java   # Input validation & error handling
          CustomerDbAssertionsTest.java # Database state validation
          ContractTest.java            # OpenAPI contract validation
          SecurityAuthTest.java        # Authentication & authorization
          SwaggerUiSmokeTest.java      # UI automation with Playwright
        util/
          Config.java                  # Configuration management
          Db.java                      # Database connection utilities
          RandomData.java              # Test data generation
    resources/
      config.properties               # Base URL configuration
      config.example.properties       # Configuration template
      db.properties                   # Database connection settings
      openapi.json                    # API contract specification
      swagger-ui/
        index.html                    # Local Swagger UI for testing
screenshots/                          # Test failure screenshots
serve-swagger.py                      # Python server for Swagger UI
start-swagger-server.sh              # Shell script to start UI server
```

### How to run specific tests

**Run by test type:**
```bash
# API tests only
mvn test -Dtest="*Test" -DexcludeTests="*UiSmokeTest"

# UI tests only (requires Swagger server running)
mvn test -Dtest="SwaggerUiSmokeTest"

# Database tests only
mvn test -Dtest="*DbAssertionsTest"

# Security tests only
mvn test -Dtest="SecurityAuthTest"

# Contract tests only
mvn test -Dtest="ContractTest"
```

**Run specific classes:**
```bash
mvn test -Dtest=CustomerCrudTest
mvn test -Dtest=SwaggerUiSmokeTest
mvn test -Dtest=CustomerDbAssertionsTest
```

**Run specific methods:**
```bash
mvn test -Dtest=CustomerCrudTest#createCustomer
mvn test -Dtest=SwaggerUiSmokeTest#swaggerUiLoadsAndShowsTitle
mvn test -Dtest=CustomerDbAssertionsTest#customerCreationInsertsCorrectRecord
```

### Current test coverage

**🚀 API Testing (REST Assured):**
- **Health Check:** `/actuator/health` endpoint validation
- **Customer CRUD:** Full lifecycle testing (Create → Read → Update → Delete)
- **Validation & Errors:** 400 (bad request), 404 (not found), 409 (conflict)
- **Response Validation:** JSON structure, data types, error messages

**🗄️ Database Testing (PostgreSQL):**
- **State Validation:** Verify database changes after API operations
- **Data Integrity:** Check foreign keys, constraints, relationships
- **Transaction Testing:** Rollback scenarios and consistency checks
- **Custom Queries:** Direct SQL validation for complex business logic

**📋 Contract Testing (OpenAPI):**
- **Schema Validation:** Request/response against OpenAPI spec
- **API Compliance:** Ensures API matches documented contract
- **Automatic Validation:** Integrated with REST Assured tests

**🔐 Security Testing:**
- **Authentication:** Token validation and session management
- **Authorization:** Role-based access control testing
- **Input Sanitization:** SQL injection and XSS prevention
- **Error Handling:** Security-conscious error responses

**🖥️ UI Automation (Playwright):**
- **Swagger UI Testing:** Page loading and element validation
- **Interactive Testing:** Expand endpoints, execute API calls
- **Authentication Flow:** Handle auth dialogs and tokens
- **Screenshot Capture:** Automatic failure documentation
- **Headless Execution:** CI/CD compatible browser automation

### How to add new tests

**🔧 API Test (REST Assured):**
```java
@Test
void yourNewApiTest() {
    given()
        .baseUri(Config.getBaseUrl())
        .contentType(ContentType.JSON)
        .body(requestData)
    .when()
        .post("/your-endpoint")
    .then()
        .statusCode(200)
        .body("field", equalTo("expected_value"));
}
```

**🗄️ Database Test:**
```java
@Test
void yourDatabaseTest() {
    // Perform API operation
    String customerId = createCustomerViaApi();
    
    // Validate database state
    try (Connection conn = Db.getConnection()) {
        String sql = "SELECT * FROM customers WHERE id = ?";
        // ... database assertions
    }
}
```

**🖥️ UI Test (Playwright):**
```java
@Test
void yourUiTest() {
    try {
        page.navigate(baseUrl + "/your-page");
        page.waitForSelector(".your-element");
        assertTrue(page.locator(".your-element").isVisible());
    } catch (Exception e) {
        captureScreenshot("your-test-failure");
        throw new AssertionError("UI test failed", e);
    }
}
```

**📋 Contract Test:**
```java
@Test
void yourContractTest() {
    ValidatableResponse response = given()
        .filter(openApiValidationFilter)  // Auto-validates against spec
        .baseUri(Config.getBaseUrl())
    .when()
        .get("/your-endpoint")
    .then()
        .statusCode(200);
}
```

### Reports & Artifacts

**📊 Test Reports:**
- **Surefire Reports:** `target/surefire-reports/` (HTML & XML format)
- **JaCoCo Coverage:** `target/site/jacoco/index.html` (run `mvn jacoco:report`)
- **Test Results:** Individual `.txt` files for each test class

**📸 Screenshots (UI Test Failures):**
- **Location:** `screenshots/` directory  
- **Format:** `[test-name]-[timestamp].png`
- **Full Page:** Complete browser screenshots for debugging
- **Auto-Generated:** Captured automatically on UI test failures

**📋 Logs & Debugging:**
- **Maven Output:** Detailed test execution logs
- **Database Logs:** Connection and query information
- **Playwright Traces:** Browser interaction recordings (if enabled)

### Troubleshooting

**🚫 Common Issues & Solutions:**

1. **Swagger UI tests failing:**
   ```bash
   # Start the local server first
   ./start-swagger-server.sh
   # Then run UI tests
   mvn test -Dtest=SwaggerUiSmokeTest
   ```

2. **Database connection errors:**
   ```bash
   # Check db.properties configuration
   # Ensure PostgreSQL is running
   # Verify database credentials and permissions
   ```

3. **Port conflicts (8080 already in use):**
   ```bash
   # Kill existing process
   lsof -ti:8080 | xargs kill -9
   # Or use different port in config.properties
   ```

4. **Maven dependency issues:**
   ```bash
   mvn clean install
   mvn dependency:resolve
   ```

**✅ Best Practices:**

- **Environment Isolation:** Use separate test databases
- **Data Cleanup:** Clean up test data between runs
- **Parallel Execution:** Configure thread-safe tests
- **CI/CD Integration:** Use headless browser mode
- **Screenshot Debugging:** Check `screenshots/` folder for UI failures
- **Contract Validation:** Keep OpenAPI spec up-to-date

### CI/CD Integration

**GitHub Actions Example:**
```yaml
name: QA Automation Tests
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    services:
      postgres:
        image: postgres:13
        env:
          POSTGRES_PASSWORD: testpass
          POSTGRES_DB: testdb
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
      - name: Install Playwright
        run: npx playwright install chromium
      - name: Run tests
        run: mvn test
      - name: Upload screenshots
        if: failure()
        uses: actions/upload-artifact@v3
        with:
          name: test-screenshots
          path: screenshots/
```

### Git
Initialize the repo, add remote, and push:
```bash
git init
git add .
git commit -m "feat: comprehensive QA automation suite with API, DB, UI, and contract testing"
git branch -M main
git remote add origin https://github.com/<you>/<repo>.git
git push -u origin main
```


# Test workflow trigger - Wed Oct  1 13:41:15 +04 2025
