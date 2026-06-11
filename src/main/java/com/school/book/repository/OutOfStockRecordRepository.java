package com.school.book.repository;

import com.school.book.entity.OutOfStockRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OutOfStockRecordRepository extends JpaRepository<OutOfStockRecord, Integer> {
    List<OutOfStockRecord> findByBookId(Integer bookId);

    List<OutOfStockRecord> findByStatus(String status);

    @Query("SELECT o FROM OutOfStockRecord o ORDER BY o.createdAt DESC")
    List<OutOfStockRecord> findAllOrderByCreatedAtDesc();
}
