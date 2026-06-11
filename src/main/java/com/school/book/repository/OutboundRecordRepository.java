package com.school.book.repository;

import com.school.book.entity.OutboundRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OutboundRecordRepository extends JpaRepository<OutboundRecord, Integer> {
    List<OutboundRecord> findByBookId(Integer bookId);

    @Query("SELECT o FROM OutboundRecord o ORDER BY o.outboundDate DESC")
    List<OutboundRecord> findAllOrderByOutboundDateDesc();
}
