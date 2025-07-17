function calculate() {
    // Input elements
    const incomeInput = document.getElementById('income');
    const birthdateInput = document.getElementById('birthdate');
    const childrenInput = document.getElementById('children');

    // Get values
    const income = parseFloat(incomeInput.value);
    const birthdate = birthdateInput.value;
    const children = parseInt(childrenInput.value);

    // Clear previous error messages
    clearErrors();

    let hasError = false;

    // Validate Income
    if (isNaN(income) || income <= 0) {
        showError(incomeInput, "Please enter a valid positive income.");
        hasError = true;
    }

    // Validate Birthdate
    if (!birthdate) {
        showError(birthdateInput, "Please select your birthdate.");
        hasError = true;
    }

    // Validate Number of Children
    if (isNaN(children) || children < 0) {
        showError(childrenInput, "Please enter a valid number of children.");
        hasError = true;
    }

    // Stop calculation if there are errors
    if (hasError) return;

    // Proceed with tax calculations
    const birthdateObj = new Date(birthdate);
    const today = new Date();
    const age = today.getFullYear() - birthdateObj.getFullYear();
    
    const taxFreeAmount = 4000;
    const taxRate = 0.32;
    const taxableIncome = Math.max(income - taxFreeAmount, 0);
    const tax = taxableIncome * taxRate;

    let ageDiscount = 0;
    if (age > 65 || age < 25) {
        ageDiscount = 0; // Example: No extra discount
    }

    // Apply discount only for max 4 children
    const childrenDiscount = Math.min(children, 4) * 0.05;
    const totalDiscount = ageDiscount + childrenDiscount;
    const finalTax = tax * (1 - totalDiscount);
    const netIncome = income - finalTax;

    // Display results
    document.getElementById('totalIncome').textContent = income.toFixed(2);
    document.getElementById('taxFree').textContent = taxFreeAmount.toFixed(2);
    document.getElementById('taxableIncome').textContent = taxableIncome.toFixed(2);
    document.getElementById('tax').textContent = tax.toFixed(2);
    document.getElementById('discount').textContent = (totalDiscount * 100).toFixed(2) + '%';
    document.getElementById('finalTax').textContent = finalTax.toFixed(2);
    document.getElementById('netIncome').innerHTML = '<strong>' + netIncome.toFixed(2) + '</strong>';

    document.getElementById('result').style.display = 'block';
}

// Function to show error messages
function showError(inputElement, message) {
    const errorDiv = document.createElement('div');
    errorDiv.className = 'error-message';
    errorDiv.textContent = message;
    inputElement.parentNode.appendChild(errorDiv);
    inputElement.classList.add('input-error');
}

// Function to clear error messages
function clearErrors() {
    document.querySelectorAll('.error-message').forEach(error => error.remove());
    document.querySelectorAll('.input-error').forEach(input => input.classList.remove('input-error'));
}
