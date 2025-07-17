#!/bin/bash

# Check if an argument was provided
if [ $# -ne 1 ]; then
    echo "Usage: $0 <directory>"
    exit 1
fi
DIR=$1

# Check if the argument is a valid directory
if [ ! -d "$DIR" ]; then
    echo "$DIR is not a valid directory."
    exit 1
fi

# Pause for 2 seconds
sleep 2

# Counters
owner_count=0
group_count=0
other_count=0

# List files in the directory
FILES=$(find "$DIR" -maxdepth 1 -type f)

for file in $FILES; do
    # Check read and write permissions for the owner
    if [ -r "$file" ] && [ -w "$file" ]; then
        ((owner_count++))
    fi
    
    # Check read and write permissions for the group
    if [ -g "$file" ] && [ -w "$file" ]; then
        ((group_count++))
    fi
    
    # Check if there are no permissions for others
    if [ ! -r "$file" ] && [ ! -w "$file" ] && [ ! -x "$file" ]; then
        ((other_count++))
    fi
done

# Display the results
echo "Files with read and write permissions for the owner: $owner_count"
echo "Files with read and write permissions for the group: $group_count"
echo "Files with no permissions for others: $other_count"
