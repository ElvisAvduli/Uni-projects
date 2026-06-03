from Crypto.Cipher import AES
from Crypto.Random import get_random_bytes
import base64

BLOCK_SIZE = 16

def pad_pkcs7(data: bytes, block_size: int = BLOCK_SIZE) -> bytes:
    # Apply PKCS#7 padding to `data` so its length is a multiple of block_size.
    pad_len = block_size - (len(data) % block_size)
    if pad_len == 0:
        pad_len = block_size
    return data + bytes([pad_len]) * pad_len


def unpad_pkcs7(data: bytes, block_size: int = BLOCK_SIZE) -> bytes:
    # Remove PKCS#7 padding, raising ValueError on invalid padding.
    if not data or len(data) % block_size != 0:
        raise ValueError("Invalid padded data length")
    pad_len = data[-1]
    if pad_len < 1 or pad_len > block_size:
        raise ValueError("Invalid padding byte value")
    if data[-pad_len:] != bytes([pad_len]) * pad_len:
        raise ValueError("Invalid padding bytes")
    return data[:-pad_len]


def xor_bytes(a: bytes, b: bytes) -> bytes:
    # XOR two equal-length byte sequences.
    return bytes(x ^ y for x, y in zip(a, b))


def aes_ecb_encrypt_block(ecb_cipher: AES, block: bytes) -> bytes:
    # Encrypt a single 16-byte block using an AES-ECB cipher object.
    if len(block) != BLOCK_SIZE:
        raise ValueError("Block must be exactly 16 bytes")
    return ecb_cipher.encrypt(block)


def aes_ecb_decrypt_block(ecb_cipher: AES, block: bytes) -> bytes:
    # Decrypt a single 16-byte block using an AES-ECB cipher object.
    if len(block) != BLOCK_SIZE:
        raise ValueError("Block must be exactly 16 bytes")
    return ecb_cipher.decrypt(block)


def aes_cbc_encrypt_ecb_based(key: bytes, iv: bytes, plaintext: bytes) -> bytes:
    if len(iv) != BLOCK_SIZE:
        raise ValueError("IV must be 16 bytes")
    if len(key) not in (16, 24, 32):
        raise ValueError("Key must be 128/192/256 bits (16/24/32 bytes)")

    # Prepare ECB cipher object (we only use its block encrypt function)
    ecb = AES.new(key, AES.MODE_ECB)

    # Pad plaintext
    padded = pad_pkcs7(plaintext, BLOCK_SIZE)

    ciphertext_blocks = []
    prev = iv
    for i in range(0, len(padded), BLOCK_SIZE):
        plain_block = padded[i:i+BLOCK_SIZE]
        xored = xor_bytes(plain_block, prev)
        cipher_block = aes_ecb_encrypt_block(ecb, xored)
        ciphertext_blocks.append(cipher_block)
        prev = cipher_block
    return b"".join(ciphertext_blocks)


def aes_cbc_decrypt_ecb_based(key: bytes, iv: bytes, ciphertext: bytes) -> bytes:
    if len(iv) != BLOCK_SIZE:
        raise ValueError("IV must be 16 bytes")
    if len(ciphertext) % BLOCK_SIZE != 0:
        raise ValueError("Ciphertext must be multiple of 16 bytes")
    if len(key) not in (16, 24, 32):
        raise ValueError("Key must be 128/192/256 bits (16/24/32 bytes)")

    ecb = AES.new(key, AES.MODE_ECB)

    plaintext_blocks = []
    prev = iv
    for i in range(0, len(ciphertext), BLOCK_SIZE):
        cipher_block = ciphertext[i:i+BLOCK_SIZE]
        decrypted = aes_ecb_decrypt_block(ecb, cipher_block)
        plain_block = xor_bytes(decrypted, prev)
        plaintext_blocks.append(plain_block)
        prev = cipher_block

    padded_plaintext = b"".join(plaintext_blocks)
    return unpad_pkcs7(padded_plaintext, BLOCK_SIZE)


# ---------------------------
# Example run (if executed)
# ---------------------------
if __name__ == "__main__":
    # Example inputs
    key = get_random_bytes(16)  
    iv = get_random_bytes(16)   
    msg = b"N0tPresl3y here"

    print("Plaintext:", msg)
    print("Key (hex):", key.hex())
    print("IV  (hex):", iv.hex())

    # Encrypt
    ct = aes_cbc_encrypt_ecb_based(key, iv, msg)
    print("\nCiphertext (hex):", ct.hex())
    print("Ciphertext (base64):", base64.b64encode(ct).decode())

    # Decrypt (using same IV)
    pt = aes_cbc_decrypt_ecb_based(key, iv, ct)
    print("\nDecrypted plaintext:", pt)
    assert pt == msg
    print("Decryption successful.")
