package com.example.elicesecondproject.mall.domain.category.service;

import com.example.elicesecondproject.mall.domain.category.dto.*;
import com.example.elicesecondproject.mall.domain.category.entity.Category;
import com.example.elicesecondproject.mall.domain.category.mapper.CategoryMapper;
import com.example.elicesecondproject.mall.domain.category.repository.CategoryRepository;
import com.example.elicesecondproject.mall.global.error.exception.BusinessException;
import com.example.elicesecondproject.mall.global.error.ErrorCode;
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

            category.setParent(parent);
        } else {
            category.setParent(null);    // 최상위 카테고리
        }

        // 4. 저장
        categoryRepository.save(category);

        return categoryMapper.toResponse(category);
    }

    // 카테고리 수정
    @Transactional
    public CategoryResponse updateCategory(Long categoryId, UpdateCategoryRequest request) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        boolean oldVisibility = category.getIsVisible();

        // 1. 기본 정보 수정
        category.updateDetails(
                request.getName(),
                request.getSlug(),
                request.getIsVisible(),
                request.getDisplayOrder()
        );

        // [수정] 노출 상태 상위 카테고리 변경 시 하위 카테고리도 변경
        if (oldVisibility != request.getIsVisible() && !category.getChildren().isEmpty()) {
            updateChildrenVisibility(category, request.getIsVisible());
        }


        return categoryMapper.toResponse(category);
    }

    // 재귀적으로 Depth를 갱신하는 메서드

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
        boolean newVisibility = !category.getIsVisible();

        // 현재 카테고리 노출 상태 변경
        category.updateDetails(
                null,
                null,
                newVisibility,
                null
        );

        // 하위 카테고리가 있으면 함께 변경
        if (!category.getChildren().isEmpty()) {
            updateChildrenVisibility(category, newVisibility);
        }
    }

    //  재귀적으로 하위 카테고리의 노출 상태 변경
    private void updateChildrenVisibility(Category parent, boolean isVisible) {
        List<Category> children = parent.getChildren();
        if (children == null || children.isEmpty()) {
            return;
        }

        for (Category child : children) {
            child.updateDetails(
                    null,
                    null,
                    isVisible,
                    null
            );

            // 손자, 증손자 등 하위로 계속 전파
            if (!child.getChildren().isEmpty()) {
                updateChildrenVisibility(child, isVisible);
            }
        }
    }
    //[추가] 관리자용 카테고리 조회(숨김 처리한 것도 조회)
    public List<CategoryTreeResponse> getCategoryTreeForAdmin() {
        // 1. 삭제되지 않은 최상위 카테고리 조회
        List<Category> rootCategories = categoryRepository
                .findByParentIsNullAndDeletedAtIsNull();

        return rootCategories.stream()
                .map(root -> {
                    List<CategoryTreeResponse> activeChildren = null;
                    if (root.getChildren() != null) {
                        activeChildren = root.getChildren().stream()
                                .filter(child -> child.getDeletedAt() == null) // 삭제된 자식 제외
                                .map(categoryMapper::toTreeResponse)           // 자식은 Mapper 이용
                                .sorted(Comparator.comparing(CategoryTreeResponse::getDisplayOrder))
                                .collect(Collectors.toList());
                    }

                    return CategoryTreeResponse.builder()
                            .id(root.getId())
                            .name(root.getName())
                            .slug(root.getSlug())
                            .displayOrder(root.getDisplayOrder())
                            .isVisible(root.getIsVisible())
                            .depth(root.getDepth())
                            .children(activeChildren)
                            .build();
                })
                .sorted(Comparator.comparing(CategoryTreeResponse::getDisplayOrder))
                .collect(Collectors.toList());
    }

    //

    public List<CategoryTreeResponse> getSubCategories(Long currentCategoryId) {
        if (currentCategoryId == null) {
            return null;
        }

        List<CategoryTreeResponse> categoryTree = getCategoryTree();

        for (CategoryTreeResponse root : categoryTree) {
            // 1. 현재 선택된 게 대분류인 경우 -> 그 자식들을 반환
            if (root.getId().equals(currentCategoryId)) {
                return root.getChildren();
            }

            // 2. 자식들(소분류) 중에 현재 선택된 게 있는지 확인
            if (root.getChildren() != null) {
                for (CategoryTreeResponse child : root.getChildren()) {
                    if (child.getId().equals(currentCategoryId)) {
                        // 소분류를 선택했더라도, 같은 레벨의 메뉴를 보여주기 위해 root의 자식들을 반환
                        return root.getChildren();
                    }
                }
            }
        }
        return null;
    }


}
