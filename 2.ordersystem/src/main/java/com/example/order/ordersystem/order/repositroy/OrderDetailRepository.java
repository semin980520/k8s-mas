package com.example.order.ordersystem.order.repositroy;

import com.example.order.ordersystem.order.domain.Ordering_details;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderDetailRepository extends JpaRepository<Ordering_details, Long> {
}
