package com.school.book.repository;

import com.school.book.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Integer> {
    Optional<Invoice> findByInvoiceNo(String invoiceNo);
    List<Invoice> findByOrderId(Integer orderId);
}
