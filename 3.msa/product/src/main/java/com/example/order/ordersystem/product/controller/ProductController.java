package com.example.order.ordersystem.product.controller;

import com.example.order.ordersystem.product.dtos.*;
import com.example.order.ordersystem.product.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }
    @PostMapping("/product/create")
    public Long create(@ModelAttribute ProductCreateDto dto,@RequestHeader("X-User-Email")String email){


        return productService.save(dto, email);
    }
    @GetMapping("/product/list")
    public Page<ProductListDto> findByAll(@PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC)Pageable pageable, @ModelAttribute ProductSearchDto searchDto){
        Page<ProductListDto> dto = productService.findByAll(pageable, searchDto);

        return dto;
    }
    @GetMapping("/product/detail/{id}")
    public ProductDetailDto findById(@PathVariable Long id){
        ProductDetailDto dto = productService.findById(id);

        return dto;
    }
    @PutMapping("/product/update/{id}")
    public ProductDetailDto update(@PathVariable Long id, ProductUpdateDto dto){
        productService.update(id, dto);

        return null;
    }
    @PutMapping("/product/updatestock")
    public ProductStockUpdateDto updateStock(@RequestBody ProductStockUpdateDto dto){
        productService.updateStock(dto);
        return  null;
    }
}
