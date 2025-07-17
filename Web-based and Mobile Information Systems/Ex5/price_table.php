<?php
// Include the products data
include 'products_data.php';

// Display the price table
echo '<table>';
echo '<thead><tr><th>ID</th><th>Name</th><th>Category</th><th>Price</th><th>Unit</th><th>Availability</th></tr></thead>';
echo '<tbody>';

foreach ($products as $product) {
    echo '<tr>';
    echo '<td>' . $product['id'] . '</td>';
    echo '<td>' . $product['name'] . '</td>';
    echo '<td>' . $product['category'] . '</td>';
    echo '<td>' . $product['price'] . ' €</td>';
    echo '<td>' . $product['unit'] . '</td>';
    echo '<td>' . $product['availability'] . '</td>';
    echo '</tr>';
}

echo '</tbody></table>';
?>