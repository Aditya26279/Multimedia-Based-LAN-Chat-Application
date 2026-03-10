@echo off
if not exist ChatClient.jar (
    echo [ERROR] ChatClient.jar not found! Please run build.bat first.
    pause
    exit /b
)
start javaw -jar ChatClient.jar
