package com.example.elicesecondproject.mall.domain.product.service;

import com.example.elicesecondproject.mall.domain.category.repository.CategoryRepository;
import com.example.elicesecondproject.mall.domain.product.dto.ProductDetailResponse;
import com.example.elicesecondproject.mall.domain.product.dto.ProductImageDto;
import com.example.elicesecondproject.mall.domain.product.dto.ProductSortType;
import com.example.elicesecondproject.mall.domain.product.dto.ProductSummaryDto;
import com.example.elicesecondproject.mall.domain.product.entity.ImageType;
import com.example.elicesecondproject.mall.domain.product.entity.Product;
import com.example.elicesecondproject.mall.domain.product.entity.ProductImage;
import com.example.elicesecondproject.mall.domain.product.entity.ProductStatus;
import com.example.elicesecondproject.mall.domain.product.mapper.ProductMapper;
import com.example.elicesecondproject.mall.domain.product.repository.ProductImageRepository;
import com.example.elicesecondproject.mall.domain.product.repository.ProductRepository;
import com.example.elicesecondproject.mall.domain.product.repository.ProductRepositoryCustom;
import com.example.elicesecondproject.mall.global.exception.BusinessException;
import com.example.elicesecondproject.mall.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final CategoryRepository categoryRepository;

    // 추가 선언
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ProductImageRepository productImageRepository;

    //PROD-F-02
    public Page<ProductSummaryDto> getProductsByCategory(
            Long categoryId,
            Boolean includeSubCategories,
            ProductSortType sortType,
            Pageable pageable) {

        validateCategoryExists(categoryId);

        includeSubCategories = includeSubCategories != null ? includeSubCategories : false;
        sortType = sortType != null ? sortType : ProductSortType.LATEST;

        return productRepository.findProductsByCategory(
                categoryId,
                includeSubCategories,
                sortType,
                pageable
        );
    }





 //PROD-F-01
    public Page<ProductSummaryDto> getAllProducts(Pageable pageable) {
            Page<Product> products = productRepository.findByDeletedAtIsNull(pageable);
            return products.map(productMapper::toSummaryDto);
    }

    /*
    DTL-F-01 : 상품 기본 정보 조회 -> 상품 ID로 상품 기본 정보를 조회한다 -> 상세 조회로 변경 및 반환타입 수정
     */
    public ProductDetailResponse getProduct(Long productId) { // 1. 반환 타입 변경
        Product foundProduct = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        if(foundProduct.getStatus() == ProductStatus.STOP) {
            throw new BusinessException(ErrorCode.PRODUCT_STOPPED);
        }

        return productMapper.toDetailResponse(foundProduct); // 2. toSummaryDto -> toDetailResponse
    }

    //PROD-F-06 - 슬라이더 이미지 조회
    public List<ProductImageDto> getSliderImages(Long productId) {
        validateProductExists(productId);

        List<ProductImage> sliderImages = productImageRepository.findSliderImagesByProductId(productId);

        if (sliderImages.isEmpty()) {
            throw new BusinessException(ErrorCode.IMAGE_NOT_FOUND);
        }

        return sliderImages.stream()
                .map(productMapper::toImageDto)
                .collect(Collectors.toList());
    }


    //main 이미지 조회
    public ProductImageDto getMainImage(Long productId) {
        validateProductExists(productId);

        ProductImage mainImage = productImageRepository
                .findFirstByProductIdAndImageTypeAndDeletedAtIsNull(productId, ImageType.MAIN)
                .orElseThrow(() -> new BusinessException(ErrorCode.IMAGE_NOT_FOUND));

        return productMapper.toImageDto(mainImage);
    }
    // 상세 설명 이미지 조회
    public List<ProductImageDto> getDescriptionImages(Long productId) {
        validateProductExists(productId);

        List<ProductImage> descImages = productImageRepository
                .findByProductIdAndImageTypeAndDeletedAtIsNullOrderByDisplayOrderAsc(
                        productId, ImageType.DESCRIPTION);

        return descImages.stream()
                .map(productMapper::toImageDto)
                .collect(Collectors.toList());
    }


    /**
     * 상품의 모든 이미지 조회
     */
    public List<ProductImageDto> getAllImages(Long productId) {
        validateProductExists(productId);

        List<ProductImage> images = productImageRepository.findByProductIdAndDeletedAtIsNull(productId);

        if (images.isEmpty()) {
            throw new BusinessException(ErrorCode.IMAGE_NOT_FOUND);
        }

        return images.stream()
                .map(productMapper::toImageDto)
                .collect(Collectors.toList());
    }

    // validate methods



    private void validateProductExists(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }
    }
    public Page<ProductSummaryDto> searchProducts(
            String keyword, ProductSortType sortType, Pageable pageable
    ) {
        String trimmed = validateAndNormalizeKeyword(keyword);
        sortType = sortType != null ? sortType : ProductSortType.LATEST;
        return productRepository.searchProducts(trimmed, sortType, pageable);
    }

    private String validateAndNormalizeKeyword(String keyword) {
        // 아무 것도 안 넣었거나 공백뿐이면
        if (!StringUtils.hasText(keyword)) {
            throw new BusinessException(ErrorCode.SEARCH_KEYWORD_REQUIRED);
        }

        String trimmed = keyword.trim();

        if (trimmed.length() < 2) {
            throw new BusinessException(ErrorCode.SEARCH_KEYWORD_TOO_SHORT);
        }

        return trimmed;
    }

    private void validateCategoryExists(Long categoryId) {
        if (categoryId != null && !categoryRepository.existsByIdAndDeletedAtIsNull(categoryId)) {
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
        }
    }

    private Product findProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
    }
    // 상태 검증
    private void validateProductActive(Product product) {
        if (product.getStatus() == ProductStatus.STOP) {
            throw new BusinessException(ErrorCode.PRODUCT_STOPPED);
        }
    }

}
