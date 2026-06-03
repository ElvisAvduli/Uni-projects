def compute_lcg_period():
    # LCG parameters
    x0, a, b, m = 14, 193, 78, 1337
    
    # Generate sequence until repetition
    seen = {}
    current = x0
    step = 0
    
    while current not in seen:
        seen[current] = step
        step += 1
        current = (a * current + b) % m

    period = step - seen[current]
    return period, len(seen)

period, distinct_values = compute_lcg_period()
print(f"Period: {period}")
print(f"Distinct values: {distinct_values}")