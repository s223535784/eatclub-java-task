@echo off
echo Checking for process on port 8080...

set found=0
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8080 ^| findstr LISTENING 2^>nul') do (
    echo Stopping application (PID: %%a)...
    taskkill /F /PID %%a >nul 2>&1
    set found=1
)

if %found%==0 (
    echo No application running on port 8080.
) else (
    echo Application stopped successfully.
)
