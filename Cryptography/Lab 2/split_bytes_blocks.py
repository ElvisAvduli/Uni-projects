def split_into_byte_blocks(s: str, block_size: int = 16, encoding: str = "utf-8"):

    # Convert the string to bytes
    b = s.encode(encoding)

    # Compute padding length
    pad_len = block_size - (len(b) % block_size)
    if pad_len == 0:
        pad_len = block_size  # always pad, even if multiple of block_size

    # Apply PKCS#7 padding
    b += bytes([pad_len]) * pad_len

    # Split into blocks
    blocks = [b[i:i+block_size] for i in range(0, len(b), block_size)]
    return blocks

if __name__ == "__main__":
    s = "CryptographyIsFunAndIsDifferentThanCryptocurrency"
    blocks = split_into_byte_blocks(s, 16)
    print(f"Original string ({len(s)} chars):\n{s}\n")
    print(f"Total bytes (with padding): {len(b''.join(blocks))}\n")
    for i, blk in enumerate(blocks, 1):
        print(f"Block {i:02d}: {len(blk)} bytes | hex: {blk.hex()} | text: {blk.decode('utf-8', 'replace')!r}")
