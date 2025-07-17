
---

## 📁 `mygrep`

```markdown
# mygrep - File Permission Analyzer

## 🧾 Overview
Despite the name, this script checks **file permissions** in a specified directory. It counts:
- Files with read/write permission for **owner**
- Files with read/write for **group**
- Files with **no permissions** for others

## 📋 Features
- Validates directory input
- Uses `find` and permission checks
- Displays three permission-based counts

## 🛠️ Usage

```bash
chmod +x mygrep.sh
./mygrep.sh /path/to/directory
