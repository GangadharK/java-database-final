package com.project.code.Controller;

import com.project.code.Model.Product;
import com.project.code.Repo.InventoryRepository;
import com.project.code.Repo.ProductRepository;
import com.project.code.Service.ServiceClass;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/product")
public class ProductController {
    @Autowired
    ProductRepository productRepository;
    @Autowired
    ServiceClass serviceClass;
    @Autowired
    InventoryRepository inventoryRepository;
// 1. Set Up the Controller Class:
//    - Annotate the class with `@RestController` to designate it as a REST controller for handling HTTP requests.
//    - Map the class to the `/product` URL using `@RequestMapping("/product")`.


// 2. Autowired Dependencies:
//    - Inject the following dependencies via `@Autowired`:
//        - `ProductRepository` for CRUD operations on products.
//        - `ServiceClass` for product validation and business logic.
//        - `InventoryRepository` for managing the inventory linked to products.


    @PostMapping
    public Map<String,String> addProduct(@Valid @RequestBody Product product) {
        try{
            if(serviceClass.validateProduct(product)) {
                 productRepository.save(product);
                 return Map.of("message","Product successfully added");
            }
        }
        catch (DataIntegrityViolationException e) {
            return Map.of("message", "Product Not Added please try again");
        }
        return Map.of("message", "Product already exists");
    }
// 3. Define the `addProduct` Method:
//    - Annotate with `@PostMapping` to handle POST requests for adding a new product.
//    - Accept `Product` object in the request body.
//    - Validate product existence using `validateProduct()` in `ServiceClass`.
//    - Save the valid product using `save()` method of `ProductRepository`.
//    - Catch exceptions (e.g., `DataIntegrityViolationException`) and return appropriate error message.

    @GetMapping("/{id}")
    public Map<String,Object> getProductbyId(@PathVariable Long id) {
        Optional<Product> products=productRepository.findById(id);
        return products.<Map<String, Object>>map(product -> Map.of("products", product))
                .orElseGet(() -> Map.of("message", "Product not found"));
    }
// 4. Define the `getProductbyId` Method:
//    - Annotate with `@GetMapping("/product/{id}")` to handle GET requests for retrieving a product by ID.
//    - Accept product ID via `@PathVariable`.
//    - Use `findById(id)` method from `ProductRepository` to fetch the product.
//    - Return the product in a `Map<String, Object>` with key `products`.

    @PutMapping
    public Map<String,Object> updateProduct(@Valid @RequestBody Product product) {
        try{
            if(product.getId() != null && serviceClass.validateProductId(product.getId())) {
                if (productRepository.findByName(product.getName()).stream()
                        .anyMatch(existing -> !existing.getId().equals(product.getId()))) {
                    return Map.of("message", "Product with this name already exists");
                }
                Product existingProduct = productRepository.findById(product.getId()).orElseThrow();
                existingProduct.setName(product.getName());
                existingProduct.setPrice(product.getPrice());
                existingProduct.setCategory(product.getCategory());
                productRepository.save(existingProduct);
                return Map.of("message","Product successfully updated");
            }else  {
                return Map.of("message", "Product doesn't exist");
            }
        }catch (DataIntegrityViolationException e) {
            return Map.of("message", "Product Not updated please try again");
        }
        //return Map.of("message", "Product Not Added please try again");
    }
 // 5. Define the `updateProduct` Method:
//    - Annotate with `@PutMapping` to handle PUT requests for updating an existing product.
//    - Accept updated `Product` object in the request body.
//    - Use `save()` method from `ProductRepository` to update the product.
//    - Return a success message with key `message` after updating the product.

    @GetMapping("/category/{name}/{category}")
    public Map<String,Object> filterbyCategoryProduct(@PathVariable String name, @PathVariable String category) {
        List<Product> products;
        boolean allNames = "null".equalsIgnoreCase(name);
        boolean allCategories = "null".equalsIgnoreCase(category);
        if (allNames && allCategories) {
            products=productRepository.findAll();
        } else if(allNames) {
            products=productRepository.findByCategory(category);
        } else if (allCategories) {
            products=productRepository.findByNameLike("%" + name + "%");
        } else {
            products=productRepository.findByNameAndCategory(name,category);
        }
        return Map.of("products",products);
    }
// 6. Define the `filterbyCategoryProduct` Method:
//    - Annotate with `@GetMapping("/category/{name}/{category}")` to handle GET requests for filtering products by `name` and `category`.
//    - Use conditional filtering logic if `name` or `category` is `"null"`.
//    - Fetch products based on category using methods like `findByCategory()` or `findProductBySubNameAndCategory()`.
//    - Return filtered products in a `Map<String, Object>` with key `products`.

    @GetMapping
    public Map<String,Object> listProduct() {
        return Map.of("products",productRepository.findAll());
    }
 // 7. Define the `listProduct` Method:
//    - Annotate with `@GetMapping` to handle GET requests to fetch all products.
//    - Fetch all products using `findAll()` method from `ProductRepository`.
//    - Return all products in a `Map<String, Object>` with key `products`.

    @GetMapping("filter/{category}/{storeid}")
    public Map<String,Object> getProductbyCategoryAndStoreId(@PathVariable String category, @PathVariable Long storeid) {
        List<Product> products;
        products=productRepository.findByCategory(category);
        products.removeIf(product -> inventoryRepository
                .findByProduct_IdAndStore_Id(product.getId(),storeid).isEmpty());
        return Map.of("product",products);
    }
// 8. Define the `getProductbyCategoryAndStoreId` Method:
//    - Annotate with `@GetMapping("filter/{category}/{storeid}")` to filter products by `category` and `storeId`.
//    - Use `findProductByCategory()` method from `ProductRepository` to retrieve products.
//    - Return filtered products in a `Map<String, Object>` with key `product`.

    @DeleteMapping("/{id}")
    public Map<String,Object> deleteProduct(@PathVariable Long id) {
        if(serviceClass.validateProductId(id)){
            inventoryRepository.deleteByProduct_Id(id);
            productRepository.deleteById(id);
            return Map.of("message","Product successfully deleted");
        }
        return Map.of("message","Product not found");
    }
// 9. Define the `deleteProduct` Method:
//    - Annotate with `@DeleteMapping("/{id}")` to handle DELETE requests for removing a product by its ID.
//    - Validate product existence using `ValidateProductId()` in `ServiceClass`.
//    - Remove product from `Inventory` first using `deleteByProductId(id)` in `InventoryRepository`.
//    - Remove product from `Product` using `deleteById(id)` in `ProductRepository`.
//    - Return a success message with key `message` indicating product deletion.

    @GetMapping("/searchProduct/{name}")
    public Map<String,Object> searchProduct(@PathVariable String name){
        return Map.of("products",productRepository.findByNameLike("%" + name + "%"));
    }
 // 10. Define the `searchProduct` Method:
//    - Annotate with `@GetMapping("/searchProduct/{name}")` to search for products by `name`.
//    - Use `findProductBySubName()` method from `ProductRepository` to search products by name.
//    - Return search results in a `Map<String, Object>` with key `products`.


  
    
}
