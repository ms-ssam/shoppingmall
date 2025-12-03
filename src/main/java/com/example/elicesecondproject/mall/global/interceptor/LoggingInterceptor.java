package com.example.elicesecondproject.mall.global.interceptor;

import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
public class LoggingInterceptor implements HandlerInterceptor {
    private static final String START_TIME_ATTR = "LOG_START_TIME";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // duration 계산용 시작 시간 저장
        request.setAttribute(START_TIME_ATTR, System.currentTimeMillis());

        // Handler 정보 (클래스, 메서드)
        String handlerClass = "";
        String handlerMethod = "";
        if (handler instanceof HandlerMethod handlerMethodObj) {
            handlerClass = handlerMethodObj.getBeanType().getSimpleName();
            handlerMethod = handlerMethodObj.getMethod().getName();
        }

        // HTTP 메서드, URL
        String httpMethod = request.getMethod();
        String requestUri = request.getRequestURI();
        String queryString = request.getQueryString();
        String fullUrl = (queryString == null) ? requestUri : requestUri + "?" + queryString;

        // PathVariables
        @SuppressWarnings("unchecked")
        Map<String, String> pathVariables =
                (Map<String, String>) request.getAttribute(
                        HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

        // Headers
        Map<String, String> headers = new LinkedHashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames != null && headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            headers.put(name, request.getHeader(name));
        }

        // Body
        String requestBody = extractRequestBody(request);

        log.info("""
                        \n========== [REQUEST] ==========
                        Handler      : {}#{}
                        HTTP Method  : {}
                        URL          : {}
                        PathVars     : {}
                        Headers      : {}
                        Body         : {}
                        ==============================
                        """,
                handlerClass, handlerMethod,
                httpMethod,
                fullUrl,
                pathVariables,
                headers,
                requestBody
        );

        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        Long startTime = (Long) request.getAttribute(START_TIME_ATTR);
        long durationMs = (startTime == null)
                ? -1L
                : System.currentTimeMillis() - startTime;

        // Handler 정보 (클래스, 메서드)
        String handlerClass = "";
        String handlerMethod = "";
        if (handler instanceof HandlerMethod handlerMethodObj) {
            handlerClass = handlerMethodObj.getBeanType().getSimpleName();
            handlerMethod = handlerMethodObj.getMethod().getName();
        }

        int status = response.getStatus();
        String responseBody = extractResponseBody(response);

        log.info("""
                        \n========== [RESPONSE] ==========
                        Handler      : {}#{}
                        HTTP Status  : {}
                        Duration     : {} ms
                        Body         : {}
                        ===============================
                        """,
                handlerClass, handlerMethod,
                status,
                durationMs,
                responseBody
        );

        if (ex != null) {
            log.error("[RESPONSE] Exception occurred: {}", ex.getMessage(), ex);
        }
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }

    // ====== 내부 유틸 메서드 ======

    private String extractRequestBody(HttpServletRequest request) {
        if (request instanceof ContentCachingRequestWrapper wrapper) {
            byte[] buf = wrapper.getContentAsByteArray();
            if (buf.length == 0) {
                return "";
            }
            // 인코딩을 UTF-8로 강제
            return new String(buf, StandardCharsets.UTF_8);
        }
        return "[RequestBody logging requires ContentCachingRequestWrapper]";
    }

    private String extractResponseBody(HttpServletResponse response) {
        if (response instanceof ContentCachingResponseWrapper wrapper) {
            byte[] buf = wrapper.getContentAsByteArray();
            if (buf.length == 0) {
                return "";
            }
            // 인코딩을 UTF-8로 강제
            return new String(buf, StandardCharsets.UTF_8);
        }
        return "[ResponseBody logging requires ContentCachingResponseWrapper]";
    }
}

/**
 * 로깅 요소
 *
 * [요청]
 * 어떤 클래스에서?
 * 어떤 메서드에서?
 * 어떤 유형의 HTTP METHOD?
 * 어떤 URL?
 * path variable 뭐 들어왔는지
 * header 뭐 들어왔는지
 * body 뭐 들어왔는지
 *
 * [응답]
 * 어떤 클래스?
 * 어떤 메서드?
 * HTTP Status 응답 상태값
 * bodu (에러 응답이면 여기에 ErrorCode 같은 거 담아서)
 * duration (응답까지 얼마나 걸렸는지)
 */