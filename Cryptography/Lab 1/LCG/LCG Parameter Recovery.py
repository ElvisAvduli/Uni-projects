def solve_lcg():
    # Given values
    m = 2**127 - 1
    x0 = 51357953340700960539503091666792115306
    x1 = 60122000323363606827929954412280678934
    x2 = 45876883472656003592807994096015528525
    
    # Compute differences
    d1 = (x1 - x0) % m
    d2 = (x2 - x1) % m
    
    # Compute modular inverse of d1 using Fermat's Little Theorem
    # Since m is prime: d1^(-1) ≡ d1^(m-2) mod m
    inv_d1 = pow(d1, m-2, m)
    
    # Compute a
    a = (d2 * inv_d1) % m

    # Compute b
    b = (x1 - a * x0) % m
    return a, b
    
    
a, b = solve_lcg()
print("\n" + "="*50)
print("SOLUTION:")
print(f"a = {a}")
print(f"b = {b}")