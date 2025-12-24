#!/bin/bash

# Shell script to load sample data into the Workflow Management System database
# Usage: ./load-sample-data.sh [database_name] [username] [password] [host] [port]

# Default values (can be overridden by command line arguments)
DB_NAME=${1:-"postgres"}
USERNAME=${2:-"postgres"}
PASSWORD=${3:-"123456"}
HOST=${4:-"localhost"}
PORT=${5:-"5432"}

echo "Loading sample data into Workflow Management System database..."
echo "Database: $DB_NAME"
echo "Host: $HOST:$PORT"
echo "Username: $USERNAME"
echo ""

# Check if PostgreSQL client is installed
if ! command -v psql &> /dev/null; then
    echo "Error: PostgreSQL client (psql) is not installed or not in PATH"
    echo "Please install PostgreSQL client tools and try again"
    exit 1
fi

# Check if the SQL file exists
SQL_FILE="sample-data.sql"
if [ ! -f "$SQL_FILE" ]; then
    echo "Error: SQL file '$SQL_FILE' not found in current directory"
    echo "Please run this script from the directory containing the SQL file"
    exit 1
fi

# Set PGPASSWORD environment variable to avoid password prompt
export PGPASSWORD=$PASSWORD

# Execute the SQL script
echo "Executing SQL script..."
psql -h $HOST -p $PORT -U $USERNAME -d $DB_NAME -f "$SQL_FILE"

# Check the exit status
if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Sample data loaded successfully!"
    echo ""
    echo "You can now log in with the following credentials:"
    echo "Username: admin"
    echo "Password: password123"
    echo ""
    echo "For a complete list of sample users, check the README.md file"
else
    echo ""
    echo "❌ Failed to load sample data"
    echo "Please check the error messages above and fix any issues"
    exit 1
fi

# Clear the password environment variable
unset PGPASSWORD