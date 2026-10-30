package com.stagora.global.filter;

import java.io.IOException;

import org.springframework.web.filter.OncePerRequestFilter;

import com.navercorp.lucy.security.xss.servletfilter.XssEscapeFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Lucy 규칙 엔진을 Jakarta Servlet 요청 파라미터에 적용한다.
 * JSON 본문, 요청 헤더와 업로드 파일 내용은 변경하지 않는다.
 */
public class LucyXssFilter extends OncePerRequestFilter {
    private final XssEscapeFilter escapeFilter;

    public LucyXssFilter(XssEscapeFilter escapeFilter) {
        this.escapeFilter = escapeFilter;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {
        filterChain.doFilter(new LucyXssRequestWrapper(request, escapeFilter), response);
    }
}
