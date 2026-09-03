# JWT 인증 방식 로그인 로직

## 기능 개요
- JWT 인증 방식은 토큰 기반 인증을 통해 서버의 세션 상태에 의존하지 않고
API 요청을 인증하는 구조이다.
- AccessToken과 RefreshToken을 분리하여
보안성과 확장성을 함께 고려한 인증 흐름을 구성하였다.
  - AccessToken은 짧은 수명으로 API 인증에 사용
  - RefreshToken은 서버 저장소(DB)에 관리하여 재발급 및 로그아웃을 제어

### [로그인]
```mermaid
sequenceDiagram
    autonumber

    participant U as 사용자
    participant FE as 프론트엔드
    participant AC as AuthController
    participant AS as AuthService
    participant AM as AuthenticationManager
    participant JP as JwtProvider
    participant RT as RefreshTokenStore
    participant DB as DB
    participant F as JwtAuthenticationFilter
    participant SC as SecurityContext

%% 로그인 요청
    U ->> FE: 로그인 정보 입력<br/>(email, password)
    FE ->> AC: 로그인 요청 전달<br/>(LoginRequest)
    AC ->> AS: 로그인 비즈니스 로직 위임
    AS ->> AM: 사용자 인증 시도<br/>(Spring Security)

    alt 인증 실패
        AM -->> AS: 인증 예외 발생
        AS -->> AC: 로그인 실패 처리
        AC -->> FE: 401 Unauthorized 응답
        FE -->> U: 로그인 실패 메시지 표시
    else 인증 성공
        AM -->> AS: 인증된 사용자 정보 반환<br/>(MemberDetail)

        AS ->> RT: 기존 RefreshToken 폐기 요청
        RT ->> DB: 사용자 RefreshToken 삭제
        DB -->> RT: 삭제 완료

        AS ->> JP: AccessToken / RefreshToken 생성 요청
        JP -->> AS: AccessToken / RefreshToken 반환

        AS ->> RT: RefreshToken 저장 요청<br/>(해시값 + 사용자 정보)
        RT ->> DB: RefreshToken 영속화
        DB -->> RT: 저장 완료

        AS ->> AS: 현재 시각으로 Member.lastLoginAt 갱신

        AS -->> AC: 토큰 묶음 반환<br/>(AccessToken, RefreshToken)
        AC -->> FE: AccessToken → Authorization(Bearer) 헤더<br/>RefreshToken → HttpOnly 쿠키
        FE -->> U: 로그인 완료 및 인증 상태 유지
    end

%% 2) 인증이 필요한 요청 (AccessToken 검증)
    U ->> FE: 보호된 기능 사용 요청
    FE ->> F: API 요청 전송<br/>Authorization: Bearer {accessToken}

    F ->> F: Authorization 헤더 파싱<br/>(Bearer 토큰 추출)
    alt 토큰 무효<br/>(헤더 없음 / 만료 / 위조)
        F -->> FE: 401 Unauthorized 응답
        FE -->> U: 재로그인 또는 토큰 재발급 안내
    else 토큰 유효
        F ->> JP: 토큰 검증 및 인증 정보 추출 요청
        JP -->> F: Authentication 반환<br/>(MemberDetail, 권한 포함)

        F ->> SC: SecurityContext에 인증 저장<br/>(setAuthentication)
        SC -->> F: 저장 완료

        F -->> FE: 다음 필터/컨트롤러로 요청 전달<br/>(인증된 사용자로 처리)
        FE -->> U: 정상 응답 표시
    end
```
### [AccessToken 재발급]
```mermaid
sequenceDiagram
    autonumber

    participant U as 사용자
    participant FE as 프론트엔드
    participant AC as AuthController
    participant AS as AuthService
    participant JP as JwtProvider
    participant RT as RefreshTokenStore
    participant DB as DB

%% 3) 토큰 재발급 (Reissue)
    U ->> FE: AccessToken 만료 감지<br/>(API 401 등)
    FE ->> AC: 재발급 요청 전송<br/>(HttpOnly 쿠키의 RefreshToken 포함)

    AC ->> AC: 쿠키에서 RefreshToken 추출<br/>(refresh_token)
    alt RefreshToken 누락
        AC -->> FE: 401 Unauthorized<br/>(REFRESH_TOKEN_MISSING)
        FE -->> U: 재로그인 안내
    else RefreshToken 존재
        AC ->> AS: 재발급 로직 위임<br/>(RefreshToken, userAgent)

        AS ->> JP: RefreshToken 1차 검증<br/>(서명/만료/형식)
        alt RefreshToken 위조/만료
            JP -->> AS: invalid
            AS -->> AC: 재발급 불가 처리
            AC -->> FE: 401 Unauthorized<br/>(REFRESH_TOKEN_INVALID)
            FE -->> U: 재로그인 안내
        else RefreshToken 1차 검증 통과
            JP -->> AS: valid

            AS ->> AS: refreshToken 해시 생성<br/>(sha256)
            AS ->> RT: 저장소에서 토큰 조회 요청<br/>(hash, userAgent)
            RT ->> DB: RefreshToken 조회<br/>(hash 기준)
            DB -->> RT: tokenEntity 반환(없을 수도 있음)
            RT -->> AS: 조회 결과 반환

            alt 저장소에 토큰 없음/폐기됨
                AS -->> AC: 재발급 불가 처리<br/>(REVOKED_OR_NOT_FOUND)
                AC -->> FE: 401 Unauthorized
                FE -->> U: 재로그인 안내
            else 저장소 토큰 유효
                AS ->> JP: 새 AccessToken 생성 요청<br/>(memberId/roles)
                JP -->> AS: newAccessToken 반환

                AS -->> AC: 재발급 성공 응답 DTO 반환
                AC -->> FE: 200 OK<br/>(newAccessToken)
                FE -->> U: 인증 상태 유지<br/>(AccessToken 갱신)
            end
        end
    end
```
### [로그아웃]
```mermaid
sequenceDiagram
    autonumber

    participant U as 사용자
    participant FE as 프론트엔드
    participant AC as AuthController
    participant AS as AuthService
    participant RT as RefreshTokenStore
    participant DB as DB

%% 4) 로그아웃 (RefreshToken 폐기)
    U ->> FE: 로그아웃 요청
    FE ->> AC: 로그아웃 API 호출
    AC ->> AS: 로그아웃 처리 위임
    AS ->> RT: RefreshToken 폐기 요청
    RT ->> DB: RefreshToken 삭제
    DB -->> RT: 삭제 완료
    AS -->> AC: 로그아웃 완료
    AC -->> FE: 200 OK
    FE -->> U: 로그인 화면 이동
```
---

