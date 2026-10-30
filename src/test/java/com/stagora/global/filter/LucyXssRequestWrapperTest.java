package com.stagora.global.filter;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.navercorp.lucy.security.xss.servletfilter.XssEscapeFilter;

class LucyXssRequestWrapperTest {
    private static final String SCRIPT = "<script>alert(1)</script>";
    private static final String ESCAPED_SCRIPT = "&lt;script&gt;alert(1)&lt;/script&gt;";

    @ParameterizedTest
    @ValueSource(strings = {"<script>alert(1)</script>", "<img src=x onerror=alert(1)>",
        "<svg onload=alert(1)>", "\" onmouseover=\"alert(1)", "' onclick='alert(1)"})
    void escapesHtmlMarkupAndQuotes(String payload) {
        var request = new MockHttpServletRequest();
        request.addParameter("value", payload);

        String value = wrap(request).getParameter("value");

        assertThat(value).isNotEqualTo(payload).doesNotContain("<", ">", "\"", "'");
    }

    @Test
    void filtersEveryValueAndKeepsRepeatedReadsConsistent() {
        var request = new MockHttpServletRequest();
        request.addParameter("value", SCRIPT, "normal-value");
        var wrapped = wrap(request);

        assertThat(wrapped.getParameter("value")).isEqualTo(ESCAPED_SCRIPT);
        assertThat(wrapped.getParameterValues("value"))
            .containsExactly(ESCAPED_SCRIPT, "normal-value");
        assertThat(wrapped.getParameterMap().get("value"))
            .containsExactly(ESCAPED_SCRIPT, "normal-value");
        assertThat(wrapped.getParameter("value")).isEqualTo(ESCAPED_SCRIPT);
        assertThat(request.getParameterValues("value")).containsExactly(SCRIPT, "normal-value");
    }

    @Test
    void returnedArraysAndMapsCannotChangeFutureParameterReads() {
        var request = new MockHttpServletRequest();
        request.addParameter("value", SCRIPT);
        var wrapped = wrap(request);

        wrapped.getParameterValues("value")[0] = "changed";
        wrapped.getParameterMap().get("value")[0] = "also changed";

        assertThat(wrapped.getParameter("value")).isEqualTo(ESCAPED_SCRIPT);
        assertThat(request.getParameter("value")).isEqualTo(SCRIPT);
        assertThatThrownBy(() -> wrapped.getParameterMap().clear())
            .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void preservesMissingBlankAndOrdinaryValues() {
        var request = new MockHttpServletRequest();
        request.addParameter("blank", " ");
        request.addParameter("empty", "");
        request.addParameter("hospitalId", "019bf94a-1e58-773c-9d46-ae8318e6bcba");
        request.addParameter("name", "한국어 기관명");
        var wrapped = wrap(request);

        assertThat(wrapped.getParameter("missing")).isNull();
        assertThat(wrapped.getParameterValues("missing")).isNull();
        assertThat(wrapped.getParameter("blank")).isEqualTo(" ");
        assertThat(wrapped.getParameter("empty")).isEmpty();
        assertThat(wrapped.getParameter("hospitalId"))
            .isEqualTo("019bf94a-1e58-773c-9d46-ae8318e6bcba");
        assertThat(wrapped.getParameter("name")).isEqualTo("한국어 기관명");
    }

    @Test
    void usesContextRelativePathForLucyRules() {
        var request = new MockHttpServletRequest();
        request.setContextPath("/application");
        request.setRequestURI("/application/echo");
        request.addParameter("value", SCRIPT);
        var engine = mock(XssEscapeFilter.class);
        when(engine.doFilter("/echo", "value", SCRIPT)).thenReturn(ESCAPED_SCRIPT);

        assertThat(new LucyXssRequestWrapper(request, engine).getParameter("value"))
            .isEqualTo(ESCAPED_SCRIPT);
        verify(engine).doFilter("/echo", "value", SCRIPT);
    }

    @Test
    void leavesJsonBodyAndHeadersUnchanged() throws Exception {
        var request = new MockHttpServletRequest();
        String body = "{\"value\":\"" + SCRIPT + "\"}";
        request.setContentType("application/json");
        request.setContent(body.getBytes(StandardCharsets.UTF_8));
        request.addHeader("X-Value", SCRIPT);

        var wrapped = wrap(request);

        assertThat(new String(wrapped.getInputStream().readAllBytes(), StandardCharsets.UTF_8))
            .isEqualTo(body);
        assertThat(wrapped.getHeader("X-Value")).isEqualTo(SCRIPT);
    }

    private LucyXssRequestWrapper wrap(MockHttpServletRequest request) {
        return new LucyXssRequestWrapper(request, XssEscapeFilter.getInstance());
    }
}
