# Vigenère Cipher - Bash Implementation

## 🧾 Overview
This Bash script implements the **Vigenère cipher**, a method of encrypting alphabetic text using a keyword. It allows users to either encrypt or decrypt messages interactively from the terminal.

## 📋 Features

- Encryption of plaintext using a keyword
- Decryption of ciphertext using the same keyword
- Handles case conversion automatically (all inputs are treated as uppercase)
- Uses ASCII-based arithmetic for character shifting

## 🔐 Cipher Logic

- The script loops through each letter of the input.
- Each letter is shifted by the corresponding letter in the key (cyclically).
- It uses modulo 26 arithmetic to stay within the A–Z range.

## 🛠️ How to Use

1. Make the script executable:
   ```bash
   chmod +x vigenere.sh
