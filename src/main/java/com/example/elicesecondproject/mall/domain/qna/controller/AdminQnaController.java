package com.example.elicesecondproject.mall.domain.qna.controller;

import com.example.elicesecondproject.mall.domain.qna.dto.response.QuestionResponse;
import com.example.elicesecondproject.mall.domain.qna.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/inquiries")
@RequiredArgsConstructor
public class AdminQnaController {

    private final QuestionService questionService;


    // 관리자 문의 목록 조회
    @GetMapping
    public String questionList(
            Model model,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Page<QuestionResponse> questions = questionService.getAllQuestions(pageable);

        model.addAttribute("questions", questions);
        model.addAttribute("menu", "inquiry");
        model.addAttribute("pageTitle", "문의 관리");
        return "admin/qna/qna-manage";
    }
}