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







    @GetMapping("/{id}")
    public Map<String,Object> getProductbyId(@PathVariable Long id) {
        Optional<Product> products=productRepository.findById(id);
        return products.<Map<String, Object>>map(product -> Map.of("products", product))
                .orElseGet(() -> Map.of("message", "Product not found"));
    }






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

    }






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






    @GetMapping
    public Map<String,Object> listProduct() {
        return Map.of("products",productRepository.findAll());
    }





    @GetMapping("filter/{category}/{storeid}")
    public Map<String,Object> getProductbyCategoryAndStoreId(@PathVariable String category, @PathVariable Long storeid) {
        List<Product> products;
        products=productRepository.findByCategory(category);
        products.removeIf(product -> inventoryRepository
                .findByProduct_IdAndStore_Id(product.getId(),storeid).isEmpty());
        return Map.of("product",products);
    }





    @DeleteMapping("/{id}")
    public Map<String,Object> deleteProduct(@PathVariable Long id) {
        if(serviceClass.validateProductId(id)){
            inventoryRepository.deleteByProduct_Id(id);
            productRepository.deleteById(id);
            return Map.of("message","Product successfully deleted");
        }
        return Map.of("message","Product not found");
    }







    @GetMapping("/searchProduct/{name}")
    public Map<String,Object> searchProduct(@PathVariable String name){
        return Map.of("products",productRepository.findByNameLike("%" + name + "%"));
    }








}
