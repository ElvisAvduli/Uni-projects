import secrets

def one_time_pad_encode(plaintext):
    plaintext = ''.join(c for c in plaintext.upper() if c.isalpha())
    # Generate random key of same length
    key = ''.join(chr(secrets.randbelow(26) + 65) for _ in range(len(plaintext)))
    
    # Encrypt using modular addition
    ciphertext = ''
    for p_char, k_char in zip(plaintext, key):
        p_val = ord(p_char) - 65
        k_val = ord(k_char) - 65
        c_val = (p_val + k_val) % 26
        ciphertext += chr(c_val + 65)
    
    return ciphertext, key

# Running commands

surname = "Avduli"
cipher, key = one_time_pad_encode(surname)
    
print(f"Plaintext:  {surname}")
print(f"Key:        {key}")
print(f"Ciphertext: {cipher}")
    