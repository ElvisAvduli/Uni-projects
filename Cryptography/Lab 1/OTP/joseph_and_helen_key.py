from pwn import xor
# Ciphertexts in hex
c1 = bytes.fromhex('335855443c194a09135f10083e094059064255442c04131f125e')
c2 = bytes.fromhex('2f59102c3a075617461079443704431c47495f117f0e5d130849101037024059045f45162c0e')

# Known plaintexts (cribs)
crib = b'Hi Helen!'
crib2= b'Hi Joseph!'

# Try decrypting with the crib
print(xor(crib, c1))
print(xor(crib, c2)) 

# Try decrypting with the second crib
print(xor(crib2, c1))
print(xor(crib2, c2)) 

# From the output, we can deduce the key
# The key is 'g00d_k3y'
key=b'g00d_k3y'

# Decrypt both ciphertexts with the key
print(xor(c1,key))
print(xor(c2,key))
