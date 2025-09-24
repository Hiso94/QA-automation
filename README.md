## QA Automation (Java 17 + JUnit 5 + REST Assured)

### What is this?
Minimal starter that runs a single API test against `/actuator/health` with a configurable base URL. If no base URL is provided, the test starts an embedded WireMock server so `mvn test` passes out-of-the-box.

### Prerequisites
- Java 17
- Maven 3.9+

### Quick start
1. Install dependencies and run all tests:
   ```bash
   mvn test
   ```

2. Configure the target base URL (no hardcoding):
   - Edit `src/test/resources/config.properties`:
     ```
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

If `baseUrl` is missing/empty, tests will spin up WireMock and stub endpoints so the suite passes out-of-the-box.

### Project layout
```
src/
  test/
    java/
      com/example/util/Config.java
      com/example/tests/HealthCheckTest.java
      com/example/tests/CustomerCrudTest.java
      com/example/tests/CustomerValidationTest.java
    resources/
      config.properties
      config.example.properties
```

### How to run specific tests
- Single class:
  ```bash
  mvn -Dtest=CustomerCrudTest test
  ```
- Single method:
  ```bash
  mvn -Dtest=CustomerCrudTest#createCustomer test
  ```

### Current test coverage
- Health: `/actuator/health` returns 200
- Customer CRUD: Create → Get → List → Update → Delete
- Validation/Errors: 400 (missing/invalid), 404 (not found), 409 (conflict); asserts error body fields

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

### Reports
- Maven Surefire reports are generated under `target/surefire-reports/`.
  - Look for `*.txt` files summarizing each test class.

### Git
Initialize the repo, add remote, and push:
```bash
git init
git add .
git commit -m "chore: initial automation scaffold with healthcheck test"
git branch -M main
git remote add origin https://github.com/<you>/<repo>.git
git push -u origin main
```


