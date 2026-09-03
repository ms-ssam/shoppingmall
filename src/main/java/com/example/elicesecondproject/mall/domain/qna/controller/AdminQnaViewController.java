package com.example.elicesecondproject.mall.domain.qna.controller;

import com.example.elicesecondproject.mall.domain.qna.dto.request.AnswerCreateRequest;
import com.example.elicesecondproject.mall.domain.qna.dto.request.QuestionSearchCondition;
import com.example.elicesecondproject.mall.domain.qna.dto.response.AdminQuestionDetailResponse;
import com.example.elicesecondproject.mall.domain.qna.dto.response.AdminQuestionListResponse;
import com.example.elicesecondproject.mall.domain.qna.service.AdminAnswerService;
import com.example.elicesecondproject.mall.domain.qna.service.AdminQuestionService;
import com.example.elicesecondproject.mall.global.security.entity.MemberDetail;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/inquiries")
@RequiredArgsConstructor
public class AdminQnaViewController {

    private final AdminQuestionService adminQuestionService;
    private final AdminAnswerService adminAnswerService;


    // 관리자 문의 목록 조회
    @GetMapping
    public String questionList(
            QuestionSearchCondition condition,
            Pageable pageable,
            Model model
            ) {
        Page<AdminQuestionListResponse> questions = adminQuestionService.getAllQuestions(condition, pageable);

        model.addAttribute("questions", questions);
        model.addAttribute("menu", "inquiry");
        model.addAttribute("pageTitle", "문의 관리");
        model.addAttribute("condition", condition);

        return "admin/qna/qna-manage";
    }

    @GetMapping("/{questionId}")
    public String getQuestionDetail(
            @PathVariable Long questionId,
            Model model
    ) {
        AdminQuestionDetailResponse question = adminQuestionService.getQuestionDetail(questionId);

        model.addAttribute("question", question);
        model.addAttribute("answerRequest", new AnswerCreateRequest());
        model.addAttribute("menu", "inquiry");
        model.addAttribute("pageTitle", "문의 관리");

        return "admin/qna/qna-detail";
    }

    @PostMapping("/{questionId}/answer")
    public String createAnswer(
            @PathVariable Long questionId,
            @AuthenticationPrincipal MemberDetail principal,
            @Valid @ModelAttribute("answerRequest") AnswerCreateRequest answerRequest,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            AdminQuestionDetailResponse question = adminQuestionService.getQuestionDetail(questionId);
            model.addAttribute("question", question);
            model.addAttribute("menu", "inquiry");
            model.addAttribute("pageTitle", "문의 관리");
            return "admin/qna/qna-detail";
        }

        adminAnswerService.createAnswer(questionId, principal.getMember(), answerRequest);
        return "redirect:/admin/inquiries/" + questionId;
    }

    @DeleteMapping("/{questionId}")
    public String deleteQuestion(@PathVariable Long questionId,
                                 @AuthenticationPrincipal MemberDetail principal) {

        adminQuestionService.deleteQuestionByAdmin(questionId, principal.getMember());

        return "redirect:/admin/inquiries";
    }

    @DeleteMapping("/{questionId}/answer")
    public String deleteAnswer(@PathVariable Long questionId,
                               @AuthenticationPrincipal MemberDetail principal) {

        adminQuestionService.deleteAnswerByAdmin(questionId, principal.getMember());

        return "redirect:/admin/inquiries/" + questionId;
    }
}