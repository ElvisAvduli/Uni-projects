// Product data
const products = [
    { id: 1, name: "Apples", category: "Fruits", price: 1.20, unit: "kg", availability: "Yes",
      description: "Our apples come from local farms and are known for their crisp texture and sweet flavor.",
      storage: "Refrigerator" },
    { id: 2, name: "Bananas", category: "Fruits", price: 1.50, unit: "kg", availability: "Yes",
      description: "Bananas are imported from Central America and ripen naturally.",
      storage: "Room temperature" },
    { id: 3, name: "Oranges", category: "Fruits", price: 0.90, unit: "kg", availability: "Yes",
      description: "Oranges come from the Peloponnese and are rich in vitamin C.",
      storage: "Refrigerator" },
    { id: 4, name: "Strawberries", category: "Fruits", price: 3.50, unit: "kg", availability: "No",
      description: "Strawberries are seasonal and will be available from April.",
      storage: "Refrigerator, best consumed quickly" },
    { id: 5, name: "Pears", category: "Fruits", price: 1.80, unit: "kg", availability: "Yes",
      description: "Pears are locally grown and have a particularly juicy texture.",
      storage: "Refrigerator" },
    { id: 6, name: "Carrots", category: "Vegetables", price: 0.70, unit: "kg", availability: "Yes",
      description: "Carrots are organically grown and rich in vitamin A.",
      storage: "Refrigerator" },
    { id: 7, name: "Potatoes", category: "Vegetables", price: 0.60, unit: "kg", availability: "Yes",
      description: "Potatoes come from Macedonia and are ideal for frying and baking.",
      storage: "Cool, dark place" },
    { id: 8, name: "Onions", category: "Vegetables", price: 0.80, unit: "kg", availability: "Yes",
      description: "Onions are locally grown and have a strong flavor.",
      storage: "Cool, dark place" }
];

// Initialize the page
document.addEventListener('DOMContentLoaded', function() {
    // Fill the price table
    const tableBody = document.querySelector('#product-table tbody');
    products.forEach(product => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${product.id}</td>
            <td>${product.name}</td>
            <td>${product.category}</td>
            <td>${product.price} €</td>
            <td>${product.unit}</td>
            <td>${product.availability}</td>
        `;
        tableBody.appendChild(row);
    });

    // Fill the dropdown select
    const select = document.getElementById('product-select');
    products.forEach(product => {
        const option = document.createElement('option');
        option.value = product.id;
        option.textContent = product.name;
        select.appendChild(option);
    });

    // Show first product by default
    showProductDetails(products[0].id);
});

// Show product info button click handler
document.getElementById('show-info-btn').addEventListener('click', function() {
    const selectedId = parseInt(document.getElementById('product-select').value);
    showProductDetails(selectedId);
});

// Function to show product details
function showProductDetails(productId) {
    const product = products.find(p => p.id === productId);
    const container = document.getElementById('product-details-container');
    
    container.innerHTML = `
        <div class="product-details" style="display: block;">
            <h3>${product.name}</h3>
            <p><strong>Category:</strong> ${product.category}</p>
            <p><strong>Price:</strong> ${product.price} € per ${product.unit}</p>
            <p><strong>Availability:</strong> ${product.availability}</p>
            <p>${product.description}</p>
            <p><strong>Storage:</strong> ${product.storage}</p>
        </div>
    `;
}