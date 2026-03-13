package com.example.order.ordersystem.order.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter @ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Ordering_details {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int quantity;
    @CreationTimestamp
    private LocalDateTime createTime;

    private Long productId;

//    mas환경에서는 빈번한 http요청에 성능저하를 막기 위한, 반정규화 설계도 가능
    private String productName;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordering_id", foreignKey = @ForeignKey(ConstraintMode.CONSTRAINT), nullable = false)
    private Ordering ordering;

    protected void updateOrdering(Ordering ordering) {
        this.ordering = ordering;
    }
}
