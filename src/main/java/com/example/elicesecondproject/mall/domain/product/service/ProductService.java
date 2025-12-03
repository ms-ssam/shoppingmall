package com.example.elicesecondproject.mall.domain.product.service;

import com.example.elicesecondproject.mall.domain.category.entity.Category;
import com.example.elicesecondproject.mall.domain.category.repository.CategoryRepository;
import com.example.elicesecondproject.mall.domain.category.service.CategoryService;
import com.example.elicesecondproject.mall.domain.member.entity.Member;
import com.example.elicesecondproject.mall.domain.member.entity.MemberDetail;
import com.example.elicesecondproject.mall.domain.member.repositorty.MemberRepository;
import com.example.elicesecondproject.mall.domain.option.entity.OptionDetail;
import com.example.elicesecondproject.mall.domain.option.entity.ProductOptionGroup;
import com.example.elicesecondproject.mall.domain.option.service.ProductOptionService;
import com.example.elicesecondproject.mall.domain.product.dto.*;
import com.example.elicesecondproject.mall.domain.product.entity.*;
import com.example.elicesecondproject.mall.domain.product.mapper.ProductMapper;
import com.example.elicesecondproject.mall.domain.product.repository.ProductImageRepository;
import com.example.elicesecondproject.mall.domain.product.repository.ProductRepository;
import com.example.elicesecondproject.mall.domain.product.repository.ProductRepositoryCustom;
import com.example.elicesecondproject.mall.domain.product.repository.WishListRepository;
import com.example.elicesecondproject.mall.global.exception.BusinessException;
import com.example.elicesecondproject.mall.global.exception.ErrorCode;
import com.example.elicesecondproject.mall.global.service.ProductImageFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;
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
    private final WishListRepository wishListRepository;
    private final CategoryService categoryService;
    private final MemberRepository memberRepository;


    // 하위 도메인 서비스로 분리 후 주입(옵션, 이미지)
    private final ProductOptionService productOptionService;
    private final ProductImageService productImageService;

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

    // DTL-F-01 : 상품 기본 정보 조회 -> 상품 ID로 상품 기본 정보를 조회한다.
    public ProductDetailResponse getProduct(Long productId) {
        Product foundProduct = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));  // 삭제된 제품 404 Error

        if (foundProduct.getStatus() == ProductStatus.STOP) {
            throw new BusinessException(ErrorCode.PRODUCT_STOPPED);  // 판매중지 상품 400 error "현재 판매하지 않는 상품입니다" 메시지
        }

        return productMapper.toDetailResponse(foundProduct); // 2. toSummaryDto -> toDetailResponse
    }

    /*//PROD-F-06 - 슬라이더 이미지 조회 -> 상품 조회로 대체 가능
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



    //PROD-REG-F-10 상품 등록
    @Transactional
    public ProductDetailResponse createProduct(CreateProductRequest request) {
        // 1. 카테고리 조회 (Service 이용 권장)
        Category category = categoryService.getCategoryById(request.getCategoryId());

        // 2. 상품 엔티티 생성 (기본 정보 세팅)
        Product product = new Product(
                request.getName(),
                request.getPrice(),
                request.getDiscountRate(),
                request.getDescription(),
                category,
                request.getStatus()
        );

        // 색상 옵션 저장
        productOptionService.updateOptionGroups(product, request.getOptionGroups());

        // 재고 합계 계산
        product.recalculateTotalStock();

        // 이미지 등록
        productImageService.updateImages(product, request.getImages());
        productRepository.save(product);

        return productMapper.toDetailResponse(product);
    }







    // PROD-REG-F-11(관리자) 상품 상세 수정
    @Transactional
    public ProductDetailResponse updateProduct(Long productId, UpdateProductRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        // 1. 기본 정보 수정
        product.updateDetails(
                request.getName(),
                request.getPrice(),
                request.getDiscountRate(),
                request.getDescription(),
                request.getStatus()
        );

        // 2. 카테고리 수정
        if (request.getCategoryId() != null) {
            Category currentCategory = product.getCategory();
            if (currentCategory == null || !currentCategory.getId().equals(request.getCategoryId())) {
                Category newCategory = categoryService.getCategoryById(request.getCategoryId());
                product.updateCategory(newCategory);
            }
        }


        // 3. 옵션 그룹 비교 수정
        productOptionService.updateOptionGroups(product, request.getOptionGroups());

        product.recalculateTotalStock();

        // 4. 이미지 비교 수정
        productImageService.updateImages(product, request.getImages());

        return productMapper.toDetailResponse(product);
    }

    @Transactional
    public void deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        // 엔티티의 비즈니스 로직 호출 (status = STOP)
        product.delete();
    }


//-------------------------
// validate methods
// -------------------------


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

    // 상태 검증
    private void validateProductActive(Product product) {
        if (product.getStatus() == ProductStatus.STOP) {
            throw new BusinessException(ErrorCode.PRODUCT_STOPPED);
        }
    }

    /*
      PROD-F-03 키워드 검색
      - 검색 대상: Product.name, Product.description, Category.name
      - 부분 일치 검색
     */
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

    // DTL-F-12 : 찜 상태 조회 -> 사용자의 찜 상태를 조회한다.
    /*
        사용자에게 상품들 보여주는 화면에서 이 기능 메서드가 true 반환하는지 false 반환하는지에 따라 뷰에서 하트 비울지 채울지 결정하는 용도
        TODO : [UI 반영]좋아요 상태: 하트 아이콘 채우기 (노란색)
               [로그인]비로그인 시 기본 회색
               뷰 작업할 때 반영하기
     */
    public boolean isInWishList(Long memberId, Long productId) {
        if(!productRepository.existsById(productId)) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);  // 존재하지 않는 상품이라면 404 error
        }

        return wishListRepository.existsByMemberIdAndProductId(memberId, productId);
    }

    // DTL-F-13 : 찜 추가/제거 토글 -> 찜 버튼 클릭 시 추가/제거한다.
    // TODO: 뷰 작업 시 하트 확대 효과 애니메이션
    @Transactional
    public WishListToggleResponseDto addWish(Long memberId, Long productId) {
        Member member = findMemberById(memberId);
        Product product = findProductById(productId);

        // 이미 찜 되어있는 경우
        if(wishListRepository.existsByMemberIdAndProductId(memberId, productId)) {
            return new WishListToggleResponseDto(true, product.getWishListCount());  // 멱등성 보장
        }

        // 찜 X 경우
        WishList wishList = WishList.builder()  // FIXME: 나중에 정적 스태틱 메서드로 수정하면 좋을 것 같습니다. (생성 의미 명확 + 코드 길이 감소)
                .member(member)
                .product(product)
                .build();
        wishListRepository.save(wishList);
        product.increaseWishListCount();  // product 내부 낙관적 락 + count 처리

        return new WishListToggleResponseDto(true, product.getWishListCount());
    }

    @Transactional
    public WishListToggleResponseDto removeWish(Long memberId, Long productId) {
        Member member = findMemberById(memberId);
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
}
