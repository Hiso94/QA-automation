# CI/CD Pipeline Documentation

This document explains the comprehensive CI/CD pipeline setup for the QA Automation project.

## 🚀 Pipeline Overview

The CI/CD pipeline consists of several automated workflows that ensure code quality, test coverage, and deployment reliability:

### 1. **Smoke Tests CI** (`smoke-tests.yml`)
- **Triggers**: Every push to `main`/`develop`, Pull Requests to `main`
- **Purpose**: Quick validation of critical functionality
- **Tests**: `HealthCheckTest`, `SwaggerUiSmokeTest`
- **Duration**: ~5-10 minutes

### 2. **Nightly Full Test Suite** (`nightly-tests.yml`)
- **Triggers**: Scheduled daily at 2 AM UTC, Manual trigger
- **Purpose**: Comprehensive testing across multiple Java versions
- **Tests**: All test classes
- **Duration**: ~30-45 minutes

### 3. **Branch Protection** (`branch-protection.yml`)
- **Triggers**: Pull Requests to `main`
- **Purpose**: Enforce quality gates before merging
- **Tests**: Critical tests that **must pass**
- **Failure**: Blocks PR merging

### 4. **Contract Drift Detection** (`contract-drift-detection.yml`)
- **Triggers**: Changes to OpenAPI specs, Manual trigger
- **Purpose**: Detect API contract violations
- **Validation**: OpenAPI specification compliance
- **Failure**: Blocks on breaking changes

### 5. **Publish Test Reports** (`publish-reports.yml`)
- **Triggers**: After other workflows complete, Manual trigger
- **Purpose**: Generate and publish HTML reports
- **Output**: GitHub Pages with interactive reports

## 📊 Reporting

### Surefire HTML Reports
- **Location**: `target/site/surefire-report.html`
- **Features**: Test results, execution time, failure details
- **Access**: Available in workflow artifacts

### Allure Reports
- **Location**: `target/allure-report/index.html`
- **Features**: Interactive reports, screenshots, test history
- **Access**: Published to GitHub Pages

### GitHub Pages Reports
- **URL**: `https://{username}.github.io/{repo-name}/`
- **Features**: Consolidated view of all test reports
- **Updates**: Automatic after each workflow run

## 🔧 Configuration

### Maven Configuration
The `pom.xml` includes enhanced reporting plugins:

```xml
<!-- Surefire HTML Reports -->
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-surefire-report-plugin</artifactId>
</plugin>

<!-- Allure Reports -->
<plugin>
  <groupId>io.qameta.allure</groupId>
  <artifactId>allure-maven</artifactId>
</plugin>
```

### Test Annotations
Tests are enhanced with Allure annotations for better reporting:

```java
@Epic("Application Health Monitoring")
@Feature("Health Check API")
@Story("Health endpoint availability")
@Severity(SeverityLevel.BLOCKER)
@Description("Verify that the health endpoint returns HTTP 200")
```

## 🛡️ Quality Gates

### Merge Protection
- ✅ Smoke tests must pass
- ✅ Critical tests must pass
- ✅ No contract breaking changes
- ✅ Code must be reviewed

### Contract Validation
- 🚫 **Breaking Changes**: Block merge automatically
- ⚠️ **Non-breaking Changes**: Allow but warn
- ✅ **No Changes**: Proceed normally

## 📱 Notifications

### Pull Request Comments
Workflows automatically comment on PRs with:
- Test execution results
- Contract change analysis
- Link to detailed reports
- Pass/fail status

### Issue Creation
Nightly failures automatically create GitHub issues with:
- Failure details
- Link to workflow run
- Labels for easy tracking

## 🔄 Running Workflows

### Locally
```bash
# Run smoke tests
mvn test -Dtest="HealthCheckTest,SwaggerUiSmokeTest"

# Generate HTML reports
mvn surefire-report:report-only site

# Generate Allure reports
mvn allure:report
```

### GitHub Actions
- **Automatic**: Triggered by pushes, PRs, schedule
- **Manual**: Use "Run workflow" button in Actions tab
- **Debug**: Check workflow logs and artifacts

## 🚨 Troubleshooting

### Test Failures
1. Check workflow logs in GitHub Actions
2. Download and review test artifacts
3. Check Allure reports for detailed failure analysis
4. Review contract validation results

### Pipeline Failures
1. Verify Java version compatibility
2. Check Maven dependency resolution
3. Ensure Playwright browsers are installed
4. Review OpenAPI specification changes

### Report Issues
1. Check GitHub Pages deployment status
2. Verify workflow permissions
3. Review artifact upload/download logs

## 📈 Metrics and Monitoring

### Test Coverage
- Tracked via Allure reports
- Historical trend analysis
- Per-feature breakdown

### Performance
- Test execution time tracking
- Workflow duration monitoring
- Resource usage analysis

### Quality Metrics
- Pass/fail rates
- Flaky test detection
- Contract compliance scores

## 🔗 Links

- [GitHub Actions Workflows](../.github/workflows/)
- [Test Reports (GitHub Pages)](https://{username}.github.io/{repo-name}/)
- [Allure Documentation](https://docs.qameta.io/allure/)
- [Maven Surefire Plugin](https://maven.apache.org/surefire/maven-surefire-plugin/)

---

**Note**: Replace `{username}` and `{repo-name}` with your actual GitHub username and repository name.

