<?php
include 'products_data.php';

foreach ($products as $product) {
    echo '<option value="' . $product['id'] . '">' . $product['name'] . '</option>';
}
?>