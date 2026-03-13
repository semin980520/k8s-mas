package com.example.order.ordersystem.product.domain;

import com.example.order.ordersystem.member.domain.Member;
import com.example.order.ordersystem.order.domain.Ordering_details;
import com.example.order.ordersystem.product.dtos.ProductUpdateDto;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter @ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Product {
    @Column(nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private int price;
    private String category;
    @Column(nullable = false)
    private int stockQuantity;
    private String imagePath;
    @CreationTimestamp
    private LocalDateTime createTime;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id",foreignKey = @ForeignKey(ConstraintMode.CONSTRAINT), nullable = false)
    private Member member;
    @OneToMany(mappedBy = "product")
    @Builder.Default
    List<Ordering_details> orderingDetailsList = new ArrayList<>();

    public void updatePorfileImageUrl(String imagePath){
        this.imagePath = imagePath;
    }
    public void updateStockQuantity(int orderQuantity){
        this.stockQuantity = this.stockQuantity - orderQuantity;
    }
    public void updateProduct(ProductUpdateDto dto){
        this.name = dto.getName();
        this.category = dto.getCategory();
        this.stockQuantity = dto.getStockQuantity();
        this.price = dto.getPrice();
    }
}
