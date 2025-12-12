package com.example.elicesecondproject.mall.domain.qna.controller;

import com.example.elicesecondproject.mall.domain.qna.service.QuestionService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/products/{productId}/questions")
public class QuestionController {
    private QuestionService questionService;

//    @GetMapping
//    public String getQuestionForm(@PathVariable Long productId,
//                                  @AuthenticationPrincipal MemberDetail principal,
//                                  Model model
//    ) {
//
//    }
}
