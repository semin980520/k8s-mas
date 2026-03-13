package com.example.order.ordersystem.order.controller;

import com.example.order.ordersystem.order.domain.Ordering;
import com.example.order.ordersystem.order.dtos.OrderCreateDto;
import com.example.order.ordersystem.order.dtos.OrderListDto;
import com.example.order.ordersystem.order.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }
    @PostMapping("/ordering/create")
    public ResponseEntity<Long> create(@RequestBody List<OrderCreateDto> dtos,@RequestHeader("X-User-Email")String email){
        Long orderId = orderService.saveFeign(dtos,email);

        System.out.println(dtos);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderId);
    }

    @GetMapping("/ordering/list")
    public List<OrderListDto> findAll(){
        List<OrderListDto> dto = orderService.findAll();
        return dto;
    }
    @GetMapping("/ordering/myorders")
    public List<OrderListDto> myorders(@RequestHeader("X-User-Email")String email){
        List<OrderListDto> dto = orderService.myorders(email);
        return dto;
    }

}
