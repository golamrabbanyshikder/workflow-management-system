@echo off
REM Batch script to load sample data into the Workflow Management System database
REM Usage: load-sample-data.bat [database_name] [username] [password] [host] [port]

REM Default values (can be overridden by command line arguments)
set DB_NAME=%1
if "%DB_NAME%"=="" set DB_NAME=postgres

set USERNAME=%2
if "%USERNAME%"=="" set USERNAME=postgres

set PASSWORD=%3
if "%PASSWORD%"=="" set PASSWORD=123456

set HOST=%4
if "%HOST%"=="" set HOST=localhost

set PORT=%5
if "%PORT%"=="" set PORT=5432

echo Loading sample data into Workflow Management System database...
echo Database: %DB_NAME%
echo Host: %HOST%:%PORT%
echo Username: %USERNAME%
echo.

REM Check if PostgreSQL client is installed
psql --version >nul 2>&1
if %errorlevel% neq 0 (
    echo Error: PostgreSQL client (psql) is not installed or not in PATH
    echo Please install PostgreSQL client tools and try again
    pause
    exit /b 1
)

REM Check if the SQL file exists
set SQL_FILE=sample-data.sql
if not exist "%SQL_FILE%" (
    echo Error: SQL file '%SQL_FILE%' not found in current directory
    echo Please run this script from the directory containing the SQL file
    pause
    exit /b 1
)

REM Set PGPASSWORD environment variable to avoid password prompt
set PGPASSWORD=%PASSWORD%

REM Execute the SQL script
echo Executing SQL script...
psql -h %HOST% -p %PORT% -U %USERNAME% -d %DB_NAME% -f "%SQL_FILE%"

REM Check the exit status
if %errorlevel% equ 0 (
    echo.
    echo ✅ Sample data loaded successfully!
    echo.
    echo You can now log in with the following credentials:
    echo Username: admin
    echo Password: password123
    echo.
    echo For a complete list of sample users, check the README.md file
) else (
    echo.
    echo ❌ Failed to load sample data
    echo Please check the error messages above and fix any issues
    pause
    exit /b 1
)

REM Clear the password environment variable
set PGPASSWORD=

pause