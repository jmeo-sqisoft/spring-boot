package com.stagora.global.filter;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.navercorp.lucy.security.xss.servletfilter.XssEscapeFilter;

import jakarta.servlet.FilterChain;

class LucyXssFilterTest {
    private final LucyXssFilter filter = new LucyXssFilter(XssEscapeFilter.getInstance());

    @ParameterizedTest
    @ValueSource(strings = {"GET", "POST"})
    void passesEscapedParametersAndOriginalResponseToNextFilter(String method) throws Exception {
        var request = new MockHttpServletRequest(method, "/apis/report/v1/sample/institutioninfo");
        if ("POST".equals(method)) {
            request.setContentType("application/x-www-form-urlencoded");
        }
        request.addParameter("clabId", "<script>alert(1)</script>", "일반 값");
        var response = new MockHttpServletResponse();
        FilterChain chain = (filteredRequest, filteredResponse) -> {
            assertThat(filteredRequest).isInstanceOf(LucyXssRequestWrapper.class);
            assertThat(filteredRequest.getParameter("clabId"))
                .isEqualTo("&lt;script&gt;alert(1)&lt;/script&gt;");
            assertThat(filteredRequest.getParameterValues("clabId"))
                .containsExactly("&lt;script&gt;alert(1)&lt;/script&gt;", "일반 값");
            assertThat(filteredRequest.getParameterMap().get("clabId"))
                .containsExactly("&lt;script&gt;alert(1)&lt;/script&gt;", "일반 값");
            assertThat(filteredResponse).isSameAs(response);
            filteredResponse.getWriter().write("processed");
        };

        filter.doFilter(request, response, chain);

        assertThat(response.getContentAsString()).isEqualTo("processed");
        assertThat(request.getParameter("clabId")).isEqualTo("<script>alert(1)</script>");
    }

    @Test
    void nestedDispatchUsesExistingWrapperWithoutDoubleEscaping() throws Exception {
        var request = new MockHttpServletRequest("GET", "/sample");
        request.addParameter("value", "<b>text</b>");
        var response = new MockHttpServletResponse();

        filter.doFilter(request, response, (firstRequest, firstResponse) ->
            filter.doFilter(firstRequest, firstResponse, (secondRequest, secondResponse) -> {
                assertThat(secondRequest).isSameAs(firstRequest);
                assertThat(secondRequest.getParameter("value")).isEqualTo("&lt;b&gt;text&lt;/b&gt;");
                secondResponse.getWriter().write("processed once");
            }));

        assertThat(response.getContentAsString()).isEqualTo("processed once");
    }

    @Test
    void propagatesDownstreamFailureAndClearsRequestMarker() {
        var request = new MockHttpServletRequest("GET", "/sample");
        var response = new MockHttpServletResponse();
        var failure = new IOException("Downstream failure");

        assertThatThrownBy(() -> filter.doFilter(request, response, (nextRequest, nextResponse) -> {
            throw failure;
        })).isSameAs(failure);

        assertThat(request.getAttributeNames().asIterator()).toIterable().isEmpty();
    }
}
