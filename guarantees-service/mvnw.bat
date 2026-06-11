@REM ----------------------------------------------------------------------------
@REM  Licensed to the Apache Software Foundation (ASF) under one
@REM  or more contributor license agreements.  See the NOTICE file
@REM  distributed with this work for additional information
@REM  regarding copyright ownership.  The ASF licenses this file
@REM  to you under the Apache License, Version 2.0 (the
@REM  "License"); you may not use this file except in compliance
@REM  with the License.  You may obtain a copy of the License at
@REM
@REM    https://www.apache.org/licenses/LICENSE-2.0
@REM
@REM  Unless required by applicable law or agreed to in writing,
@REM  software distributed under the License is distributed on an
@REM  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM  KIND, either express or implied.  See the License for the
@REM  specific language governing permissions and limitations
@REM  under the License.
@REM ----------------------------------------------------------------------------
@REM
@REM   Copyright (c) 2021, Apache Maven Project
@REM

@echo off
setlocal enabledelayedexpansion

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
@setlocal

set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%

@setlocal

set WRAPPER_JAR=%APP_HOME%\.mvn\wrapper\maven-wrapper.jar
if not "%MAVEN_PROJECTBASEDIR%"=="" (
    set MAVEN_PROJECTBASEDIR=%~dp0
    if NOT "%MAVEN_PROJECTBASEDIR:~-1%"=="\" (
        set MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR%\
    )
)

if exist %WRAPPER_JAR% (
    if "%MVNW_VERBOSE%" == "true" (
        echo Found %WRAPPER_JAR%
    )
) else (
    echo Error: JAVA_HOME is not set and no 'java' command could be found in your PATH.
    echo.
    echo Please set the JAVA_HOME variable in your environment to match the location of your Java installation.
    exit /b 1
)

setlocal enabledelayedexpansion
for /F "usebackq delims=" %%a in ("%WRAPPER_JAR%") do set WRAPPER_JAR=%%~fa
if "%MVNW_VERBOSE%" == "true" (
    echo Couldn't find %WRAPPER_JAR%, downloading it ...
    echo Downloading from: https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar
)

powershell -Command "&{"^
    "$webclient = new-object System.Net.WebClient;"^
    "if (-not ([string]::IsNullOrEmpty('%MVNW_USERNAME%') -and [string]::IsNullOrEmpty('%MVNW_PASSWORD%'))) {"^
    "$webclient.Credentials = new-object System.Net.NetworkCredential('%MVNW_USERNAME%', '%MVNW_PASSWORD%');"^
    "}"^
    "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; $webclient.DownloadFile('%WRAPPER_URL%', '%WRAPPER_JAR%')"^
    "}"
if "%MVNW_VERBOSE%" == "true" (
    echo Finished downloading %WRAPPER_JAR%
)

REM Run Maven using the wrapper
"%JAVA_HOME%\bin\java" ^
    -classpath "%WRAPPER_JAR%" ^
    "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" ^
    org.apache.maven.wrapper.MavenWrapperMain %*

if ERRORLEVEL 1 goto error
goto end

:error
set ERROR_CODE=1

:end
@endlocal & set ERROR_CODE=%ERROR_CODE%

if not "%FORK_MODE%"=="true" (
    exit /B %ERROR_CODE%
)

exit /B %ERROR_CODE%
