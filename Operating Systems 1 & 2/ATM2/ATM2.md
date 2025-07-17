
---

## 📁 `ATM2` 

```markdown
# ATM2 - Simulated ATM Terminal

## 🧾 Overview
This Bash script simulates an ATM interface that allows users to:
- Check balance
- Withdraw funds (up to €1000 limit)
- Deposit funds

All transactions are temporary and reset after the script ends.

## 📋 Features
- Balance display
- Withdrawal with overdraft and limit protection
- Deposit support
- Floating-point math via `bc`

## 🛠️ Usage

```bash
chmod +x atm2.sh
./atm2.sh
