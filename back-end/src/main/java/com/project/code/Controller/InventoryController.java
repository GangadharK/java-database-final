package com.project.code.Controller;

import com.project.code.Model.CombinedRequest;
import com.project.code.Model.Inventory;
import com.project.code.Model.Product;
import com.project.code.Repo.InventoryRepository;
import com.project.code.Repo.ProductRepository;
import com.project.code.Repo.StoreRepository;
import com.project.code.Service.ServiceClass;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    ProductRepository productRepository;
    InventoryRepository inventoryRepository;
    StoreRepository storeRepository;
    ServiceClass serviceClass;
    @Autowired
    public InventoryController(ProductRepository productRepository,
                               InventoryRepository inventoryRepository,
                               StoreRepository storeRepository,
                               ServiceClass serviceClass) {
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.storeRepository = storeRepository;
        this.serviceClass = serviceClass;
    }
// 1. Set Up the Controller Class:
//    - Annotate the class with `@RestController` to indicate that this is a REST controller, which handles HTTP requests and responses.
//    - Use `@RequestMapping("/inventory")` to set the base URL path for all methods in this controller. All endpoints related to inventory will be prefixed with `/inventory`.


// 2. Autowired Dependencies:
//    - Autowire necessary repositories and services:
//      - `ProductRepository` will be used to interact with product data (i.e., finding, updating products).
//      - `InventoryRepository` will handle CRUD operations related to the inventory.
//      - `ServiceClass` will help with the validation logic (e.g., validating product IDs and inventory data).

    @PutMapping
    public Map<String,Object> updateInventory(@RequestBody CombinedRequest combinedRequest){
        if (combinedRequest == null || combinedRequest.getProduct() == null
                || combinedRequest.getProduct().getId() == null || combinedRequest.getInventory() == null
                || combinedRequest.getInventory().getStore() == null
                || combinedRequest.getInventory().getStore().getId() == null) {
            return Map.of("error", "Product and inventory store are required");
        }
        Long productId = combinedRequest.getProduct().getId();
        if (!serviceClass.validateProductId(productId)) {
            return Map.of("error", "Product not found");
        }
        Inventory submittedInventory = combinedRequest.getInventory();
        Inventory existingInventory = inventoryRepository.findByProduct_IdAndStore_Id(
                productId, submittedInventory.getStore().getId()).orElse(null);
        if (existingInventory == null) {
            return Map.of("error", "Inventory not found");
        }
        if (submittedInventory.getStockLevel() < 0) {
            return Map.of("error", "Stock level cannot be negative");
        }
        existingInventory.setStockLevel(submittedInventory.getStockLevel());
        inventoryRepository.save(existingInventory);
        return Map.of("inventory", "Inventory updated successfully");
    }
// 3. Define the `updateInventory` Method:
//    - This method handles HTTP PUT requests to update inventory for a product.
//    - It takes a `CombinedRequest` (containing `Product` and `Inventory`) in the request body.
//    - The product ID is validated, and if valid, the inventory is updated in the database.
//    - If the inventory exists, update it and return a success message. If not, return a message indicating no data available.

    @PostMapping
    public Map<String,Object> saveInventory(@Valid @RequestBody Inventory inventory){
        if(serviceClass.validateInventory(inventory)) {
            inventoryRepository.save(inventory);
            return Map.of("inventory", "Inventory saved successfully");
        }
        return Map.of("error","Inventory is invalid or already exists");
    }
// 4. Define the `saveInventory` Method:
//    - This method handles HTTP POST requests to save a new inventory entry.
//    - It accepts an `Inventory` object in the request body.
//    - It first validates whether the inventory already exists. If it exists, it returns a message stating so. If it doesn’t exist, it saves the inventory and returns a success message.

    @GetMapping("/store/{storeid}")
    public Map<String,Object> getAllProducts(@PathVariable("storeid") Long storeid){
        List<Inventory> inventory=inventoryRepository.findByStore_Id(storeid);
        List<Product> products=new ArrayList<>();
        for(Inventory invent:inventory){
            products.add(invent.getProduct());
        }
        return Map.of("products",products);
    }
