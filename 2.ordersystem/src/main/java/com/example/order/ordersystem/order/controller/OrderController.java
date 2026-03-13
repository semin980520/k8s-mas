package com.example.order.ordersystem.order.controller;

import com.example.order.ordersystem.order.domain.Ordering;
import com.example.order.ordersystem.order.dtos.OrderCreateDto;
import com.example.order.ordersystem.order.dtos.OrderListDto;
import com.example.order.ordersystem.order.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }
    @PostMapping("/ordering/create")
    public ResponseEntity<Long> create(@RequestBody List<OrderCreateDto> dtos){
        Long orderId = orderService.save(dtos);

        System.out.println(dtos);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderId);
    }
    @GetMapping("/ordering/list")
    @PreAuthorize("hasRole('ADMIN')")
    public List<OrderListDto> findAll(){
        List<OrderListDto> dto = orderService.findAll();
        return dto;
    }
    @GetMapping("/ordering/myorders")
    public List<OrderListDto> myorders(){
        List<OrderListDto> dto = orderService.myorders();
        return dto;
    }

}
