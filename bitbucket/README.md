# Bitbucket Pipelines Configuration for QA Automation

This folder contains the Bitbucket Pipelines configuration that mirrors your GitHub Actions workflow.

## 📁 Files

- `bitbucket-pipelines.yml` - Main pipeline configuration file

## 🚀 How to Set Up in Bitbucket

### 1. Copy Configuration
Move the `bitbucket-pipelines.yml` file to your **repository root** (not in a subfolder):
```bash
cp bitbucket/bitbucket-pipelines.yml ./bitbucket-pipelines.yml
```

### 2. Enable Pipelines in Bitbucket
1. Go to your Bitbucket repository
2. Navigate to **Repository Settings** → **Pipelines** → **Settings**
3. Toggle **Enable Pipelines** to ON
4. Commit and push the `bitbucket-pipelines.yml` file

### 3. Pipeline Triggers

| Trigger | Description |
|---------|-------------|
| **Default** | Runs on any branch push (except main, develop, test-workflow-trigger) |
| **Main Branch** | Full smoke tests with comprehensive reporting |
| **Develop Branch** | Standard smoke tests for development |
| **Test-workflow-trigger Branch** | Tests for your current working branch |
| **Pull Requests** | Validation tests for all PRs |
| **Manual Trigger** | Comprehensive smoke tests (can be triggered manually) |

### 4. Key Features

#### ✅ What This Pipeline Does:
- **Installs Java 17 and Maven** using Docker image
- **Installs Playwright dependencies** for browser automation
- **Runs smoke tests**: `HealthCheckTest` and `SwaggerUiSmokeTest`
- **Generates reports**: Surefire HTML reports and Allure reports
- **Provides test summaries** with pass/fail counts
- **Uploads artifacts** (test results, reports) for download
- **Caches Maven dependencies** for faster builds

#### 🔧 Test Execution:
```bash
mvn test -Dtest="HealthCheckTest,SwaggerUiSmokeTest" 
  -Dmaven.test.failure.ignore=true 
  -Dallure.results.directory=target/allure-results 
  -DfailIfNoTests=false
```

#### 📊 Artifacts Generated:
- `target/surefire-reports/` - XML and HTML test results
- `target/site/` - Maven site reports
- `target/allure-report/` - Allure HTML reports
- `target/allure-results/` - Raw Allure data

### 5. Manual Execution
To run the manual comprehensive tests:
1. Go to **Pipelines** in your Bitbucket repository
2. Click **Run pipeline**
3. Select **Custom: manual-smoke-tests**
4. Click **Run**

### 6. Viewing Results
After pipeline execution:
1. Go to **Pipelines** → **Results**
2. Click on your pipeline run
3. Download artifacts to view detailed reports
4. Check the **Logs** for real-time test summaries

## 🔄 Migration Notes

### Differences from GitHub Actions:
- **File location**: Root of repository (not in `.github/workflows/`)
- **Artifacts**: Automatically retained for 14 days (configurable)
- **Caching**: Uses Bitbucket's built-in cache definitions
- **Environment**: Docker-based (using `maven:3.8.4-openjdk-17` image)
- **Manual triggers**: Available through custom pipelines

### Environment Variables (if needed):
Add to your pipeline step:
```yaml
script:
  - export TEST_ENV=staging
  - export BASE_URL=https://your-app.com
  - mvn test
```

## 🛠 Troubleshooting

### Common Issues:
1. **Pipeline not triggered**: Ensure `bitbucket-pipelines.yml` is in repository root
2. **Playwright issues**: The pipeline installs dependencies automatically
3. **Memory issues**: Docker service has 2048MB allocated
4. **Test failures**: Pipeline continues and reports results (doesn't fail the build)

### Support:
- Check Bitbucket Pipelines documentation
- View pipeline logs for detailed error messages
- Download artifacts to analyze test failures locally

## 📋 Next Steps
1. Copy `bitbucket-pipelines.yml` to repository root
2. Commit and push to Bitbucket
3. Enable Pipelines in repository settings
4. Test with a new commit or manual trigger