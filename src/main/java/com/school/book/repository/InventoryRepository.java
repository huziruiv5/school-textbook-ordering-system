package com.school.book.repository;

import com.school.book.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Integer> {
    Optional<Inventory> findByBookId(Integer bookId);

    @Query("SELECT i FROM Inventory i WHERE i.quantity > 0 ORDER BY i.quantity DESC")
    List<Inventory> findAllWithStock();

    @Query("SELECT i FROM Inventory i WHERE i.quantity = 0")
    List<Inventory> findAllOutOfStock();

    @Query("SELECT i FROM Inventory i WHERE i.quantity > 0 AND i.quantity < ?1")
    List<Inventory> findLowStock(Integer threshold);
}
