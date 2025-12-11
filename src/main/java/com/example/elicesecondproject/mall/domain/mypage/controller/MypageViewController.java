package com.example.elicesecondproject.mall.domain.mypage.controller;

import com.example.elicesecondproject.mall.domain.member.dto.request.PasswordChangeRequest;
import com.example.elicesecondproject.mall.domain.member.dto.request.UpdateMemberRequest;
import com.example.elicesecondproject.mall.domain.member.dto.request.WithdrawMemberRequest;
import com.example.elicesecondproject.mall.domain.member.dto.response.MemberProfileResponse;
import com.example.elicesecondproject.mall.domain.member.entity.MemberDetail;
import com.example.elicesecondproject.mall.domain.member.service.MemberService;
import com.example.elicesecondproject.mall.domain.order.dto.request.UserOrderSearchCondition;
import com.example.elicesecondproject.mall.domain.order.dto.response.UserOrderDetailResponse;
import com.example.elicesecondproject.mall.domain.order.dto.response.UserOrderInfoResponse;
import com.example.elicesecondproject.mall.domain.order.service.OrderService;
import com.example.elicesecondproject.mall.domain.product.dto.ReviewProductInfoDto;
import com.example.elicesecondproject.mall.domain.product.dto.WishListProductResponse;
import com.example.elicesecondproject.mall.domain.product.service.ProductService;
import com.example.elicesecondproject.mall.domain.product.service.WishListService;
import com.example.elicesecondproject.mall.domain.review.dto.request.CreateReviewRequest;
import com.example.elicesecondproject.mall.domain.review.dto.request.UpdateReviewRequest;
import com.example.elicesecondproject.mall.domain.review.dto.response.MyReviewDetailResponse;
import com.example.elicesecondproject.mall.domain.review.dto.response.MyReviewResponse;
import com.example.elicesecondproject.mall.domain.review.service.ReviewService;
import com.example.elicesecondproject.mall.global.error.exception.FieldValidationException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/mypage")
public class MypageViewController {
    private final MemberService memberService;
    private final ReviewService reviewService;
    private final OrderService orderService;
    private final ProductService productService;
    private final WishListService wishListService;

    @GetMapping
    public String mypageIndex(@AuthenticationPrincipal MemberDetail principal,
                             Model model
    ) {
        Long memberId = principal.getMember().getId();
        MemberProfileResponse response = memberService.getMyProfile(memberId);

        model.addAttribute("member", response);

        List<UserOrderInfoResponse> recentOrders =
                orderService.getRecentOrdersForMyPage(memberId);
        model.addAttribute("recentOrders", recentOrders);
        return "mypage/mypage-index";
    }

    @GetMapping("/profile")
    public String getProfile(@AuthenticationPrincipal MemberDetail principal,
                             Model model
    ) {
        Long memberId = principal.getMember().getId();
        MemberProfileResponse response = memberService.getMyProfile(memberId);

        model.addAttribute("member", response);

        return "mypage/mypage-profile";
    }

    @GetMapping("/profile/nickname-phone")
    public String editProfileForm(@AuthenticationPrincipal MemberDetail principal,
                                  Model model) {

        Long memberId = principal.getMember().getId();
        MemberProfileResponse member = memberService.getMyProfile(memberId);

        // 폼 객체에 기본값 세팅 (UpdateMemberRequest DTO 사용)
        UpdateMemberRequest form = new UpdateMemberRequest();
        form.setNickname(member.getNickname());
        form.setPhone(member.getPhone()); // 필드명에 맞게 수정

        model.addAttribute("member", member); // 화면 상단 정보
        model.addAttribute("form", form);     // 수정 폼

        return "mypage/mypage-profile-nickname-phone-edit";
    }

