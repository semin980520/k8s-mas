package com.example.order.ordersystem.order.dtos;

import com.example.order.ordersystem.order.domain.Ordering;
import com.example.order.ordersystem.order.domain.Ordering_details;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetailDto {
    private Long detailId;
    private String productName;
    private int productCount;

    public static OrderDetailDto fromEntity(Ordering_details detail) {
        return OrderDetailDto.builder()
                .detailId(detail.getId())
                .productName(detail.getProduct().getName())
                .productCount(detail.getQuantity())
                .build();
    }


}
