import numpy as np

# Ορισμός παραμέτρων
states = [1, 2, 3]
actions = ['k', 'l']
rewards = {1: -1, 2: -2, 3: 0}
gamma = 1.0  # Χωρίς προεξόφληση
theta = 0.0001  # Κριτήριο σύγκλισης

# Συναρτήσεις μετάβασης
def T(s, a, s_next):
    if s == 3:  # Τερματική κατάσταση
        return 1.0 if s_next == 3 else 0.0
    if s == 1:
        if a == 'k':
            return 0.8 if s_next == 2 else 0.2 if s_next == 1 else 0.0
        elif a == 'l':
            return 0.1 if s_next == 3 else 0.9 if s_next == 1 else 0.0
    elif s == 2:
        if a == 'k':
            return 0.8 if s_next == 1 else 0.2 if s_next == 2 else 0.0
        elif a == 'l':
            return 0.1 if s_next == 3 else 0.9 if s_next == 2 else 0.0
    return 0.0

# Αρχικοποίηση τιμών
V = {1: 0, 2: 0, 3: 0}

# Value Iteration
iteration = 0
while True:
    delta = 0
    new_V = {}
    for s in [1, 2]:  # Μόνο για μη τερματικές καταστάσεις
        max_value = -float('inf')
        for a in actions:
            value = rewards[s] + gamma * sum(T(s, a, s_next) * V[s_next] for s_next in states)
            if value > max_value:
                max_value = value
        new_V[s] = max_value
        delta = max(delta, abs(new_V[s] - V[s]))
    
    # Ενημέρωση τιμών
    for s in [1, 2]:
        V[s] = new_V[s]
    
    iteration += 1
    print(f"Iteration {iteration}: V(1)={V[1]:.4f}, V(2)={V[2]:.4f}")
    
    if delta < theta:
        break

# Εξαγωγή βέλτιστης πολιτικής
# Αρχικοποίηση
policy = {1: 'l', 2: 'l'}
V = {1: 0, 2: 0, 3: 0}
gamma = 1.0
theta = 0.0001
iteration = 0

while True:
    # Policy Evaluation
    print(f"\nPolicy Iteration {iteration + 1}")
    print(f"Current Policy: π(1)={policy[1]}, π(2)={policy[2]}")
    
    while True:
        delta = 0
        new_V = {}
        for s in [1, 2]:
            a = policy[s]
            value = rewards[s] + gamma * sum(T(s, a, s_next) * V[s_next] for s_next in states)
            new_V[s] = value
            delta = max(delta, abs(new_V[s] - V[s]))
        
        for s in [1, 2]:
            V[s] = new_V[s]
        
        print(f"Evaluation Step: V(1)={V[1]:.4f}, V(2)={V[2]:.4f}")
        if delta < theta:
            break
    
    # Policy Improvement
    policy_stable = True
    for s in [1, 2]:
        old_action = policy[s]
        best_action = None
        best_value = -float('inf')
        
        for a in actions:
            value = rewards[s] + gamma * sum(T(s, a, s_next) * V[s_next] for s_next in states)
            if value > best_value:
                best_value = value
                best_action = a
        
        if best_action != old_action:
            policy_stable = False
            policy[s] = best_action
    
    iteration += 1
    
    print(f"Improved Policy: π(1)={policy[1]}, π(2)={policy[2]}")
    if policy_stable:
        break

print("\nFinal Results (Policy Iteration):")
print(f"V(1) = {V[1]:.4f}")
print(f"V(2) = {V[2]:.4f}")
print(f"Optimal Policy: π(1)={policy[1]}, π(2)={policy[2]}")