    @PutMapping("/profile/nickname-phone")
    public String editProfile(@AuthenticationPrincipal MemberDetail principal,
                              @Valid @ModelAttribute("form") UpdateMemberRequest form,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {

        Long memberId = principal.getMember().getId();

        if (bindingResult.hasErrors()) {
            // 다시 정보 불러와서 폼 화면으로
            MemberProfileResponse member = memberService.getMyProfile(memberId);
            model.addAttribute("member", member);
            return "mypage/mypage-profile-nickname-phone-edit";
        }

        memberService.updateMyProfile(memberId, form);
        redirectAttributes.addFlashAttribute("message", "회원 정보가 수정되었습니다.");

        return "redirect:/mypage/profile/nickname-phone";
    }

    @GetMapping("/reviews")
    public String getMyReview(@AuthenticationPrincipal MemberDetail principal,
                              @RequestParam(required = false) LocalDate startDate,
                              @RequestParam(required = false) LocalDate endDate,
                              Pageable pageable,
                              Model model
    ) {
        Page<MyReviewResponse> reviews = reviewService.getReviewsByMember(principal.getMember().getId(), startDate, endDate, pageable);

        model.addAttribute("reviews", reviews);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "mypage/mypage-reviews";
    }

    @GetMapping("/reviews/{reviewId}")
    public String editMyReviewForm(@PathVariable Long reviewId,
                                   @AuthenticationPrincipal MemberDetail principal,
                                   Model model) {

        Long memberId = principal.getMember().getId();
        MyReviewDetailResponse review = reviewService.getMyReviewDetail(reviewId, memberId);

        model.addAttribute("review", review);

        return "mypage/mypage-review-edit";
    }

    @PutMapping("/reviews/{reviewId}")
    public String updateMyReview(@PathVariable Long reviewId,
                                 @AuthenticationPrincipal MemberDetail principal,
                                 @Valid @ModelAttribute UpdateReviewRequest request,
                                 @RequestParam(required = false) MultipartFile imageFile,
                                 @RequestParam(required = false, defaultValue = "false") boolean deleteImage
    ) {

        reviewService.updateMyReview(reviewId,
                principal.getMember().getId(),
                request,
                imageFile,
                deleteImage);

        return "redirect:/mypage/reviews";
    }

    @DeleteMapping("/reviews/{reviewId}")
    public String deleteMyReview(@PathVariable Long reviewId,
                                 @AuthenticationPrincipal MemberDetail principal

    ) {
        reviewService.softDeleteReview(reviewId, principal.getMember().getId());

        return "redirect:/mypage/reviews";
    }

    @GetMapping("/orders/{orderItemId}/review")
    public String createMyReviewForm(@PathVariable Long orderItemId,
                                     @AuthenticationPrincipal MemberDetail principal,
                                     Model model) {
        ReviewProductInfoDto response =
                reviewService.getReviewProductInfoForCreate(orderItemId, principal.getMember().getId());

        model.addAttribute("product", response);
        model.addAttribute("orderItemId", orderItemId);

        return "mypage/mypage-review-create";
    }

    @PostMapping("/orders/{orderItemId}/review")
    public String createMyReview(@PathVariable Long orderItemId,
                                 @AuthenticationPrincipal MemberDetail principal,
                                 @ModelAttribute @Valid CreateReviewRequest request,
                                 BindingResult bindingResult,
                                 @RequestParam(value = "image", required = false) MultipartFile image,
                                 Model model) {
        Long memberId = principal.getMember().getId();

        if (bindingResult.hasErrors()) {
            ReviewProductInfoDto product =
                    reviewService.getReviewProductInfoForCreate(orderItemId, memberId);

            model.addAttribute("product", product);
            model.addAttribute("orderItemId", orderItemId); // ★ 폼에서 다시 쓰니까 같이 넣어주기

            return "mypage/mypage-review-create";
        }

        // 이미지는 서비스에서 저장 + URL 생성
        reviewService.createReview(orderItemId, memberId, request, image);

        return "redirect:/mypage/reviews";
    }


    // 비밀번호 변경 폼
    @GetMapping("/profile/password")
    public String passwordChangeForm(Model model) {

        // 폼 객체 초기화
        PasswordChangeRequest form = new PasswordChangeRequest();
        model.addAttribute("form", form);

        return "mypage/mypage-profile-password-edit";
    }

    // 비밀번호 변경 처리
    @PutMapping("/profile/password")
    public String changePassword(@AuthenticationPrincipal MemberDetail principal,
                                 @Valid @ModelAttribute("form") PasswordChangeRequest form,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {

        // 1) 폼 검증 실패 → 다시 비밀번호 변경 화면으로
        if (bindingResult.hasErrors()) {
            return "mypage/mypage-profile-password-edit";
        }

        Long memberId = principal.getMember().getId();
        try {
            // 2) 서비스 비즈니스 검증 + 비밀번호 변경
            memberService.changePassword(memberId, form);

        } catch (FieldValidationException e) {
            // 서비스에서 던진 필드 유효성 예외를 BindingResult로 변환
            //bindingResult.rejectValue(e.getField(),"invalid." + e.getField(),e.getReason());

            // 페이지 상단 alert로 보여주기
            model.addAttribute("message", e.getReason());
            model.addAttribute("error", true);

            return "mypage/mypage-profile-password-edit";  // 비밀번호 페이지 다시 표시

        }
        // 3) 변경 성공
        redirectAttributes.addFlashAttribute("message", "비밀번호가 변경되었습니다.");
        return "redirect:/mypage/profile/password";
    }

    // 회원 탈퇴
    @GetMapping("/profile/withdraw")
    public String withdrawForm(Model model) {

        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new WithdrawMemberRequest());
        }
        return "mypage/mypage-profile-withdraw";
    }