# 세션 기반 로그인 로직

## 기능개요
- 세션 기반 로그인은 Spring Security의 기본 인증 모델로, 서버가 로그인 상태를 세션에 저장하고 관리하는 방식이다.
- View(SSR, Thymeleaf) 기반 웹 애플리케이션의 페이지 이동, redirect, form submit 흐름과 자연스럽게 결합된다.
  - 로그인 상태는 서버 세션(HttpSession)에 저장
  - 클라이언트는 인증 정보를 직접 관리하지 않음
  
```mermaid
sequenceDiagram
    autonumber

    participant U as 사용자
    participant FE as 프론트엔드
    participant SS as Spring Security FilterChain
    participant UPF as UsernamePasswordAuthenticationFilter
    participant AM as AuthenticationManager
    participant SC as SecurityContextHolder
    participant HS as HttpSession

%% 1) 세션 기반 로그인 (POST /login)
    U ->> FE: 이메일/비밀번호 입력 후 제출
    FE ->> SS: POST /login<br/>(email, password)

    SS ->> UPF: loginProcessingUrl 매칭<br/>(POST /login)
    UPF ->> AM: authenticate(email, password)

    alt 인증 실패
        AM -->> UPF: AuthenticationException
        UPF -->> FE: 실패 처리<br/>302 /login?error=true
        FE -->> U: 로그인 실패 화면 표시
    else 인증 성공
        AM -->> UPF: Authentication 반환
        UPF ->> SC: SecurityContext에 Authentication 저장
        UPF ->> HS: 세션 생성(IF_REQUIRED)<br/>SecurityContext 세션에 저장
        HS -->> FE: Set-Cookie: JSESSIONID
        UPF -->> FE: 성공 처리<br/>302 / (defaultSuccessUrl)
        FE -->> U: 로그인 완료(세션 유지)
    end

%% 2) 인증이 필요한 요청 (세션 검증)
    U ->> FE: 보호된 페이지/API 요청
    FE ->> SS: 요청 전송<br/>(Cookie: JSESSIONID 포함)

    SS ->> HS: 세션에서 SecurityContext 로드
    alt 세션 없음/만료
        HS -->> SS: SecurityContext 없음
        SS -->> FE: 인증 진입점 처리<br/>/login 리다이렉트
        FE -->> U: 로그인 필요 안내
    else 세션 유효
        HS -->> SS: SecurityContext 반환
        SS -->> FE: 정상 응답(인증된 사용자)
        FE -->> U: 요청 결과 표시
    end

%% 3) 로그아웃 (세션 무효화)
    U ->> FE: 로그아웃 클릭
    FE ->> SS: POST /logout
    SS ->> HS: 세션 무효화(invalidate)
    SS -->> FE: 쿠키 삭제(deleteCookies)<br/>JSESSIONID 만료
    FE -->> U: / 로 이동(logoutSuccessUrl)
```
-----
# JWT 방식 도입 후 세션 방식으로 전환한 이유
본 프로젝트는 Thymeleaf 기반의 서버 사이드 렌더링(View, SSR) 구조로 진행되었으며, <br/>
초기 설계 단계에서는 확장성과 학습 목적을 고려하여 JWT 기반 인증 방식을 도입하였다.

