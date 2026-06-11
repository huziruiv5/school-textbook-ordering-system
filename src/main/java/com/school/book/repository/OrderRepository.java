package com.school.book.repository;

import com.school.book.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    Optional<Order> findByOrderNo(String orderNo);

    List<Order> findByUserId(Integer userId);

    List<Order> findByStatus(String status);

    List<Order> findByApproverId(Integer approverId);

    @Query("SELECT o FROM Order o ORDER BY o.createdAt DESC")
    List<Order> findAllOrderByCreatedAtDesc();

    @Query("SELECT o FROM Order o WHERE o.userId = ?1 ORDER BY o.createdAt DESC")
    List<Order> findByUserIdOrderByCreatedAtDesc(Integer userId);
}
