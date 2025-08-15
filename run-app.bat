@echo off
echo Starting Sportal Application...
echo.

echo Setting JAVA_HOME...
set JAVA_HOME=C:\Program Files\Java\jdk-21

echo Compiling and running the application...
call mvnw.cmd spring-boot:run

pause
