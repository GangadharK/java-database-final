package com.project.code.Repo;

import com.project.code.Model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    @Query("SELECT i FROM Inventory i WHERE i.product.id = :productId AND i.store.id = :storeId")
    public Optional<Inventory> findByProduct_IdAndStore_Id(
            @Param("productId") Long productId, @Param("storeId") Long storeId);
    public List<Inventory> findByStore_Id(Long storeId);
    public void deleteByStore_Id(Long storeId);
    @Modifying
    @Transactional
    public void deleteByProduct_Id(Long productId);
// 1. Add the repository interface:
//    - Extend JpaRepository<Inventory, Long> to inherit basic CRUD functionality.
//    - This allows the repository to perform operations like save, delete, update, and find without having to implement these methods manually.

// Example: public interface InventoryRepository extends JpaRepository<Inventory, Long> {}

// 2. Add custom query methods:
//    - **findByProduct_IdAndStore_Id**:
//      - This method will allow you to find an inventory record by its product ID and store ID.
//      - Return type: Optional<Inventory>
//      - Parameters: Long productId, Long storeId

// Example: Optional<Inventory> findByProduct_IdAndStore_Id(Long productId, Long storeId);

//    - **findByStore_Id**:
//      - This method will allow you to find a list of inventory records for a specific store.
//      - Return type: List<Inventory>
//      - Parameter: Long storeId

// Example: List<Inventory> findByStore_Id(Long storeId);

//    - **deleteByProduct_Id**:
//      - This method will allow you to delete all inventory records related to a specific product ID.
//      - Return type: void
//      - Parameter: Long productId
//      - Use @Modifying and @Transactional annotations to ensure the database is modified correctly.

}
