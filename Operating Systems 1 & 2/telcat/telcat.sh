#!/bin/bash

# The file that contains the phone directory
FILENAME="katalogos"

# Function that displays the usage of the script
usage() {
    echo "Usage:"
    echo "telcat -a : Add a new entry"
    echo "telcat -l : List all entries"
    echo "telcat -s <column> : Sort entries by column (1: name, 2: surname, 3: city, 4: phone)"
    echo "telcat -c <keyword> : Search entries by keyword"
    echo "telcat -d <keyword> : Delete entries containing keyword"
    echo "telcat -n : Count the number of entries"
    exit 1
}

# Function to add a new entry to the directory
add_entry() {
    # Prompt the user to enter information
    read -p "Enter name: " name
    read -p "Enter surname: " surname
    read -p "Enter city: " city
    read -p "Enter phone: " phone
    # Add the new entry to the file
    echo "$name $surname $city $phone" >> "$FILENAME"
    echo "Entry added."
}

# Function to list all entries in the directory
list_entries() {
    # Check if the file exists
    if [[ ! -f "$FILENAME" ]]; then
        echo "No entries found."
        return
    fi
    # Display the contents of the file
    cat "$FILENAME"
}

# Function to sort entries by a specific column
sort_entries() {
    local column=$1
    # Check if the file exists
    if [[ ! -f "$FILENAME" ]]; then
        echo "No entries found."
        return
    fi
    # Sort the file based on the specified column
    sort -k"$column" "$FILENAME"
}

# Function to search for entries containing a keyword
search_entries() {
    local keyword=$1
    # Check if the file exists
    if [[ ! -f "$FILENAME" ]]; then
        echo "No entries found."
        return
    fi
    # Search and display entries containing the keyword
    grep -i "$keyword" "$FILENAME" || echo "No entries found with the given keyword."
}

# Function to delete entries containing a keyword
delete_entries() {
    local keyword=$1
    # Check if the file exists
    if [[ ! -f "$FILENAME" ]]; then
        echo "No entries found."
        return
    fi
    # Create a temporary file without entries containing the keyword
    grep -vi "$keyword" "$FILENAME" > "$FILENAME.tmp" && mv "$FILENAME.tmp" "$FILENAME"
    echo "Entries deleted (if any contained the keyword)."
}

# Function to count the number of entries in the directory
count_entries() {
    # Check if the file exists
    if [[ ! -f "$FILENAME" ]]; then
        echo "No entries found."
        return
    fi
    # Count and display the number of entries
    local count=$(wc -l < "$FILENAME")
    echo "Number of entries: $count"
}

# Check the input parameters
if [[ $# -lt 1 ]]; then
    usage
fi

# Process the input parameters
option=$1

case $option in
    -a)
        add_entry
        ;;
    -l)
        list_entries
        ;;
    -s)
        if [[ $# -ne 2 ]]; then
            usage
        fi
        sort_entries "$2"
        ;;
    -c)
        if [[ $# -ne 2 ]]; then
            usage
        fi
        search_entries "$2"
        ;;
    -d)
        if [[ $# -ne 2 ]]; then
            usage
        fi
        delete_entries "$2"
        ;;
    -n)
        count_entries
        ;;
    *)
        usage
        ;;
esac
