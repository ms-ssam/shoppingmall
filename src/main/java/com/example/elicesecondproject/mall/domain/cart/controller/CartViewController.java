package com.example.elicesecondproject.mall.domain.cart.controller;

import com.example.elicesecondproject.mall.domain.cart.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@RequiredArgsConstructor  // TODO: 주소 cart가 맞을지, carts가 맞을지 찾아보기 (cart 같은 경우엔 여러 개 조회하는 게 아니니까 cart가 맞나?)
@RequestMapping("/cart")  // TODO: 경로 매핑 팀원들한테 물어보기 (Security 걸리는 거 때문에 - API의 경우 /api로 시작해서 로그인 사용자만 볼 수 있도록...)
@Controller
public class CartViewController {
    private final CartService cartService;

//    public String getCartPage(Model model, @AuthenticationPrincipal MemberDetail memberDetail) {
//        Long memberId = memberDetail.getMember().getId();
//
//
//    }
}
