package com.project.code.Repo;


import com.project.code.Model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product,Long> {
    public List<Product> findAll();
    public List<Product> findByCategory(String category);
    public List<Product> findByPriceBetween(Double priceStart, Double priceEnd);
    public List<Product> findByPriceGreaterThan(Double priceStart);
    public List<Product> findByName(String name);
    public List<Product> findByNameAndCategory(String name, String category);
    public List<Product> findByNameLike(String productName);
    @Query("SELECT DISTINCT p FROM Product p JOIN p.inventory i "
            + "WHERE i.store.id = :storeId AND p.category = :category")
    public List<Product> findByStoreIdAndCategory(
            @Param("storeId") Long storeId, @Param("category") String category);
















































}