JWT를 적용하여 AccessToken / RefreshToken 발급, 인증 필터 구성 등 기본적인 인증 구조를 구현하였으나, <br/>
개발을 진행하는 과정에서 프로젝트의 실제 요청 흐름과 개발 일정 측면에서 부담이 커진다는 점을 인지하게 되었다.

이에 따라 개발 중반부에 인증 방식에 대한 재검토를 진행하였고, 최종적으로 세션 기반 인증 방식으로 전환하는 의사결정을 내렸다.

## 1. JWT 적용 과정에서 예상보다 많은 추가 작업이 발생
JWT 방식으로 View 기반 기능을 구현하는 과정에서 다음과 같은 추가적인 고려 사항이 필요했다.
- redirect 및 form submit 요청 시 토큰 전달 문제
- AccessToken 만료 시 재발급 흐름 설계
이러한 요소들은 기술적으로 해결 가능했으나, 인증 로직에 예상보다 많은 구현 및 디버깅 시간이 소요되었고, 전체 기능 개발 일정에 영향을 줄 가능성이 있다고 판단하였다.

## 2. 개발 완료 기한을 고려한 인증 방식 전환 결정
본 프로젝트에서 중요하게 고려한 핵심 기능은 다음과 같다.
- 주문/결제 기능
- 마이페이지 기능
- 상품 관리 기능
- 리뷰/ 문의 / 위시리스트
이러한 핵심 기능들을 정해진 기간 내에 안정적으로 완성하는 것이 프로젝트의 가장 중요한 목표였다.

이 기준으로 재검토한 결과, Spring Security의 기본 흐름을 그대로 활용하여 로그인 개발 시간을 단축하는
세션 기반 인증 방식이 더 적합하다고 판단하였다.

## 3. JWT → 세션 전환을 통해 얻은 효과
세션 기반 인증 방식으로 전환한 이후, 인증과 관련된 불필요한 설계 고민과 추가 구현을 줄일 수 있었다.
- Access Token을 쿠키에 담을지, Authorization 헤더에 담을지, 혹은 View 단에서 JavaScript로 주입할지에 대한 고민과 구현이 불필요해졌다.
- View(Thymeleaf) 환경에서 Access Token 만료를 어떻게 감지할 것인지, 만료 시 재발급 요청을 프론트엔드에서 처리해야 하는지,<br/>
  혹은 백엔드 필터에서 감지하여 처리해야 하는지와 같은 토큰 재발급 흐름에 대한 설계 고민이 사라졌다.
그 결과 인증 로직에 소요되는 개발 시간을 줄이고, 비즈니스 로직과 핵심 기능 구현에 더 집중할 수 있었다.

### 결론
본 프로젝트는 Thymeleaf 기반의 View 중심 구조로 진행되었으며, JWT 방식은 redirect 및 토큰 관리 측면에서 추가적인 구현 부담이 발생하였다.
이에 따라 제한된 개발 기간 내에 핵심 기능을 안정적으로 완성하기 위해 Spring Security의 기본 흐름을 활용할 수 있는 세션 기반 인증 방식이<br/>
프로젝트에 더 적합하다고 판단하여 전환하였다.