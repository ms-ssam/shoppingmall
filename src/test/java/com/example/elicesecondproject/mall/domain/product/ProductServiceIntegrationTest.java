package com.example.elicesecondproject.mall.domain.product;

import com.example.elicesecondproject.mall.domain.category.entity.Category;
import com.example.elicesecondproject.mall.domain.category.repository.CategoryRepository;
import com.example.elicesecondproject.mall.domain.product.dto.CreateProductRequest;
import com.example.elicesecondproject.mall.domain.product.dto.ProductDetailResponse;
import com.example.elicesecondproject.mall.domain.product.entity.ProductStatus;
import com.example.elicesecondproject.mall.domain.product.repository.ProductRepository;
import com.example.elicesecondproject.mall.domain.product.service.ProductService;
import com.example.elicesecondproject.mall.global.config.FileConfig;
import com.example.elicesecondproject.mall.global.error.exception.BusinessException;
import com.example.elicesecondproject.mall.global.service.FileService;

import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.TestPropertySource; // 추가됨
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.context.TestConstructor.AutowireMode.ALL;

@SpringBootTest
@ActiveProfiles("test")
@TestConstructor(autowireMode = ALL)
@RequiredArgsConstructor
// 👇 [핵심] data.sql 실행을 막아서 ID 충돌 방지 (깨끗한 DB에서 시작)
@TestPropertySource(properties = "spring.sql.init.mode=never")
public class ProductServiceIntegrationTest {

    private final ProductService productService;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final FileConfig fileConfig;

    @SpyBean
    private FileService fileService;

    private Long savedCategoryId;

    @BeforeEach
    void setUp() {
        Category category = Category.builder()
                .name("INTEGRATION_TEST_CATEGORY")
                .slug("test-slug-" + UUID.randomUUID())
                .displayOrder(999)
                .isVisible(true)
                .depth(0)
                .build();

        Category savedCategory = categoryRepository.save(category);

        // 경로 완성 (선택)
        categoryRepository.save(savedCategory);

        this.savedCategoryId = savedCategory.getId();
    }

    @AfterEach
    void tearDown() {
        // 1. 파일 정리
        Path productsPath = Paths.get(fileConfig.getBasePath(), fileConfig.getProductPath());
        if (Files.exists(productsPath)) {
            try {
                FileSystemUtils.deleteRecursively(productsPath);
            } catch (IOException e) {
                System.out.println("테스트 파일 삭제 실패: " + e.getMessage());
            }
        }

        // 2. 데이터 정리
        try {
            productRepository.deleteAll();
            categoryRepository.deleteAll();
        } catch (Exception e) {
            System.out.println("데이터 정리 중 오류 발생 (무시 가능): " + e.getMessage());
        }
    }

    // 1. 성공 케이스
    @Test
    @Transactional
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
                .categoryId(savedCategoryId)
                .status(ProductStatus.SELLING)
                .optionGroups(Collections.emptyList())
                .build();

        CreateProductRequest requestUnder = CreateProductRequest.builder()
                .name("테스트 상품")
                .price(10000)
                .discountRate(-1)
                .categoryId(savedCategoryId)
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
    @Transactional
    @DisplayName("[엣지] 지원하지 않는 파일 확장자(예시:.exe)를 업로드하면 예외가 발생한다.")
    void createProductEdgeTest() {
        // given
        CreateProductRequest request = createDefaultRequest();
        MockMultipartFile exeFile = createMockImage("virus.exe", "application/x-msdownload");

        // when & then
        assertThatThrownBy(() -> productService.createProductWithFiles(request, exeFile, null, null))
                .satisfies(e -> {
                    boolean isMatch = (e instanceof IllegalArgumentException) ||
                            (e instanceof BusinessException) ||
                            (e.getCause() instanceof IllegalArgumentException);
                    assertThat(isMatch).as("예외는 IllegalArgumentException이어야 합니다.").isTrue();
                });
    }

    // 4. Error Handling (롤백 테스트)
    @Test
    @DisplayName("[에러처리] 파일 저장 중 IO 에러가 발생하면 DB 저장 내용도 롤백되어야 한다.")
    void createProductRollbackTest() throws Exception {
        // given
        String uniqueName = "ROLLBACK_CHECK_" + UUID.randomUUID();

        CreateProductRequest request = CreateProductRequest.builder()
                .name(uniqueName)
                .price(10000)
                .discountRate(30)
                .categoryId(savedCategoryId)
                .status(ProductStatus.SELLING)
                .optionGroups(Collections.emptyList())
                .build();

        MockMultipartFile image = createMockImage("main.jpg", "image/jpeg");

        // Spy를 통해 강제 에러 주입
        doThrow(new IOException("디스크 에러 시뮬레이션"))
                .when(fileService).saveImage(any(), any(), any());

        // when
        assertThatThrownBy(() -> productService.createProductWithFiles(request, image, null, null))
                .isInstanceOf(BusinessException.class);

        // then
        boolean exists = productRepository.findAll().stream()
                .anyMatch(p -> p.getName().equals(uniqueName));
        assertThat(exists).isFalse();
    }

    // --- Helper Methods ---
    private CreateProductRequest createDefaultRequest() {
        return CreateProductRequest.builder()
                .name("테스트 상품")
                .price(10000)
                .discountRate(30)
                .categoryId(savedCategoryId)
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