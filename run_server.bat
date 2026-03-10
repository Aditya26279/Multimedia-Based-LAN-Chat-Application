@echo off
if not exist ChatServer.jar (
    echo [ERROR] ChatServer.jar not found! Please run build.bat first.
    pause
    exit /b
)
start javaw -jar ChatServer.jar
