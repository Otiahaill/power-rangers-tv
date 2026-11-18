@echo off
set GRADLE_HOME=%~dp0..\gradle
if "%GRADLE_HOME%"=="" set GRADLE_HOME=%HOME%\gradle
if not exist "%GRADLE_HOME%\bin\gradle.bat" (
    echo Gradle not found at %GRADLE_HOME%
    exit /b 1
)
"%GRADLE_HOME%\bin\gradle.bat" %*
