@echo off
echo =======================================================================
echo     Smart Retail Forge - Interactive Microservices Bootstrapper
echo =======================================================================
echo.
echo This script boots all 8 microservices.
echo Please ensure that MySQL, Redis, Kafka, and Keycloak are running on default ports.
echo.

echo [1/8] Start Eureka Discovery Server (Port 8761)?
choice /c yn /n /m "Start Eureka Discovery Server? [Y/N]: "
if errorlevel 2 goto skip_eureka
start "Eureka Discovery Server [8761]" cmd /k "gradlew.bat :eureka-server:bootRun"
echo Eureka Discovery Server triggered.
:skip_eureka

echo.
echo [2/8] Start Central Config Server (Port 8888)?
choice /c yn /n /m "Start Central Config Server? [Y/N]: "
if errorlevel 2 goto skip_config
start "Central Config Server [8888]" cmd /k "gradlew.bat :config-server:bootRun"
echo Central Config Server triggered.
:skip_config

echo.
echo [3/8] Start API Gateway (Port 8081)?
choice /c yn /n /m "Start API Gateway? [Y/N]: "
if errorlevel 2 goto skip_gateway
start "API Gateway [8081]" cmd /k "gradlew.bat :api-gateway:bootRun"
echo API Gateway triggered.
:skip_gateway

echo.
echo -----------------------------------------------------------------------
echo Infrastructure services (Eureka, Config, Gateway) have been processed.
echo -----------------------------------------------------------------------
echo How would you like to start the remaining 5 core microservices?
echo.
echo [1] Start ALL remaining microservices at once (parallel)
echo [2] Start remaining microservices ONE-BY-ONE (requires individual confirmation)
echo [3] Skip remaining and Exit
echo.

choice /c 123 /n /m "Select your starting method (1, 2 or 3): "
if errorlevel 3 goto end
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