    @DeleteMapping("/profile/withdraw")
    public String withdraw(@AuthenticationPrincipal MemberDetail principal,
                           @Valid @ModelAttribute("form") WithdrawMemberRequest form,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes) {

        Long memberId = principal.getMember().getId();

        try {
            memberService.withdraw(memberId, form);

        } catch (FieldValidationException e) {
            // 비밀번호 불일치 - 서비스에서 던진 필드 유효성 예외를 BindingResult로 변환
            bindingResult.rejectValue(e.getField(), "PASSWORD_MISMATCH", e.getReason());
            return "mypage/mypage-profile-withdraw";
        }

        redirectAttributes.addFlashAttribute("message", "회원 탈퇴가 완료되었습니다.");
        // 로그아웃으로 보내기
        return "redirect:/logout";
    }

    @GetMapping("/orders")
    public String getMyOrders(@ModelAttribute UserOrderSearchCondition condition,
                              @AuthenticationPrincipal MemberDetail principal,
                              Pageable pageable,
                              Model model
    ) {
        Page<UserOrderInfoResponse> responses =
                orderService.getMyOrders(condition, principal.getMember().getId(), pageable);

        model.addAttribute("orders", responses);
        model.addAttribute("condition", condition);
        return "mypage/mypage-orders";
    }

    @GetMapping("/orders/{orderId}")
    public String getMyOrderDetail(@PathVariable Long orderId,
                                   @AuthenticationPrincipal MemberDetail principal,
                                   Model model) {
        UserOrderDetailResponse response = orderService.getMyOrderDetail(orderId, principal.getMember().getId());

        model.addAttribute("orderDetail", response);
        return "mypage/mypage-order-detail";
    }

    @PutMapping("/orders/{orderId}")
    public String requestCancelOrder (@PathVariable Long orderId,
                                      @AuthenticationPrincipal MemberDetail principal
    ) {
        orderService.requestCancel(orderId, principal.getMember());
        return "redirect:/mypage/orders";
    }

    @GetMapping("/wish-list")
    public String getWishList(@AuthenticationPrincipal MemberDetail principal,
                              Pageable pageable,
                              Model model) {
        Page<WishListProductResponse> wishList = wishListService.getWishListByMember(principal.getMember().getId(), pageable);

        model.addAttribute("wishList", wishList);

        return "mypage/mypage-wish-list";
    }
}
