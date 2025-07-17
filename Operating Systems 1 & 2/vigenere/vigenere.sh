#!/bin/bash

# This function encrypts the given plaintext using the Vigenère cipher
encrypt() 
{
  # Read the plaintext and the key from the user
  read -p "Enter the plaintext: " plaintext
  read -p "Enter the key: " key

  # Convert the plaintext and key to all upper case
  plaintext=$(echo $plaintext | tr '[:lower:]' '[:upper:]')
  key=$(echo $key | tr '[:lower:]' '[:upper:]')

  # Initialize the ciphertext
  ciphertext=""

  # Loop through each character in the plaintext
  for (( i=0; i<${#plaintext}; i++ )); do
    # Get the ASCII code of the current character
    charCode=$(printf "%d" "'${plaintext:i:1}")

    # Shift the character code based on the corresponding character in the key
    keyIndex=$(($i % ${#key}))
    keyCharCode=$(printf "%d" "'${key:keyIndex:1}")
    shiftedCharCode=$((($charCode - 65) + ($keyCharCode - 65)) % 26 + 65)
    #charCode and keyCharCode are ASCII numbers converted to numbers from 1 to 26

    # Add the shifted character to the ciphertext
    ciphertext+=$(printf \\$(printf "%o" $shiftedCharCode))
  done

  # Print the ciphertext
  echo "Ciphertext: $ciphertext"
}


# This function decrypts the given ciphertext using the Vigenère cipher
decrypt() 
{
  # Read the ciphertext and the key from the user
  read -p "Enter the ciphertext: " ciphertext
  read -p "Enter the key: " key

  # Convert the ciphertext and key to all upper case
  ciphertext=$(echo $ciphertext | tr '[:lower:]' '[:upper:]')
  key=$(echo $key | tr '[:lower:]' '[:upper:]')

  # Initialize the plaintext
  plaintext=""

  # Loop through each character in the ciphertext
  for (( i=0; i<${#ciphertext}; i++ )); 
  do
    # Get the ASCII code of the current character
    charCode=$(printf "%d" "'${ciphertext:i:1}")

    # Shift the character code based on the corresponding character in the key
    keyIndex=$(($i % ${#key}))
    keyCharCode=$(printf "%d" "'${key:keyIndex:1}")
    shiftedCharCode=$((($charCode - 65) - ($keyCharCode - 65)) % 26 + 65)

    # Add the shifted character to the plaintext
    plaintext+=$(printf \\$(printf "%o" $shiftedCharCode))
  done

  # Print the plaintext
  echo "Plaintext: $plaintext"
}


# Ask the user whether they want to encrypt or decrypt
read -p "Would you like to encrypt or decrypt? " choice

# Call the appropriate function based on the user's choice
if [ "$choice" == "encrypt" ]; then 
     encrypt
fi
if [ "$choice" == "decrypt" ]; then 
     decrypt
fi

