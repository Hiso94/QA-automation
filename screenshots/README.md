# 📸 Test Screenshots

This directory contains screenshots captured during test failures for debugging purposes.

## 🎯 Purpose
- **Failure Analysis**: Screenshots are automatically captured when UI tests fail
- **Debugging**: Visual evidence of what went wrong during test execution
- **Documentation**: Visual proof of test scenarios and expected vs actual behavior

## 📁 Structure
```
screenshots/
├── README.md                          # This file
├── examples/                          # Example screenshots for documentation
└── [timestamp]/                       # Auto-generated during test runs
    ├── swagger-ui-title-failure-[timestamp].png
    ├── swagger-ui-documentation-failure-[timestamp].png
    └── swagger-ui-endpoint-expansion-failure-[timestamp].png
```

## 🔧 How Screenshots Are Generated
Screenshots are automatically captured by the `SwaggerUiSmokeTest` class when:
- Page fails to load
- Expected elements are missing
- API interactions fail
- Authentication issues occur

## 📋 Screenshot Naming Convention
- **Format**: `[test-name]-[timestamp].png`
- **Example**: `swagger-ui-title-failure-2025-09-29T14-30-45.png`

## 🎨 Test Coverage
Our UI tests capture screenshots for:
- ✅ Swagger UI page loading
- ✅ API documentation display
- ✅ Endpoint expansion interactions
- ✅ Authentication workflows
- ✅ Error conditions

## 🔍 How to Use
1. Run tests: `mvn test -Dtest=SwaggerUiSmokeTest`
2. Check this directory for failure screenshots
3. Analyze visual evidence to debug issues
4. Screenshots are automatically timestamped for easy tracking