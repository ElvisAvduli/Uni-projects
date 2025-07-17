
---

## 📁 `telcat` 

```markdown
# telcat - Simple Phonebook Manager

## 🧾 Overview
`telcat` is a terminal-based phonebook manager in Bash that lets you add, list, sort, search, delete, and count contact entries stored in a local file called `katalogos`.

## 📋 Features

- `-a`: Add a new entry (name, surname, city, phone)
- `-l`: List all entries
- `-s <column>`: Sort by column (1: name, 2: surname, 3: city, 4: phone)
- `-c <keyword>`: Search entries by keyword
- `-d <keyword>`: Delete entries containing a keyword
- `-n`: Count total entries

## 🛠️ Usage Examples

```bash
./telcat.sh -a           # Add entry
./telcat.sh -l           # List entries
./telcat.sh -s 2         # Sort by surname
./telcat.sh -c Athens    # Search for "Athens"
./telcat.sh -d John      # Delete entries containing "John"
./telcat.sh -n           # Count entries
