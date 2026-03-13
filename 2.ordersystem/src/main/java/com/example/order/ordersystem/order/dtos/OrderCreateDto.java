package com.example.order.ordersystem.order.dtos;

import com.example.order.ordersystem.member.domain.Member;
import com.example.order.ordersystem.order.domain.Order_status;
import com.example.order.ordersystem.order.domain.Ordering;
import com.example.order.ordersystem.order.domain.Ordering_details;
import com.example.order.ordersystem.product.domain.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderCreateDto {

    private Long productId;
    private int productCount;




}
