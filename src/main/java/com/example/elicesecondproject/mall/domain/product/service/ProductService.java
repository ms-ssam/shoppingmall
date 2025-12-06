package com.example.elicesecondproject.mall.domain.product.service;

import com.example.elicesecondproject.mall.domain.category.entity.Category;
import com.example.elicesecondproject.mall.domain.category.repository.CategoryRepository;
import com.example.elicesecondproject.mall.domain.category.service.CategoryService;
import com.example.elicesecondproject.mall.domain.member.entity.Member;
import com.example.elicesecondproject.mall.domain.member.repositorty.MemberRepository;
import com.example.elicesecondproject.mall.domain.option.service.ProductOptionService;
import com.example.elicesecondproject.mall.domain.product.dto.*;
import com.example.elicesecondproject.mall.domain.product.entity.*;
import com.example.elicesecondproject.mall.domain.product.mapper.ProductMapper;
import com.example.elicesecondproject.mall.domain.product.repository.ProductImageRepository;
import com.example.elicesecondproject.mall.domain.product.repository.ProductRepository;
import com.example.elicesecondproject.mall.domain.product.repository.WishListRepository;
import com.example.elicesecondproject.mall.global.exception.BusinessException;
import com.example.elicesecondproject.mall.global.exception.ErrorCode;
import com.example.elicesecondproject.mall.global.service.ProductImageFileService;
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
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ProductImageRepository productImageRepository;
    private final WishListRepository wishListRepository;
    private final CategoryService categoryService;
    private final MemberRepository memberRepository;
    private final ProductOptionService productOptionService;
    private final ProductImageService productImageService;

    // 카테고리별 상품 조회
    public Page<ProductSummaryDto> getProductsByCategory(
            Long categoryId,
            Boolean includeSubCategories,
            ProductSortType sortType,
            Pageable pageable,
            Long memberId) {

        validateCategoryExists(categoryId);

        includeSubCategories = includeSubCategories != null ? includeSubCategories : false;
        sortType = sortType != null ? sortType : ProductSortType.LATEST;

        return productRepository.findProductsByCategory(
                categoryId,
                includeSubCategories,
                sortType,
                pageable,
                memberId
        );
    }

    // [수정] 전체 상품 조회 (sortType 추가)
    public Page<ProductSummaryDto> getAllProducts(Pageable pageable, Long memberId, ProductSortType sortType) {
        ProductSortType finalSortType = sortType != null ? sortType : ProductSortType.LATEST;
        return productRepository.findAllProductsSummary(pageable, memberId, finalSortType);
    }

    // 상품 기본 정보 조회
    public ProductDetailResponse getProduct(Long productId, Long memberId) {
        Product foundProduct = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        if (foundProduct.getStatus() == ProductStatus.STOP) {
            throw new BusinessException(ErrorCode.PRODUCT_STOPPED);
        }

        ProductDetailResponse response = productMapper.toDetailResponse(foundProduct);

        if (memberId != null) {
            boolean isLiked = wishListRepository.existsByMemberIdAndProductId(memberId, productId);
            response.setLiked(isLiked);
        }

        return response;
    }

    public ProductDetailResponse getProduct(Long productId) {
        return getProduct(productId, null);
    }

    public List<ProductImageDto> getAllImages(Long productId) {
        validateProductExists(productId);
        List<ProductImage> images = productImageRepository.findByProductIdAndDeletedAtIsNull(productId);
        if (images.isEmpty()) {
            throw new BusinessException(ErrorCode.IMAGE_NOT_FOUND);
        }
        return images.stream().map(productMapper::toImageDto).collect(Collectors.toList());
    }

    @Transactional
    public ProductDetailResponse createProduct(CreateProductRequest request) {
        Category category = categoryService.getCategoryById(request.getCategoryId());
        Product product = new Product(
                request.getName(), request.getPrice(), request.getDiscountRate(),
                request.getDescription(), category, request.getStatus()
        );
        productOptionService.updateOptionGroups(product, request.getOptionGroups());
        product.recalculateTotalStock();
        productImageService.updateImages(product, request.getImages());
        productRepository.save(product);
        return productMapper.toDetailResponse(product);
    }

    @Transactional
    public ProductDetailResponse updateProduct(Long productId, UpdateProductRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        product.updateDetails(
                request.getName(), request.getPrice(), request.getDiscountRate(),
                request.getDescription(), request.getStatus()
        );

        if (request.getCategoryId() != null) {
            Category currentCategory = product.getCategory();
            if (currentCategory == null || !currentCategory.getId().equals(request.getCategoryId())) {
                Category newCategory = categoryService.getCategoryById(request.getCategoryId());
                product.updateCategory(newCategory);
            }
        }

        productOptionService.updateOptionGroups(product, request.getOptionGroups());
        product.recalculateTotalStock();
        productImageService.updateImages(product, request.getImages());

        return productMapper.toDetailResponse(product);
    }

    @Transactional
    public void deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        product.delete();
    }

    private void validateProductExists(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }
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

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    public Page<ProductSummaryDto> searchProducts(String keyword, ProductSortType sortType, Pageable pageable) {
        String trimmed = validateAndNormalizeKeyword(keyword);
        sortType = sortType != null ? sortType : ProductSortType.LATEST;
        return productRepository.searchProducts(trimmed, sortType, pageable);
    }

    private String validateAndNormalizeKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) throw new BusinessException(ErrorCode.SEARCH_KEYWORD_REQUIRED);
        String trimmed = keyword.trim();
        if (trimmed.length() < 2) throw new BusinessException(ErrorCode.SEARCH_KEYWORD_TOO_SHORT);
        return trimmed;
    }

    public boolean isInWishList(Long memberId, Long productId) {
        if (!productRepository.existsById(productId)) throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        return wishListRepository.existsByMemberIdAndProductId(memberId, productId);
    }

    @Transactional
    public WishListToggleResponseDto addWish(Long memberId, Long productId) {
        Member member = findMemberById(memberId);
        Product product = findProductById(productId);
        if (wishListRepository.existsByMemberIdAndProductId(memberId, productId)) {
            return new WishListToggleResponseDto(true, product.getWishListCount());
        }
        WishList wishList = WishList.builder().member(member).product(product).build();
        wishListRepository.save(wishList);
        product.increaseWishListCount();
        return new WishListToggleResponseDto(true, product.getWishListCount());
    }

    @Transactional
    public WishListToggleResponseDto removeWish(Long memberId, Long productId) {
        Member member = findMemberById(memberId);
        Product product = findProductById(productId);
        WishList wish = wishListRepository.findByMemberIdAndProductId(memberId, productId).orElse(null);
        if (wish == null) {
            return new WishListToggleResponseDto(false, product.getWishListCount());
        }
        wishListRepository.delete(wish);
        product.decreaseWishListCount();
        return new WishListToggleResponseDto(false, product.getWishListCount());
    }
}