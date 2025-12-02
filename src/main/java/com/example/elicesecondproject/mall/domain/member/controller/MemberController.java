package com.example.elicesecondproject.mall.domain.member.controller;

import com.example.elicesecondproject.mall.domain.member.dto.request.AddMemberRequest;
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
import org.springframework.web.bind.annotation.*;

@Tag(name = "회원 API", description = "로그인 및 회원가입 API")
@RequiredArgsConstructor
@RestController
public class MemberController {

    private final MemberService memberService;
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;

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

    // 내 정보 조회(PROFVIEW-F-01)
    @GetMapping("api/me")
    public ResponseEntity<ApiResponse<MemberProfileResponse>> getMyProfile(
            @AuthenticationPrincipal MemberDetail memberDetail
    ) {
        Long memberId = memberDetail.getMember().getId();
        MemberProfileResponse response = memberService.getMyProfile(memberId);

        return ResponseEntity.ok(
                ApiResponse.success("회원 정보 조회 성공", response)
        );
    }

    // 내 정보 수정(닉네밍, 전화번호)( PROFVIEW-F-02,3)
    @PutMapping("api/me")
    public ResponseEntity<ApiResponse<Void>> updateMyProfile(
            @AuthenticationPrincipal MemberDetail memberDetail,
            @Valid @RequestBody UpdateMemberRequest request
    ){
        Long memberId = memberDetail.getMember().getId();
        memberService.updateMyProfile(memberId, request);
        return ResponseEntity.ok(
                ApiResponse.success("회원 정보 수정 성공", null)
        );
    }

}
