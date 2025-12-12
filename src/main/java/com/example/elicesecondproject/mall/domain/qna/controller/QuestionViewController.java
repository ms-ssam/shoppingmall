package com.example.elicesecondproject.mall.domain.qna.controller;

import com.example.elicesecondproject.mall.domain.product.dto.QnaProductInfoResponse;
import com.example.elicesecondproject.mall.domain.qna.dto.request.QnaCreateRequest;
import com.example.elicesecondproject.mall.domain.qna.service.QuestionService;
import com.example.elicesecondproject.mall.global.security.entity.MemberDetail;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/products/{productId}")
@RequiredArgsConstructor
public class QuestionViewController {
    private final QuestionService questionService;

    @GetMapping("/question")
    public String getCreateQuestionForm(@PathVariable Long productId, Model model) {
        QnaProductInfoResponse response = questionService.getProductInfo(productId);

        model.addAttribute("product", response);

        return "question/question-create";
    }

    @PostMapping("/question")
    public String createQuestion(@PathVariable Long productId,
                                 @AuthenticationPrincipal MemberDetail principal,
                                 @ModelAttribute @Valid QnaCreateRequest request,
                                 BindingResult bindingResult,
                                 Model model) {
        if (bindingResult.hasErrors()) {
            QnaProductInfoResponse response = questionService.getProductInfo(productId);
            model.addAttribute("product", response);
            return "question/question-create";
        }

        questionService.createQuestion(productId, principal.getMember().getId(), request);

        return "redirect:/products/" + productId;
    }
}
