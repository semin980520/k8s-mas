package com.example.order.ordersystem.product.dtos;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductUpdateDto {

    private String name;
    private int price;
    private String category;
    private int stockQuantity;
//    이미지수정은 일반적으로 별도의 api로 처리
    private MultipartFile productImage;

}
