package com.example.elicesecondproject.mall.domain.qna.controller;

import com.example.elicesecondproject.mall.domain.member.entity.MemberDetail;
import com.example.elicesecondproject.mall.domain.qna.service.QuestionService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
