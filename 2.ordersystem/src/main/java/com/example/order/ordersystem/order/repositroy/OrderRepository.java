package com.example.order.ordersystem.order.repositroy;

import com.example.order.ordersystem.order.domain.Ordering;
import org.hibernate.query.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Ordering, Long> {
    List<Ordering> findByMemberEmail(String email); //맴버에서 가져옴
}
