# Shopping Mall Project

Spring Boot 기반의 이커머스(쇼핑몰) 웹 애플리케이션입니다. 회원가입/로그인부터 상품 탐색, 장바구니, 주문/결제(Toss Payments 연동), 리뷰·문의, 마이페이지, 관리자 페이지까지 쇼핑몰의 핵심 도메인을 서버 사이드 렌더링(Thymeleaf) 기반으로 구현했습니다.

- **기간**: 2025.11.25 ~ 2025.12.22
- **팀 구성**: 백엔드 개발자 4명
- **협업 도구**: GitLab, Git Flow (도메인 중심 브랜치 전략)
- **협업 성과**: 총 578개 커밋, 도메인별 브랜치 병렬 개발

## 목차

- [기술 스택](#기술-스택)
- [주요 기능](#주요-기능)
- [아키텍처](#아키텍처)
- [테스트](#테스트)
- [프로젝트 문서](#프로젝트-문서)
- [협업 전략](#협업-전략)
- [실행 방법](#실행-방법)

## 기술 스택

| 구분 | 스택 |
| --- | --- |
| Language / Framework | Java 21, Spring Boot 3.3.5 |
| Web | Spring MVC, Thymeleaf (SSR) |
| Data | Spring Data JPA, QueryDSL 5.0, MySQL 8.0, Redis |
| Auth | Spring Security (세션 기반), JWT (초기 도입 후 세션 방식으로 전환) |
| 외부 연동 | Toss Payments (결제 위젯 / 결제 승인 API) |
| 기타 | MapStruct, Thumbnailator(이미지 리사이징), springdoc-openapi(Swagger) |
| 인프라 / CI-CD | Docker Compose(MySQL, Redis), GitLab CI (build → test → deploy) |
| 테스트 | JUnit5, Spring Security Test, H2 |

## 주요 기능

- **인증/회원**: 회원가입, 로그인/로그아웃, 세션 기반 인증, 권한 분리(회원/관리자)
- **상품**: 상품/옵션(색상·사이즈) 등록·수정·삭제(소프트 삭제), 상품 이미지 업로드(원본+썸네일), 카테고리 관리
- **장바구니**: 다중 옵션 일괄 담기, 동일 옵션 수량 병합, 프론트/백엔드 이중 재고 검증
- **주문/결제**: 장바구니 기반 주문서 생성 → 배송정보 입력 → 주문 생성 → Toss Payments 결제, 결제 성공/실패 처리, 관리자 주문 관리
- **마이페이지**: 주문 내역 조회, 회원 정보 관리, 위시리스트
- **리뷰/문의(Q&A)**: 상품 리뷰 작성, 1:1 문의 등록 및 관리자 답변
- **관리자**: 상품/카테고리/주문/리뷰/문의 관리 페이지

## 아키텍처

도메인 중심 패키지 구조로 구성했습니다.

```
mall
├── domain
│   ├── auth, member, mypage, address        # 인증/회원
│   ├── product, option, category            # 상품
│   ├── cart, order, payment                 # 주문/결제
│   └── review, qna, notice                  # 리뷰/문의/공지
└── global
    ├── config, security, filter, interceptor
    ├── error, response, web
    └── service, util, entity, dto
```

각 도메인은 Controller → Service → Repository 계층으로 구성되며, 도메인 간 결합을 낮추기 위해 스냅샷 엔티티(OrderItem), 이벤트 기반 후처리(결제 성공 후 장바구니 비우기) 등을 적용했습니다. 자세한 흐름은 [프로젝트 문서](#프로젝트-문서)의 시퀀스 다이어그램을 참고해 주세요.

## 테스트

`Service` 단위 테스트와 실제 흐름을 검증하는 통합 테스트를 함께 작성했습니다.

- `AuthServiceTest`, `CartServiceTest`, `OrderServiceTest`, `TossPaymentServiceTest` (단위 테스트)
- `OrderFlowIntegrationTest`, `ProductServiceIntegrationTest`, `QnaIntegrationTest`, `TossPaymentControllerMvcTest` (통합 테스트, H2 기반)

```bash
./gradlew test
```

## 프로젝트 문서

기능별 상세 설계, 시퀀스 다이어그램, 트러블슈팅, 향후 개선 방향은 `docs/` 디렉터리에 정리되어 있습니다.

| 문서 | 내용 |
| --- | --- |
| [login-logic.md](docs/login-logic.md) | 로그인/인증 (JWT → 세션 전환 배경 포함) |
| [product-cart-logic.md](docs/product-cart-logic.md) | 상품 상세 → 장바구니 담기 |
| [cart-order-sheet-payment-logic.md](docs/cart-order-sheet-payment-logic.md) | 장바구니 → 주문서 → 주문 생성 |
| [payment-logic.md](docs/payment-logic.md) | Toss Payments 결제 성공/실패 처리 |
| [uploadImageLogic.md](docs/uploadImageLogic.md) | 상품 이미지 업로드 및 관리 |
| [GitFlow.md](docs/GitFlow.md) | Git 협업 전략, 브랜치/커밋 컨벤션, 충돌 관리 |

## 협업 전략

- **도메인 중심 브랜치 전략**: `develop` → 도메인 브랜치(`feature/product`) → 세부 기능 브랜치(`feature/PROD-F-01-...`) 순으로 분기·병합
- **커밋 컨벤션**: `[이슈코드] 타입: 내용` 형식으로 커밋과 기능명세서를 연결 (예: `[PROD-F-01] feat: 상품 옵션 조회 API 구현`)
- **로컬 선행 머지 전략**: MR 이전 로컬에서 대상 브랜치를 선병합해 충돌을 미리 해결 → 주 5~6회였던 Merge 충돌을 주 1회 이하로 감소
- **데일리 스크럼**: 매일 아침 30~40분간 진행 상황 공유, 예외 처리/기능명세 일치 여부 중심 코드 리뷰

자세한 내용은 [docs/GitFlow.md](docs/GitFlow.md)를 참고해 주세요.

## 실행 방법

### 1. 인프라 실행 (MySQL, Redis)

```bash
docker-compose up -d
```

### 2. 애플리케이션 실행

```bash
./gradlew bootRun
```

기본 프로필은 `local`이며, `src/main/resources/application-local.yml`에 로컬 DB 접속 정보 등을 설정해야 합니다. 서버는 기본적으로 `8080` 포트에서 실행됩니다.

### 3. API 문서 확인

```
http://localhost:8080/swagger-ui/index.html
```
