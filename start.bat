@echo off
echo Checking for process on port 8080...

for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8080 ^| findstr LISTENING 2^>nul') do (
    echo Killing process with PID: %%a
    taskkill /F /PID %%a >nul 2>&1
)

echo Port 8080 is free. Starting application...
echo.

mvn clean spring-boot:run
