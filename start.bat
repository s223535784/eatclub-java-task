@echo off
echo Checking for process on port 8080...

for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8080 ^| findstr LISTENING 2^>nul') do (
    echo Killing process with PID: %%a
    taskkill /F /PID %%a >nul 2>&1
)

echo Port 8080 is free. Starting application...
echo Swagger UI will open automatically when app is ready...
echo.

start "EatClub API" cmd /c "mvn clean spring-boot:run"

echo Waiting for application to start...
:waitloop
timeout /t 2 /nobreak >nul
curl -s -o nul http://localhost:8080/swagger-ui.html
if %errorlevel% neq 0 goto waitloop

echo Application started! Opening Swagger UI...
start "" http://localhost:8080/swagger-ui.html
