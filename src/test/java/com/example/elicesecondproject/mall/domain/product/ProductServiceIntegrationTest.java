package com.example.elicesecondproject.mall.domain.product;

import com.example.elicesecondproject.mall.domain.product.dto.CreateProductRequest;
import com.example.elicesecondproject.mall.domain.product.dto.ProductDetailResponse;
import com.example.elicesecondproject.mall.domain.product.entity.ProductStatus;
import com.example.elicesecondproject.mall.domain.product.repository.ProductRepository;
import com.example.elicesecondproject.mall.domain.product.service.ProductService;
import com.example.elicesecondproject.mall.global.config.FileConfig;
import com.example.elicesecondproject.mall.global.error.exception.BusinessException;
import com.example.elicesecondproject.mall.global.service.FileService;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional; // import 유지
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
@ActiveProfiles("local")
public class ProductServiceIntegrationTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private FileConfig fileConfig;

    @SpyBean
    private FileService fileService;

    @AfterEach
    void tearDown() {
        // 파일 정리
        Path productsPath = Paths.get(fileConfig.getBasePath(), fileConfig.getProductPath());
        if (Files.exists(productsPath)) {
            try {
                FileSystemUtils.deleteRecursively(productsPath);
            } catch (IOException e) {
                System.out.println("테스트 파일 삭제 실패" + e.getMessage());
            }
        }
    }

    // 1. 성공 케이스
    @Test
    @Transactional // 성공 케이스는 DB 오염 방지를 위해 롤백 필요
    @DisplayName("[성공] 정상적인 상품 정보와 이미지를 입력하면 등록에 성공한다.")
    void createProductSuccessTest() throws Exception {
        // given
        CreateProductRequest request = createDefaultRequest();
        MockMultipartFile image = createMockImage("main.jpg", "image/jpeg");

        // when
        ProductDetailResponse response = productService.createProductWithFiles(request, image, null, null);

        // then
        assertThat(response.getId()).isNotNull();
        assertThat(response.getMainImageUrl()).contains("/uploads/");

        Path filePath = convertWebUrlToPhysicalPath(response.getMainImageUrl());
        assertThat(Files.exists(filePath)).isTrue();
    }

    // 실패 케이스: 할인률 오류, 파일 확장자, 디스크 오류(이미지 오류시 상품 DB 롤백이 되는지)

    // 2. Boundary Test
    @Test
    @Transactional
    @DisplayName("[경계값] 할인율이 0보다 작거나 100보다 크면 예외가 발생한다.")
    void createProductBoundaryTest() {
        // given
        CreateProductRequest requestOver = CreateProductRequest.builder()
                .name("테스트 상품")
                .price(10000)
                .discountRate(101)
                .categoryId(1L)
                .status(ProductStatus.SELLING)
                .optionGroups(Collections.emptyList())
                .build();

        CreateProductRequest requestUnder = CreateProductRequest.builder()
                .name("테스트 상품")
                .price(10000)
                .discountRate(-1)
                .categoryId(1L)
                .status(ProductStatus.SELLING)
                .optionGroups(Collections.emptyList())
                .build();

        MockMultipartFile image = createMockImage("main.jpg", "image/jpeg");

        // when & then
        assertThatThrownBy(() -> productService.createProductWithFiles(requestOver, image, null, null))
                .satisfies(e -> {
                    boolean isMatch = (e instanceof ConstraintViolationException) ||
                            (e.getCause() instanceof ConstraintViolationException);
                    assertThat(isMatch).as("예외는 ConstraintViolationException이어야 합니다.").isTrue();
                });

        assertThatThrownBy(() -> productService.createProductWithFiles(requestUnder, image, null, null))
                .satisfies(e -> {
                    boolean isMatch = (e instanceof ConstraintViolationException) ||
                            (e.getCause() instanceof ConstraintViolationException);
                    assertThat(isMatch).as("예외는 ConstraintViolationException이어야 합니다.").isTrue();
                });
    }

    // 3. Edge Case
    @Test
    @Transactional // [수정 2]
    @DisplayName("[엣지] 지원하지 않는 파일 확장자(.exe)를 업로드하면 예외가 발생한다.")
    void createProductEdgeTest() {
        // given
        CreateProductRequest request = createDefaultRequest();
        MockMultipartFile exeFile = createMockImage("virus.exe", "application/x-msdownload");

        // when & then
        assertThatThrownBy(() -> productService.createProductWithFiles(request, exeFile, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("허용되지 않은 확장자");
    }

    // 4. Error Handling (롤백 테스트)
    @Test
    @DisplayName("[에러처리] 파일 저장 중 IO 에러가 발생하면 DB 저장 내용도 롤백되어야 한다.")
    void createProductRollbackTest() throws Exception {
        // given
        long initialCount = productRepository.count();

        CreateProductRequest request = createDefaultRequest();
        MockMultipartFile image = createMockImage("main.jpg", "image/jpeg");

        // [Spy] 강제 예외
        doThrow(new IOException("디스크 에러 시뮬레이션"))
                .when(fileService).saveImage(any(), any(), any());

        // when
        assertThatThrownBy(() -> productService.createProductWithFiles(request, image, null, null))
                .isInstanceOf(BusinessException.class);

        // then
        long finalCount = productRepository.count();
        // 트랜잭션이 분리되었으므로, 실제 DB에는 데이터가 롤백되어 개수가 늘어나지 않아야 함
        assertThat(finalCount).isEqualTo(initialCount);
    }

    // --- Helper Methods ---
    private CreateProductRequest createDefaultRequest() {
        return CreateProductRequest.builder()
                .name("테스트 상품")
                .price(10000)
                .discountRate(30)
                .categoryId(1L)
                .status(ProductStatus.SELLING)
                .optionGroups(Collections.emptyList())
                .build();
    }

    private MockMultipartFile createMockImage(String name, String contentType) {
        return new MockMultipartFile("mainImage", name, contentType, "dummy-data".getBytes());
    }

    private Path convertWebUrlToPhysicalPath(String webUrl) {
        String relative = webUrl.replace("/uploads/", "");
        return Paths.get(fileConfig.getBasePath(), relative);
    }
}