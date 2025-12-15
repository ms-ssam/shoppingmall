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
### [accessToken 재발급]
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

---
# JWT 방식이 View(View, SSR, Thymeleaf) 방식과 맞지 않았던 이유

- 본 프로젝트는 Thymeleaf 기반의 서버 사이드 렌더링(View 방식) 을 사용하며,<br/>
  로그인 이후에도 페이지 이동, redirect, form submit 이 빈번하게 발생하는 구조이다.
- 이러한 환경에서 JWT 인증을 적용한 결과,<br/>
  JWT의 설계 철학과 View 방식의 요청 흐름 사이에 구조적인 부조화가 발생하였다.

## 1. View 방식에서는 Authorization 헤더 유지가 자연스럽지 않다
JWT 인증은 기본적으로 다음 전제를 가진다.
- 매 요청마다 Authorization: Bearer {AccessToken} 헤더 포함
- 클라이언트가 토큰 전달을 명시적으로 관리

그러나 View 기반 웹 환경에서는 브라우저가 페이지 이동, redirect, form submit 과정에서 <br/>
Authorization 헤더를 자동으로 유지하거나 주입하지 않는다.

그 결과, 모든 요청에 토큰을 포함시키기 위해 <br/>
JavaScript(fetch, axios 등)를 강제적으로 개입시켜야 하며, <br/>
이는 View 기반의 단순한 요청 흐름을 깨뜨리는 원인이 된다.

## 2. View 방식에서는 로그인 상태를 프론트엔드가 알 필요가 없다.
View 기반 인증의 이상적인 구조는 서버가 로그인 상태를 관리하여<br/>
클라이언트는 인증 여부를 신경 쓰지 않고 사용 가능한 환경이다.

하지만 JWT를 사용할 경우 AccessToken 저장 위치(LocalStorage / JS 변수 등)를 결정해야 하고,<br/>
토큰 만료 여부를 클라이언트가 인지해야 하며 만료 시 재발급 요청을 프론트엔드가 제어해야 한다.

이는 View 환경에서 불필요한 토큰 관리 및 재발급 로직이 추가되어 프론트엔드 복잡도가 증가한다.

## 3. View 방식은 세션 기반 인증 모델과 더 자연스럽게 결합된다
View 기반 웹 애플리케이션의 로그인 흐름은
Spring Security의 세션 기반 인증 모델과 매우 잘 부합한다.<br/>
세션 기반 인증 모델은 로그인 성공 시 SecurityContext를 세션에 저장하고<br/>
이후 요청은 JSESSIONID 쿠키만으로 인증 상태를 유지할 수 있다.<br/>

이 구조에서는 인증 및 인가 책임이 전적으로 서버에 집중되며, 프론트엔드는 인증 상태를 별도로 관리할 필요가 없어<br/>
구조적으로 단순하고 안정적인 인증 흐름을 구성할 수 있다.

#### 결론
- JWT는 명시적인 API 요청에는 적합했지만, <br/>redirect와 form submit이 중심인 View 기반 인증 흐름과는 <br/>구조적으로 맞지 않아 세션 기반 인증으로 전환하였다.
