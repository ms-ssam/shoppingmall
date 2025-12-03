package com.example.elicesecondproject.mall.domain.member.controller;

import com.example.elicesecondproject.mall.domain.member.dto.PasswordChangeRequest;
import com.example.elicesecondproject.mall.domain.member.dto.request.WithdrawMemberRequest;
import com.example.elicesecondproject.mall.domain.member.dto.response.MemberProfileResponse;
import com.example.elicesecondproject.mall.domain.member.dto.request.UpdateMemberRequest;
import com.example.elicesecondproject.mall.domain.member.entity.MemberDetail;
import com.example.elicesecondproject.mall.domain.member.service.MemberService;
import com.example.elicesecondproject.mall.global.jwt.JwtProvider;
import com.example.elicesecondproject.mall.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Tag(name = "회원 API", description = "로그인 및 회원가입 API")
@RequiredArgsConstructor
@RestController("/api/members")
public class MemberController {

    private final MemberService memberService;
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
/*

    @GetMapping("/hello")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("hello");
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signup(
            @Valid @RequestBody AddMemberRequest request
            ){
        memberService.save(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("회원가입 성공", null));
    }
*/

    // 내 정보 조회
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MemberProfileResponse>> getMyProfile(
            @AuthenticationPrincipal MemberDetail memberDetail
    ) {
        Long memberId = memberDetail.getMember().getId();
        MemberProfileResponse response = memberService.getMyProfile(memberId);

        return ResponseEntity.ok(
                ApiResponse.success("회원 정보 조회 성공", response)
        );
    }

    // 내 정보 수정(닉네임, 전화번호)
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<Void>> updateMyProfile(
            @AuthenticationPrincipal MemberDetail memberDetail,
            @Valid @RequestBody UpdateMemberRequest request,
            BindingResult result
    ){
        if(result.hasErrors()){
            System.err.println(result.getAllErrors());
        }
        Long memberId = memberDetail.getMember().getId();
        memberService.updateMyProfile(memberId, request);
        return ResponseEntity.ok(
                ApiResponse.success("회원 정보 수정 성공", null)
        );
    }

    // 비밀번호 수정
    @PutMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal MemberDetail memberDetail,
            @RequestBody @Valid PasswordChangeRequest request
    ) {
        Long memberId = memberDetail.getMember().getId();
        memberService.changePassword(memberId, request);

        return ResponseEntity.ok(
                ApiResponse.success("비밀번호 변경 성공", null)
        );
    }

    // 회원 탈퇴
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> withdraw(
            @AuthenticationPrincipal MemberDetail memberDetail,
            @RequestBody @Valid WithdrawMemberRequest request
    ) {
        Long memberId = memberDetail.getMember().getId();

        memberService.withdraw(memberId, request);

        return ResponseEntity.ok(
                ApiResponse.success("회원 탈퇴가 완료되었습니다.", null)
        );
    }
}
