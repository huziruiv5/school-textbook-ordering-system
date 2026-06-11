package com.school.book.repository;

import com.school.book.entity.InboundRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InboundRecordRepository extends JpaRepository<InboundRecord, Integer> {
    List<InboundRecord> findByBookId(Integer bookId);

    @Query("SELECT i FROM InboundRecord i ORDER BY i.inboundDate DESC")
    List<InboundRecord> findAllOrderByInboundDateDesc();
}
