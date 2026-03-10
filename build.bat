@echo off
echo =========================================
echo LAN Chat Application Build Script
echo =========================================

echo.
echo Cleaning up old build files...
if exist out rmdir /s /q out
mkdir out

echo.
echo Compiling Java source files...
javac -d out src\com\chat\common\*.java src\com\chat\server\*.java src\com\chat\client\*.java
if %errorlevel% neq 0 (
    echo [ERROR] Compilation failed!
    pause
    exit /b %errorlevel%
)

echo.
echo Creating ChatServer.jar...
jar cfe ChatServer.jar com.chat.server.ServerWindow -C out .
if %errorlevel% neq 0 (
    echo [ERROR] Failed to create ChatServer.jar!
    pause
    exit /b %errorlevel%
)

echo.
echo Creating ChatClient.jar...
jar cfe ChatClient.jar com.chat.client.ClientWindow -C out .
if %errorlevel% neq 0 (
    echo [ERROR] Failed to create ChatClient.jar!
    pause
    exit /b %errorlevel%
)

echo.
echo =========================================
echo Build complete successfully!
echo Executables: ChatServer.jar, ChatClient.jar
echo =========================================
pause
