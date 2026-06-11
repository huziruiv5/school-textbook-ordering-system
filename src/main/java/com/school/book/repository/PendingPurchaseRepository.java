package com.school.book.repository;

import com.school.book.entity.PendingPurchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PendingPurchaseRepository extends JpaRepository<PendingPurchase, Integer> {
    List<PendingPurchase> findByStatus(String status);

    List<PendingPurchase> findByBookId(Integer bookId);

    @Query("SELECT p FROM PendingPurchase p ORDER BY p.createdAt DESC")
    List<PendingPurchase> findAllOrderByCreatedAtDesc();
}
