package com.example.order.ordersystem.order.service;

import com.example.order.ordersystem.common.service.SseAlarmService;
import com.example.order.ordersystem.member.domain.Member;
import com.example.order.ordersystem.member.repositroy.MemberRepository;
import com.example.order.ordersystem.order.domain.Ordering;
import com.example.order.ordersystem.order.domain.Ordering_details;
import com.example.order.ordersystem.order.dtos.OrderCreateDto;
import com.example.order.ordersystem.order.dtos.OrderDetailDto;
import com.example.order.ordersystem.order.dtos.OrderListDto;
import com.example.order.ordersystem.order.repositroy.OrderDetailRepository;
import com.example.order.ordersystem.order.repositroy.OrderRepository;
import com.example.order.ordersystem.product.domain.Product;
import com.example.order.ordersystem.product.repositroy.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class OrderService {
    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final SseAlarmService sseAlarmService;
    private final RedisTemplate<String, String> redisTemplate;
    private final OrderDetailRepository orderDetailRepository;
    @Autowired
    public OrderService(OrderRepository orderRepository, MemberRepository memberRepository, ProductRepository productRepository, SseAlarmService sseAlarmService, @Qualifier("stockInventory") RedisTemplate<String, String> redisTemplate, OrderDetailRepository orderDetailRepository) {
        this.orderRepository = orderRepository;
        this.memberRepository = memberRepository;
        this.productRepository = productRepository;
        this.sseAlarmService = sseAlarmService;
        this.redisTemplate = redisTemplate;
        this.orderDetailRepository = orderDetailRepository;
    }
    public Long save(List<OrderCreateDto> dtos){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findAllByEmail(email).orElseThrow(() -> new EntityNotFoundException("이메일이 없습니다"));

        Ordering ordering = Ordering.builder() // 오더 객체 조립
                .member(member)
                .build();
        Ordering order = orderRepository.save(ordering);

        for (OrderCreateDto dto : dtos) {
            Product product = productRepository.findById(dto.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("상품이 없습니다."));
           if (product.getStockQuantity() < dto.getProductCount()){
               throw new IllegalArgumentException("재고가 부족합니다.");
           }
            product.updateStockQuantity(dto.getProductCount());
            Ordering_details detail = Ordering_details.builder()
                    .product(product)
                    .quantity(dto.getProductCount())
                    .ordering(ordering)
                    .build();
           orderDetailRepository.save(detail);

            ordering.getOrderingDetailsList().add(detail);

        }

        return order.getId();
    }

    public List<OrderListDto> findAll(){
        List<Ordering> orderingList = orderRepository.findAll();
        List<OrderListDto> dto = new ArrayList<>();

        for (Ordering o : orderingList) {
            List<OrderDetailDto> detailDtos = new ArrayList<>();
            for (Ordering_details d : o.getOrderingDetailsList()) {
                detailDtos.add(OrderDetailDto.fromEntity(d));
            }

            dto.add(OrderListDto.fromEntity(o, detailDtos));
        }

        return dto;
    }
    public List<OrderListDto> myorders() {
        String email = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        List<Ordering> orderingList = orderRepository.findByMemberEmail(email);
        List<OrderListDto> dto = new ArrayList<>();
        for (Ordering o : orderingList) {
            List<OrderDetailDto> detailDtos = new ArrayList<>();
            for (Ordering_details d : o.getOrderingDetailsList()) {
                detailDtos.add(OrderDetailDto.fromEntity(d));
            }
            dto.add(OrderListDto.fromEntity(o, detailDtos));
        }
        return dto;
    }
}
