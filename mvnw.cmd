@echo off
where mvn >nul 2>&1
if errorlevel 1 (
  echo Maven is required. Install Maven or run this project in the provided Dockerfile.
  exit /b 1
)
mvn %*