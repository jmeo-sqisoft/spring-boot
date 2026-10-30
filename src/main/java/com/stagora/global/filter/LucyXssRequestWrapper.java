package com.stagora.global.filter;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.navercorp.lucy.security.xss.servletfilter.XssEscapeFilter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

/**
 * 단일 값·다중 값·전체 파라미터 조회에 같은 Lucy 규칙을 적용한다.
 * 매번 원본 값의 복사본을 변환해 반복 조회 시 중복 이스케이프와 원본 배열 변경을 방지한다.
 */
public class LucyXssRequestWrapper extends HttpServletRequestWrapper {
    private final XssEscapeFilter escapeFilter;
    private final String path;

    public LucyXssRequestWrapper(HttpServletRequest request, XssEscapeFilter escapeFilter) {
        super(request);
        this.escapeFilter = escapeFilter;
        this.path = request.getRequestURI().substring(request.getContextPath().length());
    }

    @Override
    public String getParameter(String name) {
        String[] values = getParameterValues(name);
        return values == null || values.length == 0 ? null : values[0];
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (values == null) {
            return null;
        }
        String[] filtered = new String[values.length];
        for (int index = 0; index < values.length; index++) {
            filtered[index] = escapeFilter.doFilter(path, name, values[index]);
        }
        return filtered;
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> filtered = new LinkedHashMap<>();
        super.getParameterMap().forEach((name, values) ->
            filtered.put(name, getParameterValues(name)));
        return Collections.unmodifiableMap(filtered);
    }
}
