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
import com.example.elicesecondproject.mall.domain.product.repository.ProductRepository;
import com.example.elicesecondproject.mall.domain.product.repository.WishListRepository;
import com.example.elicesecondproject.mall.global.error.ErrorCode;
import com.example.elicesecondproject.mall.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final WishListRepository wishListRepository;
    private final CategoryService categoryService;
    private final MemberRepository memberRepository;

    // 하위 도메인 서비스
    private final ProductOptionService productOptionService;
    private final ProductImageService productImageService;

    public Page<ProductSummaryDto> getProductList(
            String keyword,
            Long categoryId,
            ProductSortType sortType,
            Pageable pageable,
            Long memberId
    ) {
        if (StringUtils.hasText(keyword)) {
            return searchProducts(keyword, sortType, pageable);
        }
        if (categoryId != null) {
            return getProductsByCategory(categoryId, true, sortType, pageable, memberId);
        }
        return getAllProducts(pageable, memberId, sortType);
    }

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

    public Page<ProductSummaryDto> getAllProducts(Pageable pageable, Long memberId, ProductSortType sortType) {
        ProductSortType finalSortType = sortType != null ? sortType : ProductSortType.LATEST;
        return productRepository.findAllProductsSummary(pageable, memberId, finalSortType);
    }

    public ProductDetailResponse getProduct(Long productId, Long memberId) {
        Product foundProduct = productRepository.findByIdAndDeletedAtIsNull(productId)
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

    public Page<ProductSummaryDto> getAllProductsForAdmin(Pageable pageable, ProductSortType sortType) {
        ProductSortType finalSortType = sortType != null ? sortType : ProductSortType.LATEST;
        return productRepository.findAllProductsForAdmin(pageable, finalSortType);
    }

    public Page<ProductSummaryDto> searchProductsForAdmin(String keyword, ProductSortType sortType, Pageable pageable) {
        String trimmed = validateAndNormalizeKeyword(keyword);
        sortType = sortType != null ? sortType : ProductSortType.LATEST;
        return productRepository.searchProductsForAdmin(trimmed, sortType, pageable);
    }

    @Transactional
    public ProductDetailResponse createProductWithFiles(
            CreateProductRequest request,
            MultipartFile mainImage,
            List<MultipartFile> sliderImages,
            List<MultipartFile> descImages
    ) {
        Category category = categoryService.getCategoryById(request.getCategoryId());

        Product product = new Product(
                request.getName(), request.getPrice(), request.getDiscountRate(),
                request.getDescription(), category, request.getStatus()
        );
        productRepository.save(product);

        productOptionService.updateOptionGroups(product, request.getOptionGroups());
        product.recalculateTotalStock();

        productImageService.uploadAndSaveImages(product, mainImage, sliderImages, descImages);

        return productMapper.toDetailResponse(product);
    }

    // [수정 완료] 상품 수정 로직 (이미지 증발 방지 및 정합성 보장)
    @Transactional
    public ProductDetailResponse updateProductWithFiles(
            Long productId,
            UpdateProductRequest request,
            MultipartFile mainImage,
            List<MultipartFile> sliderImages,
            List<MultipartFile> descImages
    ) {
        // 1. 기존 상품 조회
        Product product = findProductById(productId);

        // 2. 기본 정보 수정
        product.updateDetails(
                request.getName(), request.getPrice(), request.getDiscountRate(),
                request.getDescription(), request.getStatus()
        );

        // 3. 카테고리 수정
        if (request.getCategoryId() != null && !request.getCategoryId().equals(product.getCategory().getId())) {
            Category newCategory = categoryService.getCategoryById(request.getCategoryId());
            product.updateCategory(newCategory);
        }

        // 4. 옵션 수정 및 재고 재계산
        productOptionService.updateOptionGroups(product, request.getOptionGroups());
        product.recalculateTotalStock();

        // 5. 이미지 수정 로직 최신화
        productImageService.updateImages(product, request.getImages());

        if (isHasNewFiles(mainImage, sliderImages, descImages)) {
            productImageService.uploadAndSaveImages(product, mainImage, sliderImages, descImages);
        }

        return productMapper.toDetailResponse(product);
    }

    private boolean isHasNewFiles(MultipartFile main, List<MultipartFile> sliders, List<MultipartFile> descs) {
        return (main != null && !main.isEmpty()) ||
                (sliders != null && sliders.stream().anyMatch(f -> f != null && !f.isEmpty())) ||
                (descs != null && descs.stream().anyMatch(f -> f != null && !f.isEmpty()));
    }

    @Transactional
    public void deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        product.delete();
    }

    public Page<ProductSummaryDto> searchProducts(
            String keyword, ProductSortType sortType, Pageable pageable
    ) {
        String trimmed = validateAndNormalizeKeyword(keyword);
        sortType = sortType != null ? sortType : ProductSortType.LATEST;
        return productRepository.searchProducts(trimmed, sortType, pageable);
    }

    private String validateAndNormalizeKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            throw new BusinessException(ErrorCode.SEARCH_KEYWORD_REQUIRED);
        }
        String trimmed = keyword.trim();
        if (trimmed.length() < 2) {
            throw new BusinessException(ErrorCode.SEARCH_KEYWORD_TOO_SHORT);
        }
        return trimmed;
    }

    @Transactional
    public void bulkDeleteProducts(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        List<Product> products = productRepository.findAllById(productIds);
        if (products.isEmpty()) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        products.forEach(Product::delete);
    }

    @Transactional
    public void bulkUpdateStatus(List<Long> productIds, ProductStatus status) {
        if (productIds == null || productIds.isEmpty() || status == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        List<Product> products = productRepository.findAllById(productIds);
        if (products.isEmpty()) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        products.forEach(product -> product.updateStatus(status));
    }

    @Transactional
    public WishListToggleResponseDto addWish(Long memberId, Long productId) {
        Member member = findMemberById(memberId);
        Product product = findProductById(productId);

        if(wishListRepository.existsByMemberIdAndProductId(memberId, productId)) {
            return new WishListToggleResponseDto(true, product.getWishListCount());
        }

        WishList wishList = WishList.builder()
                .member(member)
                .product(product)
                .build();
        wishListRepository.save(wishList);
        product.increaseWishListCount();

        return new WishListToggleResponseDto(true, product.getWishListCount());
    }

    @Transactional
    public WishListToggleResponseDto removeWish(Long memberId, Long productId) {
        findMemberById(memberId);
        Product product = findProductById(productId);

        WishList wish = wishListRepository.findByMemberIdAndProductId(memberId, productId)
                .orElse(null);

        if(wish == null) {
            return new WishListToggleResponseDto(false, product.getWishListCount());
        }

        wishListRepository.delete(wish);
        product.decreaseWishListCount();

        return new WishListToggleResponseDto(false, product.getWishListCount());
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
}