// 5. Define the `getAllProducts` Method:
//    - This method handles HTTP GET requests to retrieve products for a specific store.
//    - It uses the `storeId` as a path variable and fetches the list of products from the database for the given store.
//    - The products are returned in a `Map` with the key `"products"`.

    @GetMapping("/category/{category}/product/{productName}")
    public Map<String,Object> getProductName(@PathVariable  String category,@PathVariable String productName){
        List<String> productNames=new ArrayList<>();
        boolean allCategories = "null".equalsIgnoreCase(category);
        boolean allNames = "null".equalsIgnoreCase(productName);
        if(!allCategories && !allNames){
            List<Product> products=productRepository.findByNameAndCategory(productName,category);
            products.forEach(product->productNames.add(product.getName()));
        }else if(allCategories && !allNames){
            List<Product> products=productRepository.findByName(productName);
            products.forEach(product->productNames.add(product.getName()));
        }else if(!allCategories){
            List<Product> products=productRepository.findByCategory(category);
            products.forEach(product->productNames.add(product.getName()));
        }else{
            List<Product> products=productRepository.findAll();
            products.forEach(product->productNames.add(product.getName()));
        }
        return Map.of("product",productNames);
    }

    @GetMapping("/filter/{category}/{name}/{storeId}")
    public Map<String, Object> filterProductsForStore(@PathVariable String category,
                                                       @PathVariable String name,
                                                       @PathVariable Long storeId) {
        boolean allCategories = "null".equalsIgnoreCase(category);
        boolean allNames = "null".equalsIgnoreCase(name);
        List<Product> products;
        if (allCategories) {
            products = inventoryRepository.findByStore_Id(storeId).stream()
                    .map(Inventory::getProduct)
                    .distinct()
                    .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        } else {
            products = new ArrayList<>(productRepository.findByStoreIdAndCategory(storeId, category));
        }
        if (!allNames) {
            products.removeIf(product -> product.getName() == null
                    || !product.getName().toLowerCase(Locale.ROOT).contains(name.toLowerCase(Locale.ROOT)));
        }
        return Map.of("products", products);
    }
// 6. Define the `getProductName` Method:
//    - This method handles HTTP GET requests to filter products by category and name.
//    - If either the category or name is `"null"`, adjust the filtering logic accordingly.
//    - Return the filtered products in the response with the key `"product"`.

    @GetMapping("/name/{productName}/storeId/{storeId}")
    public Map<String,Object> searchProducts(@PathVariable String productName,@PathVariable Long storeId){
        List<Product> products= productRepository.findByNameLike("%" + productName + "%");
        products.removeIf(product -> inventoryRepository.findByProduct_IdAndStore_Id(product.getId(), storeId).isEmpty());
        return Map.of("product",products);
    }
// 7. Define the `searchProduct` Method:
//    - This method handles HTTP GET requests to search for products by name within a specific store.
//    - It uses `name` and `storeId` as parameters and searches for products that match the `name` in the specified store.
//    - The search results are returned in the response with the key `"product"`.

    @DeleteMapping("/productId/{productID}")
    public Map<String,Object> deleteProduct(@PathVariable Long productID){
        if(serviceClass.validateProductId(productID)){
            inventoryRepository.deleteByProduct_Id(productID);
            productRepository.deleteById(productID);
            return Map.of("message","product deleted successfully");
        }
        return Map.of("message","product not found");
    }
// 8. Define the `removeProduct` Method:
//    - This method handles HTTP DELETE requests to delete a product by its ID.
//    - It first validates if the product exists. If it does, it deletes the product from the `ProductRepository` and also removes the related inventory entry from the `InventoryRepository`.
//    - Returns a success message with the key `"message"` indicating successful deletion.

    @GetMapping("/productId/{productId}/storeid/{storeId}/quantity/{quantity}")
    public Boolean validateQuantity(@PathVariable Long productId,@PathVariable Long storeId,@PathVariable Integer quantity){
        return quantity != null && quantity > 0
                && inventoryRepository.findByProduct_IdAndStore_Id(productId,storeId)
                .filter(inventory -> inventory.getStockLevel()>=quantity)
                .isPresent();
    }

    @GetMapping("/validate/{quantity}/{storeId}/{productId}")
    public Boolean validateAvailableQuantity(@PathVariable Integer quantity,
                                             @PathVariable Long storeId,
                                             @PathVariable Long productId) {
        return quantity != null && quantity > 0
                && inventoryRepository.findByProduct_IdAndStore_Id(productId, storeId)
                .filter(inventory -> inventory.getStockLevel() >= quantity)
                .isPresent();
    }
// 9. Define the `validateQuantity` Method:
//    - This method handles HTTP GET requests to validate if a specified quantity of a product is available in stock for a given store.
//    - It checks the inventory for the product in the specified store and compares it to the requested quantity.
//    - If sufficient stock is available, return `true`; otherwise, return `false`.

}
