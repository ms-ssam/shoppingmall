package com.example.elicesecondproject.mall.domain.product.service;

import com.example.elicesecondproject.mall.domain.category.repository.CategoryRepository;
import com.example.elicesecondproject.mall.domain.product.dto.ProductSortType;
import com.example.elicesecondproject.mall.domain.product.dto.ProductSummaryDto;
import com.example.elicesecondproject.mall.domain.product.entity.Product;
import com.example.elicesecondproject.mall.domain.product.entity.ProductStatus;
import com.example.elicesecondproject.mall.domain.product.mapper.ProductMapper;
import com.example.elicesecondproject.mall.domain.product.repository.ProductRepository;
import com.example.elicesecondproject.mall.domain.product.repository.ProductRepositoryCustom;
import com.example.elicesecondproject.mall.global.exception.BusinessException;
import com.example.elicesecondproject.mall.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepositoryCustom productRepositoryCustom;
    private final CategoryRepository categoryRepository;

    // 추가 선언
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    
    //PROD-F-01
    public Page<ProductSummaryDto> getProductsByCategory(
            Long categoryId,
            Boolean includeSubCategories,
            ProductSortType sortType,
            Pageable pageable) {

        validateCategoryExists(categoryId);

        includeSubCategories = includeSubCategories != null ? includeSubCategories : false;
        sortType = sortType != null ? sortType : ProductSortType.LATEST;

        return productRepositoryCustom.findProductsByCategory(
                categoryId,
                includeSubCategories,
                sortType,
                pageable
        );
    }

    private void validateCategoryExists(Long categoryId) {
        if (categoryId != null && !categoryRepository.existsByIdAndDeletedAtIsNull(categoryId)) {
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
        }
    }



 //PROD-F-01
    public Page<ProductSummaryDto> getAllProducts(Pageable pageable) {
            Page<Product> products = productRepository.findByDeletedAtIsNull(pageable);
            return products.map(productMapper::toSummaryDto);
    }

    /*
    DTL-F-01 : 상품 기본 정보 조회 -> 상품 ID로 상품 기본 정보를 조회한다.
        response : name, price, discountRate, description, categoryId
     */
      public ProductSummaryDto getProduct(Long productId) {
          Product foundProduct = productRepository.findById(productId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));  // 삭제된 제품 404 Error

          if(foundProduct.getStatus() == ProductStatus.STOP) {
              throw new BusinessException(ErrorCode.PRODUCT_STOPPED);  // 판매중지 상품 400 error "현재 판매하지 않는 상품입니다" 메시지
          }

          return productMapper.toSummaryDto(foundProduct);
      }

}
