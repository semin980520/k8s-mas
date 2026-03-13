package com.example.order.ordersystem.order.service;

import com.example.order.ordersystem.common.service.SseAlarmService;
import com.example.order.ordersystem.order.domain.Ordering;
import com.example.order.ordersystem.order.domain.Ordering_details;
import com.example.order.ordersystem.order.dtos.OrderCreateDto;
import com.example.order.ordersystem.order.dtos.OrderDetailDto;
import com.example.order.ordersystem.order.dtos.OrderListDto;
import com.example.order.ordersystem.order.dtos.ProductDto;
import com.example.order.ordersystem.order.feignclients.ProductFeignClient;
import com.example.order.ordersystem.order.repositroy.OrderDetailRepository;
import com.example.order.ordersystem.order.repositroy.OrderRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class OrderService {
    private final OrderRepository orderRepository;
    private final SseAlarmService sseAlarmService;
    private final OrderDetailRepository orderDetailRepository;
    private final RestTemplate restTemplate;
    private final ProductFeignClient productFeignClient;
    private final KafkaTemplate<String , Object> kafkaTemplate;
    @Autowired
    public OrderService(OrderRepository orderRepository, SseAlarmService sseAlarmService, OrderDetailRepository orderDetailRepository, RestTemplate restTemplate, ProductFeignClient productFeignClient, KafkaTemplate<String, Object> kafkaTemplate) {
        this.orderRepository = orderRepository;
        this.sseAlarmService = sseAlarmService;
        this.orderDetailRepository = orderDetailRepository;
        this.restTemplate = restTemplate;
        this.productFeignClient = productFeignClient;
        this.kafkaTemplate = kafkaTemplate;
    }
    public Long save(List<OrderCreateDto> dtos,String email){

        Ordering ordering = Ordering.builder() // 오더 객체 조립
                .memberEmail(email)
                .build();
        Ordering order = orderRepository.save(ordering);

        for (OrderCreateDto dto : dtos) {
//            1. 재고조회요청 (동기요청-http요청)
//            http://localhost:8080/product-service : apigateway을 통한 호출
//            http://product-service : 유레카에게 직접 질의 후 product-service 직접 호출
            String endPoint1 = "http://product-service/product/detail/"+ dto.getProductId();
            HttpHeaders headers = new HttpHeaders();
//            httpEntity: header+body
            HttpEntity<String> httpEntity = new HttpEntity<>(headers);
            ResponseEntity<ProductDto> responseEntity = restTemplate.exchange(endPoint1, HttpMethod.GET,httpEntity, ProductDto.class);
            ProductDto product = responseEntity.getBody();
            System.out.println(product);
            if (product.getStockQuantity() < dto.getProductCount()){
               throw new IllegalArgumentException("재고가 부족합니다.");
             }
//          2. 주문조회발생
            Ordering_details detail = Ordering_details.builder()
                    .productName(product.getName())
                    .productId(dto.getProductId())
                    .quantity(dto.getProductCount())
                    .ordering(ordering)
                    .build();
           orderDetailRepository.save(detail);
//           3.재고감소요청(동기-http요청/비동기-이벤트기반 모두 가능)
            System.out.println(dto);
            String endPoint2 = "http://product-service/product/updatestock";
            HttpHeaders headers2 = new HttpHeaders();
            headers2.setContentType(MediaType.APPLICATION_JSON);
//            httpEntity: header+body

            HttpEntity<OrderCreateDto> httpEntity2 = new HttpEntity<>(dto,headers2);
            restTemplate.exchange(endPoint2, HttpMethod.PUT,httpEntity2, Void.class);


            ordering.getOrderingDetailsList().add(detail);

        }

        return order.getId();
    }
    public Long saveFeign(List<OrderCreateDto> dtos,String email){

        Ordering ordering = Ordering.builder() // 오더 객체 조립
                .memberEmail(email)
                .build();
        Ordering order = orderRepository.save(ordering);

        for (OrderCreateDto dto : dtos) {
            ProductDto product = productFeignClient.getProductById(dto.getProductId());

            if (product.getStockQuantity() < dto.getProductCount()){
                throw new IllegalArgumentException("재고가 부족합니다.");
            }
            Ordering_details detail = Ordering_details.builder()
                    .productName(product.getName())
                    .productId(dto.getProductId())
                    .quantity(dto.getProductCount())
                    .ordering(ordering)
                    .build();
            orderDetailRepository.save(detail);
//            feign을 사용한 동기적 재고감소 요청
//            productFeignClient.updateStockQuantity(dto);
//            kafka를 활용한 비동기적 재고감소 요청
            kafkaTemplate.send("stock-update-topic", dto);

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
    public List<OrderListDto> myorders(String email) {
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
