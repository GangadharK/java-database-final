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
    @PostMapping
    public Map<String,Object> saveInventory(@Valid @RequestBody Inventory inventory){
        if(serviceClass.validateInventory(inventory)) {
            inventoryRepository.save(inventory);
            return Map.of("inventory", "Inventory saved successfully");
        }
        return Map.of("error","Inventory is invalid or already exists");
    }
    @GetMapping("/store/{storeid}")
    public Map<String,Object> getAllProducts(@PathVariable("storeid") Long storeid){
        List<Inventory> inventory=inventoryRepository.findByStore_Id(storeid);
        List<Product> products=new ArrayList<>();
        for(Inventory invent:inventory){
            products.add(invent.getProduct());
        }
        return Map.of("products",products);
    }
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
    @GetMapping("/{name}/{storeId}")
    public Map<String,Object> searchProducts(@PathVariable("name") String productName,@PathVariable Long storeId){
        List<Product> products= productRepository.findByNameLike("%" + productName + "%");
        products.removeIf(product -> inventoryRepository.findByProduct_IdAndStore_Id(product.getId(), storeId).isEmpty());
        return Map.of("product",products);
    }
    @DeleteMapping("/{productID}")
    public Map<String,Object> deleteProduct(@PathVariable Long productID){
        if(Boolean.TRUE.equals(serviceClass.validateProductId(productID))){
            inventoryRepository.deleteByProduct_Id(productID);
            productRepository.deleteById(productID);
            return Map.of("message","product deleted successfully");
        }
        return Map.of("message","product not found");
    }
    @GetMapping("/validate/{quantity}/{storeId}/{productId}")
    public Boolean validateQuantity(@PathVariable Integer quantity,
                                             @PathVariable Long storeId,
                                             @PathVariable Long productId) {
        return quantity != null && quantity > 0
                && inventoryRepository.findByProduct_IdAndStore_Id(productId, storeId)
                .filter(inventory -> inventory.getStockLevel() >= quantity)
                .isPresent();
    }
}
