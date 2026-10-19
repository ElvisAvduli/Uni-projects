const Marketplace = artifacts.require("Marketplace");

contract("Marketplace", (accounts) => {
  const seller = accounts[0];
  const buyer = accounts[1];
  const price = web3.utils.toWei("1", "ether");

  it("lists a product", async () => {
    const instance = await Marketplace.new();
    await instance.listProduct("Laptop", price, { from: seller });

    const count = await instance.productCount();
    const product = await instance.products(count);

    assert.equal(count.toString(), "1");
    assert.equal(product.name, "Laptop");
    assert.equal(product.price.toString(), price);
    assert.equal(product.seller, seller);
    assert.equal(product.sold, false);
  });

  it("allows product purchase with exact amount", async () => {
    const instance = await Marketplace.new();
    await instance.listProduct("Phone", price, { from: seller });

    await instance.purchaseProduct(1, { from: buyer, value: price });

    const product = await instance.products(1);
    assert.equal(product.sold, true);
  });

  it("rejects purchase with wrong amount", async () => {
    const instance = await Marketplace.new();
    await instance.listProduct("Tablet", price, { from: seller });

    try {
      await instance.purchaseProduct(1, { from: buyer, value: web3.utils.toWei("0.5", "ether") });
      assert.fail("Expected revert not received");
    } catch (error) {
      assert(error.message.includes("Incorrect payment amount"));
    }
  });
});
