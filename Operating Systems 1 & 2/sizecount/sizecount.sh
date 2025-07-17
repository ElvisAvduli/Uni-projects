#!/bin/bash

# Check if the number of arguments is 1
if [ "$#" -ne 1 ]; then
    echo "Usage: $0 <integer number>"
    exit 1
fi

# Check if the argument is an integer
if ! [[ "$1" =~ ^[0-9]+$ ]]; then
    echo "The argument must be an integer"
    exit 1
fi

# Assign the value of the argument to the variable HOUR
HOUR="$1"

# Check if the number is greater than 0 and less than or equal to 24
if [ "$HOUR" -le 0 ] || [ "$HOUR" -gt 24 ]; then
    echo "The number must be greater than 0 and less than or equal to 24"
    exit 1
fi

# Find files created at the hour specified by the number
FILES=$(find . -type f -newermt "$(date +%Y-%m-%d) $HOUR:00:00" ! -newermt "$(date +%Y-%m-%d) $HOUR:59:59")

# Check if files were found
if [ -z "$FILES" ]; then
    echo "No files found created during the hour $HOUR"
    exit 0
fi

# Save the names of the files to the file timefile
echo "$FILES" > timefile

echo "Files have been saved to timefile"
