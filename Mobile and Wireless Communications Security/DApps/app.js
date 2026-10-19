const connectWalletBtn = document.getElementById("connectWalletBtn");
const walletAddress = document.getElementById("walletAddress");
const listProductForm = document.getElementById("listProductForm");
const productsContainer = document.getElementById("productsContainer");
const statusMessage = document.getElementById("statusMessage");
const refreshBtn = document.getElementById("refreshBtn");

let web3;
let account;
let marketplace;

const CONTRACT_ARTIFACT_PATH = "./build/contracts/Marketplace.json";

function setStatus(message, isError = false) {
  statusMessage.textContent = message;
  statusMessage.classList.toggle("error", isError);
}

async function loadContract() {
  const artifactResponse = await fetch(CONTRACT_ARTIFACT_PATH);
  if (!artifactResponse.ok) {
    throw new Error("Could not load contract artifact. Run truffle compile first.");
  }

  const artifact = await artifactResponse.json();
  const networkId = await web3.eth.net.getId();
  const deployment = artifact.networks[networkId];

  if (!deployment) {
    throw new Error("Contract is not deployed on this network. Run truffle migrate --network development.");
  }

  const code = await web3.eth.getCode(deployment.address);
  if (!code || code === "0x" || code === "0x0") {
    throw new Error(
      "No contract code found at the deployed address. If Ganache was restarted, run truffle migrate --reset --network development and refresh this page."
    );
  }

  marketplace = new web3.eth.Contract(artifact.abi, deployment.address);
}

function productCardMarkup(product, priceEth) {
  return `
    <article class="product-card">
      <h3 class="product-title">${product.name}</h3>
      <p class="price-tag">${priceEth} ETH</p>
      <p class="wallet">Seller: ${product.seller}</p>
      <button class="btn btn-accent" data-buy-id="${product.id}" data-buy-price="${product.price}">Buy</button>
    </article>
  `;
}

async function renderProducts() {
  if (!marketplace) {
    return;
  }

  productsContainer.innerHTML = "";
  const totalProducts = Number(await marketplace.methods.productCount().call());

  if (totalProducts === 0) {
    productsContainer.innerHTML = '<p class="note">No products listed yet.</p>';
    return;
  }

  const unsoldProducts = [];
  for (let i = 1; i <= totalProducts; i += 1) {
    const product = await marketplace.methods.products(i).call();
    if (!product.sold) {
      unsoldProducts.push(product);
    }
  }

  if (unsoldProducts.length === 0) {
    productsContainer.innerHTML = '<p class="note">All products are sold.</p>';
    return;
  }

  const cards = unsoldProducts
    .map((product) => {
      const priceEth = web3.utils.fromWei(product.price, "ether");
      return productCardMarkup(product, priceEth);
    })
    .join("");

  productsContainer.innerHTML = cards;

  document.querySelectorAll("[data-buy-id]").forEach((button) => {
    button.addEventListener("click", async (event) => {
      const productId = event.currentTarget.getAttribute("data-buy-id");
      const productPrice = event.currentTarget.getAttribute("data-buy-price");

      try {
        setStatus("Sending purchase transaction...");
        await marketplace.methods.purchaseProduct(productId).send({
          from: account,
          value: productPrice,
        });
        setStatus("Product purchased successfully.");
        await renderProducts();
      } catch (error) {
        setStatus(error.message || "Purchase failed.", true);
      }
    });
  });
}

async function connectWallet() {
  if (!window.ethereum) {
    setStatus("MetaMask is not installed.", true);
    return;
  }

  try {
    web3 = new Web3(window.ethereum);
    const accounts = await window.ethereum.request({ method: "eth_requestAccounts" });
    account = accounts[0];
    walletAddress.textContent = `Wallet: ${account}`;

    await loadContract();
    setStatus("Connected to wallet and contract loaded.");
    await renderProducts();
  } catch (error) {
    setStatus(error.message || "Failed to connect wallet.", true);
  }
}

listProductForm.addEventListener("submit", async (event) => {
  event.preventDefault();

  if (!marketplace || !account) {
    setStatus("Connect MetaMask first.", true);
    return;
  }

  const productName = document.getElementById("productName").value.trim();
  const productPriceEth = document.getElementById("productPrice").value.trim();

  if (!productName || !productPriceEth) {
    setStatus("Please fill in product name and price.", true);
    return;
  }

  try {
    const priceInWei = web3.utils.toWei(productPriceEth, "ether");
    setStatus("Sending list transaction...");

    await marketplace.methods.listProduct(productName, priceInWei).send({ from: account });

    setStatus("Product listed successfully.");
    listProductForm.reset();
    await renderProducts();
  } catch (error) {
    setStatus(error.message || "Listing failed.", true);
  }
});

connectWalletBtn.addEventListener("click", connectWallet);
refreshBtn.addEventListener("click", renderProducts);

if (window.ethereum) {
  window.ethereum.on("accountsChanged", (accounts) => {
    account = accounts[0] || null;
    walletAddress.textContent = account ? `Wallet: ${account}` : "Wallet: Not connected";
  });

  window.ethereum.on("chainChanged", () => {
    window.location.reload();
  });
}
