package com.example.elicesecondproject.mall.domain.qna.controller;

import com.example.elicesecondproject.mall.domain.product.dto.QnaProductInfoResponse;
import com.example.elicesecondproject.mall.domain.qna.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/products/{productId}")
@RequiredArgsConstructor
public class QuestionViewController {
    private final QuestionService questionService;

    @GetMapping("/question")
    public String getCreateQuestionForm(@PathVariable Long productId, Model model) {
        QnaProductInfoResponse response = QuestionService.getProductInfo(productId);
        model.addAttribute("product", response);

        return "question/question-create";
    }
}
