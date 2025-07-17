import numpy as np

# Δεδομένα: Τετραγωνικά μέτρα (x) και Αξία σε χιλιάδες ευρώ (y)
x = np.array([50, 60, 70, 80, 90, 100])
y = np.array([100, 150, 175, 220, 280, 350])

# 1. Μέθοδος Gradient Descent
def gradient_descent(x, y, learning_rate=0.0001, epochs=1000):
    N = len(x)
    w0, w1 = 0, 0  # Αρχικές τιμές
    
    for _ in range(epochs):
        y_pred = w0 + w1 * x
        error = y_pred - y
        
        # Υπολογισμός gradients
        grad_w0 = np.sum(error) / N
        grad_w1 = np.sum(error * x) / N
        
        # Ενημέρωση βαρών
        w0 -= learning_rate * grad_w0
        w1 -= learning_rate * grad_w1
    
    return w0, w1

# 2. Κανονική Εξίσωση (Closed-form solution)
def normal_equation(x, y):
    X = np.vstack([np.ones(len(x)), x]).T
    w = np.linalg.inv(X.T @ X) @ X.T @ y
    return w[0], w[1]  # w0, w1

# Εκτέλεση Gradient Descent
w0_gd, w1_gd = gradient_descent(x, y)
print(f"Gradient Descent: ŷ = {w0_gd:.2f} + {w1_gd:.2f} * x")

# Εκτέλεση Κανονικής Εξίσωσης
w0_ne, w1_ne = normal_equation(x, y)
print(f"Κανονική Εξίσωση: ŷ = {w0_ne:.2f} + {w1_ne:.2f} * x")

# Πρόβλεψη για ΤΜ = 120 και ΤΜ = 75
def predict(x_val, w0, w1):
    return w0 + w1 * x_val

print("\nΠροβλέψεις (Gradient Descent):")
print(f"120 ΤΜ → {predict(120, w0_gd, w1_gd):.2f} χιλιάδες €")
print(f"75 ΤΜ → {predict(75, w0_gd, w1_gd):.2f} χιλιάδες €")

print("\nΠροβλέψεις (Κανονική Εξίσωση):")
print(f"120 ΤΜ → {predict(120, w0_ne, w1_ne):.2f} χιλιάδες €")
print(f"75 ΤΜ → {predict(75, w0_ne, w1_ne):.2f} χιλιάδες €")