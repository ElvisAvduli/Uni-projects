<?php
include 'products_data.php';

foreach ($products as $product) {
    echo '<div class="product-info" id="product-' . $product['id'] . '">';
    echo '<h3>' . $product['name'] . '</h3>';
    echo '<p><strong>Category:</strong> ' . $product['category'] . '</p>';
    echo '<p><strong>Price:</strong> ' . $product['price'] . ' € per ' . $product['unit'] . '</p>';
    echo '<p><strong>Availability:</strong> ' . $product['availability'] . '</p>';
    
    switch ($product['id']) {
        case 1:
            echo '<p>Our apples come from local farms and are known for their crisp texture and sweet flavor.</p>';
            echo '<p><strong>Storage:</strong> Refrigerator</p>';
            break;
        case 2:
            echo '<p>Bananas are imported from Central America and ripen naturally.</p>';
            echo '<p><strong>Storage:</strong> Room temperature</p>';
            break;
        case 3:
            echo '<p>Oranges come from the Peloponnese and are rich in vitamin C.</p>';
            echo '<p><strong>Storage:</strong> Refrigerator</p>';
            break;
        case 4:
            echo '<p>Strawberries are seasonal and will be available from April.</p>';
            echo '<p><strong>Storage:</strong> Refrigerator, best consumed quickly</p>';
            break;
        case 5:
            echo '<p>Pears are locally grown and have a particularly juicy texture.</p>';
            echo '<p><strong>Storage:</strong> Refrigerator</p>';
            break;
        case 6:
            echo '<p>Carrots are organically grown and rich in vitamin A.</p>';
            echo '<p><strong>Storage:</strong> Refrigerator</p>';
            break;
        case 7:
            echo '<p>Potatoes come from Macedonia and are ideal for frying and baking.</p>';
            echo '<p><strong>Storage:</strong> Cool, dark place</p>';
            break;
        case 8:
            echo '<p>Onions are locally grown and have a strong flavor.</p>';
            echo '<p><strong>Storage:</strong> Cool, dark place</p>';
            break;
    }
    
    echo '</div>';
}
?>