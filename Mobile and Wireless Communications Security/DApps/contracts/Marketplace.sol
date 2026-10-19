// SPDX-License-Identifier: MIT
pragma solidity ^0.8.21;

contract Marketplace {
    struct Product {
        uint256 id;
        string name;
        uint256 price;
        address seller;
        bool sold;
    }

    uint256 public productCount;
    mapping(uint256 => Product) public products;

    event ProductListed(uint256 indexed id, string name, uint256 price, address indexed seller);
    event ProductPurchased(uint256 indexed id, address indexed buyer, uint256 price);

    function listProduct(string calldata name, uint256 price) external {
        require(bytes(name).length > 0, "Name is required");
        require(price > 0, "Price must be greater than zero");

        productCount += 1;
        products[productCount] = Product({
            id: productCount,
            name: name,
            price: price,
            seller: msg.sender,
            sold: false
        });

        emit ProductListed(productCount, name, price, msg.sender);
    }

    function purchaseProduct(uint256 productId) external payable {
        Product storage product = products[productId];

        require(product.id != 0, "Product does not exist");
        require(!product.sold, "Product already sold");
        require(msg.value == product.price, "Incorrect payment amount");
        require(msg.sender != product.seller, "Seller cannot buy own product");

        product.sold = true;
        payable(product.seller).transfer(msg.value);

        emit ProductPurchased(productId, msg.sender, product.price);
    }
}
