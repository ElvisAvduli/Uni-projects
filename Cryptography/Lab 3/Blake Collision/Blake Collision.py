import hashlib

def H(x):
    if isinstance(x, str):
        x = x.encode('utf-8')   
    md5 = hashlib.md5(x).digest()
    return md5[:2]


def find_collision():
    print("Searching for a collision...") 
    seen_hashes = {} 
    counter = 0
    while True:
        current_input = str(counter)  
        hash_val = H(current_input)
        if hash_val in seen_hashes:
            previous_input = seen_hashes[hash_val]
            # Collision found!
            print(f"\n--- Collision Found! ---")
            print(f"Input 1: '{previous_input}'")
            print(f"Input 2: '{current_input}'")
            print(f"Truncated Hash (Hex): {hash_val.hex()}")
            print(f"Attempts needed: {counter}")
            return
        
        seen_hashes[hash_val] = current_input
        counter += 1

if __name__ == "__main__":
    find_collision()