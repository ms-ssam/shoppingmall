package com.example.elicesecondproject.mall.domain.product.controller;

import com.example.elicesecondproject.mall.domain.product.dto.CreateProductRequest;
import com.example.elicesecondproject.mall.domain.product.service.ProductService;
import com.example.elicesecondproject.mall.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class ProductAdminController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ApiResponse<Long>> createProduct(@RequestBody @Valid CreateProductRequest createProductRequest){
        Long productId = productService.createProduct(createProductRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(productId));
    }
}
