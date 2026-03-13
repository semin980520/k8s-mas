package com.example.order.ordersystem.member.domain;

import com.example.order.ordersystem.order.domain.Ordering;
import com.example.order.ordersystem.product.domain.Product;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.query.Order;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter @ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Member {
    @Column(nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;
    @Column(nullable = false, unique = true)
    private String email;
    private String name;
    @Column(nullable = false)
    private String password;
    @CreationTimestamp
    private LocalDateTime createTime;
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.USER;
    @OneToMany(mappedBy = "member")
    @Builder.Default
    List<Ordering> orderingList = new ArrayList<>();
    @OneToMany(mappedBy = "member")
    @Builder.Default
    List<Product> productList = new ArrayList<>();
}
