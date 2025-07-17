# sizecount - Hour-Based File Finder

## 🧾 Overview
This Bash script finds files created during a specific hour of the **current day** and saves their names to a file named `timefile`.

## 📋 Features
- Input validation for hour (must be 1–24)
- Uses `find` to locate files based on creation time
- Outputs file list to `timefile`

## 🛠️ Usage

```bash
chmod +x sizecount.sh
./sizecount.sh 13
