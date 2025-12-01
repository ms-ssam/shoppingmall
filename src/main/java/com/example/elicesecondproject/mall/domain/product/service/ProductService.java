package com.example.elicesecondproject.mall.domain.product.service;

import com.example.elicesecondproject.mall.domain.product.dto.ProductSummaryDto;
import com.example.elicesecondproject.mall.domain.product.entity.Product;
import com.example.elicesecondproject.mall.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
 //PROD-F-01
    public Page<ProductSummaryDto> getAllProducts(Pageable pageable) {
            Page<Product> products = productRepository.findByDeletedAtIsNull(pageable);
            return products.map(ProductSummaryDto::from);
    }
    /*
    DTL-F-01 : 상품 기본 정보 조회 -> 상품 ID로 상품 기본 정보를 조회한다.
        response : name, price, discountRate, description, categoryId
                    삭제된 상품: 404 에러 : 상품 상태 => 판매 / 판중 / 품절 (판매중지 == 삭제???)
                    판매중지 상품: "현재 판매하지 않는 상품입니다" 메시지
      public ProductSummaryDto getProduct(Long productId) {
        Product foundProduct = productRepository.findById(productId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));  // 삭제된 제품 404 Error

        fou
      }
     */

}
