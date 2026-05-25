@echo off
echo =======================================================================
echo     Smart Retail Forge - Interactive Microservices Bootstrapper
echo =======================================================================
echo.
echo This script boots all 8 microservices.
echo Please ensure that MySQL, Redis, Kafka, and Keycloak are running on default ports.
echo.

echo [1/8] Starting Eureka Discovery Server (Port 8761)...
start "Eureka Discovery Server [8761]" cmd /k "gradlew.bat :eureka-server:bootRun"
echo Eureka Server has been triggered in a new window.
echo Wait until Eureka dashboard (http://localhost:8761) is fully active.
echo.
choice /c c /n /m "Press [C] to continue and boot Config Server..." > nul

echo.
echo [2/8] Starting Central Config Server (Port 8888)...
start "Central Config Server [8888]" cmd /k "gradlew.bat :config-server:bootRun"
echo Config Server has been triggered in a new window.
echo Wait until Config Server is healthy and readable at http://localhost:8888/actuator/health.
echo.
choice /c c /n /m "Press [C] to continue and boot API Gateway..." > nul

echo.
echo [3/8] Starting API Gateway (Port 8081)...
start "API Gateway [8081]" cmd /k "gradlew.bat :api-gateway:bootRun"
echo API Gateway has been triggered in a new window.
echo Wait for Gateway routing services to bind.
echo.

echo -----------------------------------------------------------------------
echo Infrastructure services (Eureka, Config, Gateway) have been started!
echo -----------------------------------------------------------------------
echo How would you like to start the remaining 5 core microservices?
echo.
echo [1] Start ALL remaining microservices at once (parallel)
echo [2] Start remaining microservices ONE-BY-ONE (requires individual confirmation)
echo.

choice /c 12 /n /m "Select your starting method (1 or 2): "
if errorlevel 2 goto onebyone
if errorlevel 1 goto allatonce

:allatonce
echo.
echo Starting remaining microservices in parallel...
echo.
echo Starting Product Catalog Service (Port 8082)...
start "Product Catalog Service [8082]" cmd /k "gradlew.bat :product-service:bootRun"

echo Starting Inventory Control Service (Port 8083)...
start "Inventory Control Service [8083]" cmd /k "gradlew.bat :inventory-service:bootRun"

echo Starting POS Billing Service (Port 8084)...
start "POS Billing Service [8084]" cmd /k "gradlew.bat :billing-service:bootRun"

echo Starting Notification Service (Port 8085)...
start "Notification Service [8085]" cmd /k "gradlew.bat :notification-service:bootRun"

echo Starting Real-Time Analytics Service (Port 8086)...
start "Real-Time Analytics Service [8086]" cmd /k "gradlew.bat :analytics-service:bootRun"
goto end

:onebyone
echo.
echo Starting remaining microservices one-by-one...

echo.
choice /c yn /n /m "Start Product Catalog Service (Port 8082)? [Y/N]: "
if errorlevel 2 goto skip_product
start "Product Catalog Service [8082]" cmd /k "gradlew.bat :product-service:bootRun"
echo Product Catalog Service triggered.
:skip_product

echo.
choice /c yn /n /m "Start Inventory Control Service (Port 8083)? [Y/N]: "
if errorlevel 2 goto skip_inventory
start "Inventory Control Service [8083]" cmd /k "gradlew.bat :inventory-service:bootRun"
echo Inventory Control Service triggered.
:skip_inventory

echo.
choice /c yn /n /m "Start POS Billing Service (Port 8084)? [Y/N]: "
if errorlevel 2 goto skip_billing
start "POS Billing Service [8084]" cmd /k "gradlew.bat :billing-service:bootRun"
echo POS Billing Service triggered.
:skip_billing

echo.
choice /c yn /n /m "Start Notification Service (Port 8085)? [Y/N]: "
if errorlevel 2 goto skip_notification
start "Notification Service [8085]" cmd /k "gradlew.bat :notification-service:bootRun"
echo Notification Service triggered.
:skip_notification

echo.
choice /c yn /n /m "Start Real-Time Analytics Service (Port 8086)? [Y/N]: "
if errorlevel 2 goto skip_analytics
start "Real-Time Analytics Service [8086]" cmd /k "gradlew.bat :analytics-service:bootRun"
echo Real-Time Analytics Service triggered.
:skip_analytics

goto end

:end
echo.
echo =======================================================================
echo     Microservice triggering sequence completed!
echo =======================================================================
echo You can monitor service registration at the Eureka Console:
echo      http://localhost:8761
echo.
pause
