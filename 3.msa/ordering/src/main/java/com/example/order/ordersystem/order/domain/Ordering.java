package com.example.order.ordersystem.order.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter @ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Ordering {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Order_status orderStatus = Order_status.ORDERED;
    @CreationTimestamp
    private LocalDateTime createTime;

//    mas모듈간의 관계성 제거
    private String memberEmail;
    @OneToMany(mappedBy = "ordering", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Ordering_details> orderingDetailsList = new ArrayList<>();

    public void addOrderingDetail(Ordering_details detail) {
        this.orderingDetailsList.add(detail);
        detail.updateOrdering(this);
    }
}
