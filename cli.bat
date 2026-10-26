@echo off
cd /d "%~dp0"
if not exist "target\classes\com\pharmacy\cli\StandaloneConsoleApp.class" (
    echo Compiling classes...
    call "C:\Users\DELL\apache-maven-3.9.9\bin\mvn.cmd" compile
)
java -cp target\classes com.pharmacy.cli.StandaloneConsoleApp
pause
