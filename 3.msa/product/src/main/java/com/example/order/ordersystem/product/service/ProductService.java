package com.example.order.ordersystem.product.service;

import com.example.order.ordersystem.product.domain.Product;
import com.example.order.ordersystem.product.dtos.*;
import com.example.order.ordersystem.product.repositroy.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ProductService {
    private final ProductRepository productRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final S3Client s3Client;
    @Value("${aws.s3.bucket1}")
    private String bucket;

    public ProductService(ProductRepository productRepository, @Qualifier("stockInventory") RedisTemplate<String, String> redisTemplate, S3Client s3Client) {
        this.productRepository = productRepository;
        this.redisTemplate = redisTemplate;
        this.s3Client = s3Client;
    }

    public Long save(ProductCreateDto dto,String email) {



        Product product = dto.toEntity(email);
        Product productDb = productRepository.save(product);
        if (dto.getProductImage()!= null) {
            String fileName = "product-" + product.getId() + "profileImage-" + dto.getProductImage().getOriginalFilename();
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileName) // 파일명
                    .contentType(dto.getProductImage().getContentType()) // image/jpeg, video/mp4, ...인지 정보
                    .build();
            try {
                s3Client.putObject(request, RequestBody.fromBytes(dto.getProductImage().getBytes()));
            } catch (IOException e) {
//            이미지 올리다가 안됐으면 안됐다고 얘기해줘야 하기 때문.
                throw new RuntimeException(e); // 롤백의 기준이 되기 때문에 필요.
            }
            String imgUrl = s3Client.utilities().getUrl(a-> a.bucket(bucket).key(fileName)).toExternalForm();
            product.updatePorfileImageUrl(imgUrl);
            System.out.println(imgUrl);
        }

//        동시성문제 해결을 위해 상품등록 시 redis에 재고 세팅
        redisTemplate.opsForValue().set(String.valueOf(product.getId()), String.valueOf(product.getStockQuantity()));

        return productDb.getId();
    }

    public Page<ProductListDto> findByAll(Pageable pageable, ProductSearchDto searchDto) {

        Specification<Product> specification = new Specification<Product>() {
            @Override
            public Predicate toPredicate(Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicateList = new ArrayList<>();
                if (searchDto.getProductName() != null) {
                    predicateList.add(criteriaBuilder.like(root.get("name"), "%" + searchDto.getProductName() + "%"));
                }
                if (searchDto.getCategory() != null) {
                    predicateList.add(criteriaBuilder.equal(root.get("category"), searchDto.getCategory()));
                }
                Predicate[] predicatesArr = new Predicate[predicateList.size()];
                for (int i = 0; i < predicatesArr.length; i++) {
                    predicatesArr[i] = predicateList.get(i);
                }
                Predicate predicate = criteriaBuilder.and(predicatesArr);

                return predicate;
            }
        };
        Page<Product> products = productRepository.findAll(specification, pageable);
        Page<ProductListDto> dto = products.map(p -> ProductListDto.fromEntity(p));
        return dto;
    }

    @Transactional(readOnly = true)
    public ProductDetailDto findById(Long id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("상품이 없습니다"));
        ProductDetailDto dto = ProductDetailDto.fromEntity(product);

        return dto;
    }

    public void update(Long id, ProductUpdateDto dto) {
        Product product = productRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("상품이 없습니다"));
        product.updateProduct(dto);

        if (dto.getProductImage() != null) {
//            이미지를 수정하거나 추가하고자 하는 경우
//            기존이지미를 파일명으로 삭제

            String imgul = product.getImagePath();
            String fileName = imgul.substring(imgul.lastIndexOf("/") + 1);
            s3Client.deleteObject(a -> a.bucket(bucket).key(fileName));

//            신규 이미지 등록
            String newFileName = "product-" + product.getId() + "-profileimage-" + dto.getProductImage().getOriginalFilename();
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(newFileName)
                    .contentType(dto.getProductImage().getContentType())
                    .build();
            try {
                s3Client.putObject(request, RequestBody.fromBytes(dto.getProductImage().getBytes()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String newImgUrl = s3Client.utilities().getUrl(a -> a.bucket(bucket).key(newFileName)).toExternalForm();
            product.updatePorfileImageUrl(newImgUrl);
        } else {
//            이미지를 삭제하고자 하는 경우
            if (product.getImagePath() != null) {
                String imgul = product.getImagePath();
                String fileName = imgul.substring(imgul.lastIndexOf("/") + 1);
                s3Client.deleteObject(a -> a.bucket(bucket).key(fileName));
            }
        }
    }
    public void updateStock(ProductStockUpdateDto dto){
        Product product = productRepository.findById(dto.getProductId()).orElseThrow(()-> new EntityNotFoundException("아이디가 없습니다."));
        if (product.getStockQuantity() < dto.getProductCount()){
            throw new IllegalArgumentException("제고가 없습니다.");
        }
        product.updateStockQuantity(dto.getProductCount());

    }

}