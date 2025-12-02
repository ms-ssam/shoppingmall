package com.example.elicesecondproject.mall.domain.product.service;

import com.example.elicesecondproject.mall.domain.category.entity.Category;
import com.example.elicesecondproject.mall.domain.category.repository.CategoryRepository;
import com.example.elicesecondproject.mall.domain.option.dto.OptionDetailDto;
import com.example.elicesecondproject.mall.domain.option.dto.ProductOptionGroupDto;
import com.example.elicesecondproject.mall.domain.option.entity.OptionDetail;
import com.example.elicesecondproject.mall.domain.option.entity.ProductOptionGroup;
import com.example.elicesecondproject.mall.domain.option.mapper.OptionMapper;
import com.example.elicesecondproject.mall.domain.option.repository.ProductOptionGroupRepository;
import com.example.elicesecondproject.mall.domain.product.dto.CreateProductRequest;
import com.example.elicesecondproject.mall.domain.product.dto.ProductImageDto;
import com.example.elicesecondproject.mall.domain.product.dto.ProductSummaryDto;
import com.example.elicesecondproject.mall.domain.product.entity.ImageType;
import com.example.elicesecondproject.mall.domain.product.entity.Product;
import com.example.elicesecondproject.mall.domain.product.entity.ProductImage;
import com.example.elicesecondproject.mall.domain.product.mapper.ProductMapper;
import com.example.elicesecondproject.mall.domain.product.repository.ProductRepository;
import com.example.elicesecondproject.mall.global.exception.BusinessException;
import com.example.elicesecondproject.mall.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductOptionGroupRepository productOptionGroupRepository;
    private final ProductMapper productMapper;
    private final OptionMapper optionMapper;

    //PROD-F-01 (상품 전체 조회)
    public Page<ProductSummaryDto> getAllProducts(Pageable pageable) {
            return productRepository.findByDeletedAtIsNull(pageable)
                    .map(productMapper::toSummaryDto);
    }

    //PROD-REG-F-01 (상품 등록)
    @Transactional
    public Long createProduct(CreateProductRequest dto) {

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        Product product = productMapper.toEntity(dto);

        product.setCategory(category);

        for (ProductOptionGroupDto groupDto : dto.getOptionGroups()) {
            ProductOptionGroup group = ProductOptionGroup.builder()
                    .name(groupDto.getName())
                    .displayOrder(groupDto.getDisplayOrder())
                    .build();

            for (OptionDetailDto detailDto : groupDto.getDetails()) {
                OptionDetail detail = OptionDetail.builder()
                        .name(detailDto.getName())
                        .sku(detailDto.getSku())
                        .addPrice(detailDto.getAddPrice())
                        .stockQuantity(detailDto.getStockQuantity())
                        .displayOrder(detailDto.getDisplayOrder())
                        .build();

                group.addDetail(detail);   // group <-> detail 연관관계 세팅 (cascade 필요)
            }

            for (ProductImageDto imageDto : dto.getImages()) {
                ProductImage image = ProductImage.builder()
                        .imageUrl(imageDto.getImageUrl())
                        .imageType(imageDto.getImageType())
                        .displayOrder(imageDto.getDisplayOrder())
                        .build();
                if(imageDto.getImageType() == ImageType.COLOR){
                    group.addImage(image);
                }
                product.addImage(image);  // product <-> image
            }

            product.addOptionGroup(group);  // product <-> group 연관관계 세팅
        }


        /*for (ProductImageDto imageDto : dto.getImages()) {
            ProductOptionGroup productOptionGroup = productOptionGroupRepository.findById(product.getId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND)); //TODO: 에러코드는 추후 수정

            ProductImage image =  ProductImage.builder()
                    .imageUrl(imageDto.getImageUrl())
                    .imageType(imageDto.getImageType())
                    .displayOrder(imageDto.getDisplayOrder())
                    .productOptionGroup(productOptionGroup)
                    .build();

            product.addImage(image);        // product <-> image 연관관계 세팅
        }*/

        Product saved = productRepository.save(product);  // <- 여기서 한방에 영속화

        return saved.getId();
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
