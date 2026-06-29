@REM Licensed to the Apache Software Foundation (ASF)
@REM https://www.apache.org/licenses/LICENSE-2.0

@echo off
setlocal

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%

if exist "%APP_HOME%\.mvn\wrapper\maven-wrapper.jar" (
    java -cp "%APP_HOME%\.mvn\wrapper\maven-wrapper.jar" org.apache.maven.wrapper.MavenWrapperMain %*
    exit /b %errorlevel%
) else (
    echo Error: Maven wrapper JAR not found. Please run setup.
    exit /b 1
)
endlocal
