@echo off
echo ==============================================================
echo  Launching PureMed Pharmacy Management System
echo  In-Memory Java Store ^| Zero Database Required
echo ==============================================================

if exist "C:\Users\DELL\apache-maven-3.9.9\bin\mvn.cmd" (
    call "C:\Users\DELL\apache-maven-3.9.9\bin\mvn.cmd" spring-boot:run
) else (
    call mvn spring-boot:run
)
pause
