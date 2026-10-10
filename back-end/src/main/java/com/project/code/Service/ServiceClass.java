package com.project.code.Service;

import com.project.code.Model.Inventory;
import com.project.code.Model.Product;
import com.project.code.Repo.InventoryRepository;
import com.project.code.Repo.ProductRepository;
import com.project.code.Repo.StoreRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ServiceClass {
    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;

    public ServiceClass(InventoryRepository inventoryRepository,
                        ProductRepository productRepository,
                        StoreRepository storeRepository) {
        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
        this.storeRepository = storeRepository;
    }

    public Boolean validateInventory(Inventory inventory) {
        if (inventory == null || inventory.getProduct() == null || inventory.getStore() == null
                || inventory.getProduct().getId() == null || inventory.getStore().getId() == null
                || inventory.getStockLevel() < 0) {
            return false;
        }
        return productRepository.existsById(inventory.getProduct().getId())
                && storeRepository.existsById(inventory.getStore().getId())
                && inventoryRepository.findByProduct_IdAndStore_Id(
                        inventory.getProduct().getId(), inventory.getStore().getId()).isEmpty();
    }

    public Boolean validateProduct(Product product) {
        return product != null && product.getName() != null
                && productRepository.findByName(product.getName()).isEmpty();
    }
    public Boolean validateProductId(Long productId) {
        return productRepository.findById(productId).isPresent();
    }
    public Inventory getInventoryById(Inventory inventory) {
        return inventoryRepository.findById(inventory.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventory not found"));
    }

    public Inventory getInventoryId(Long productId, Long storeId) {
        return inventoryRepository.findByProduct_IdAndStore_Id(productId, storeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventory not found"));
    }
// 1. **validateInventory Method**:
//    - Checks if an inventory record exists for a given product and store combination.
//    - Parameters: `Inventory inventory`
//    - Return Type: `boolean` (Returns `false` if inventory exists, otherwise `true`)

// 2. **validateProduct Method**:
//    - Checks if a product exists by its name.
//    - Parameters: `Product product`
//    - Return Type: `boolean` (Returns `false` if a product with the same name exists, otherwise `true`)

// 3. **ValidateProductId Method**:
//    - Checks if a product exists by its ID.
//    - Parameters: `long id`
//    - Return Type: `boolean` (Returns `false` if the product does not exist with the given ID, otherwise `true`)

// 4. **getInventoryId Method**:
//    - Fetches the inventory record for a given product ID and store ID.
//    - Parameters: `Long productId`, `Long storeId`
//    - Return Type: `Inventory` (Returns the inventory record for the product-store combination)

}
