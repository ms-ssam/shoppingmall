package com.example.elicesecondproject.mall.domain.category.service;

import com.example.elicesecondproject.mall.domain.category.dto.*;
import com.example.elicesecondproject.mall.domain.category.entity.Category;
import com.example.elicesecondproject.mall.domain.category.mapper.CategoryMapper;
import com.example.elicesecondproject.mall.domain.category.repository.CategoryRepository;
import com.example.elicesecondproject.mall.global.exception.BusinessException;
import com.example.elicesecondproject.mall.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public Category getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
    }

    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        // 1. 중복 Slug 체크
        if (categoryRepository.existsBySlug(request.getSlug())) {
            throw new BusinessException(ErrorCode.DUPLICATE_CATEGORY_SLUG);
        }

        // 2. 엔티티 생성
        Category category = Category.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .displayOrder(request.getDisplayOrder())
                .isVisible(request.getIsVisible())
                .build();

        // 3. 부모 카테고리 설정
        if (request.getParentId() != null) {
            Category parent = getCategoryById(request.getParentId());

            // depth 2단계 제한 (0: 루트, 1: 자식 → 2 이상 금지(대분류, 소분류))
            if (parent.getDepth() >= 1) {
                throw new BusinessException(ErrorCode.CATEGORY_DEPTH_EXCEEDED);
            }

            category.setParent(parent);  // setParent 안에서 depth, path, children 연동
        } else {
            category.setParent(null);    // 최상위 카테고리
        }

        // 4. 저장
        categoryRepository.save(category);

        // 5. 경로 완성 (ID가 생성된 후 처리)
        category.completePath();

        return categoryMapper.toResponse(category);
    }

    // 카테고리 수정
    @Transactional
    public CategoryResponse updateCategory(Long categoryId, UpdateCategoryRequest request) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        // 1. 기본 정보 수정
        category.updateDetails(
                request.getName(),
                request.getSlug(),
                request.getIsVisible(),
                request.getDisplayOrder()
        );

        // 2. 부모 카테고리 변경 감지 및 처리
        if (request.getParentId() != null) {
            // 기존 부모 ID와 요청된 부모 ID가 다를 경우에만 로직 수행
            Long currentParentId = category.getParent() != null ? category.getParent().getId() : null;

            if (!Objects.equals(currentParentId, request.getParentId())) {
                // 자기 자신을 부모로 설정하는 경우 예외 처리
                if (category.getId().equals(request.getParentId())) {
                    throw new BusinessException(ErrorCode.CATEGORY_CANNOT_BE_ITS_OWN_PARENT);
                }

                Category newParent = categoryRepository.findById(request.getParentId())
                        .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

                // 부모 변경 및 경로 갱신
                category.setParent(newParent);
                category.completePath();

                // [핵심] 하위 카테고리들도 재귀적으로 경로 업데이트
                updateChildrenPath(category);
            }
        }
        // 부모를 제거하여 최상위로 만드는 경우 (request.getParentId()가 0 또는 특정 값일 때 처리 필요 시 추가)

        return categoryMapper.toResponse(category);
    }

    // 재귀적으로 자식들의 Path와 Depth를 갱신하는 메서드
    private void updateChildrenPath(Category parent) {
        List<Category> children = parent.getChildren();
        if (children == null || children.isEmpty()) {
            return;
        }

        for (Category child : children) {
            // 부모(parent)의 바뀐 path와 depth를 기반으로 자식 갱신
            child.setParent(parent);
            child.completePath(); // path 문자열 재계산

            // 손자, 증손자 등 하위로 계속 전파
            updateChildrenPath(child);
        }
    }

    // 카테고리 삭제 (Soft Delete)
    @Transactional
    public void deleteCategory(Long categoryId) {
        Category category = getCategoryById(categoryId);

        // 하위 카테고리가 있으면 삭제 불가
        if (!category.getChildren().isEmpty()) {
            throw new BusinessException(ErrorCode.CATEGORY_HAS_CHILDREN);
        }

        category.softDelete(); // Soft Delete & 필요시 자식 전파
    }

    // 사용자용 카테고리 트리 (루트 + 자식들)
    public List<CategoryTreeResponse> getCategoryTree() {
        List<Category> rootCategories = categoryRepository
                .findByParentIsNullAndDeletedAtIsNullAndIsVisibleTrue();

        return rootCategories.stream()
                .map(categoryMapper::toTreeResponse)  // 이 안에서 children 재귀 매핑한다고 가정
                .sorted(Comparator.comparing(CategoryTreeResponse::getDisplayOrder))
                .collect(Collectors.toList());
    }

    // 관리자용 전체 카테고리 평면 목록
    public List<CategoryResponse> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .map(categoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    // 노출 상태 토글 (숨김/보임)
    @Transactional
    public void toggleVisibility(Long categoryId) {
        Category category = getCategoryById(categoryId);
        category.updateDetails(
                null,
                null,
                !category.getIsVisible(),
                null
        );
    }
}
