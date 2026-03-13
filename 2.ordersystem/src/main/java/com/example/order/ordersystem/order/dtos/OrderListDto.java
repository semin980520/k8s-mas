package com.example.order.ordersystem.order.dtos;


import com.example.order.ordersystem.order.domain.Order_status;
import com.example.order.ordersystem.order.domain.Ordering;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderListDto {
    private Long id;
    private String memberEmail;
    private Order_status orderStatus;
    @Builder.Default
    private List<OrderDetailDto> orderDetails = new ArrayList<>();

    public static OrderListDto fromEntity(Ordering ordering, List<OrderDetailDto> orderDetails) {
        return OrderListDto.builder()
                .id(ordering.getId())
                .memberEmail(ordering.getMember().getEmail())
                .orderStatus(ordering.getOrderStatus())
                .orderDetails(orderDetails)
                .build();
    }

}
