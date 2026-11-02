package com.stagora.global.filter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.assertj.core.api.Assertions.assertThat;

import com.nhncorp.lucy.security.xss.XssSaxFilter;

class LucyXssSaxRulesTest {
    private final XssSaxFilter filter = XssSaxFilter.getInstance("lucy-xss-sax.xml", true);

    @ParameterizedTest
    @ValueSource(strings = {"<p>문단</p>", "<br>", "<b>굵게</b>",
        "<p>안녕하세요<br><b>강조</b></p>"})
    void preservesAllowedTags(String html) {
        assertThat(filter.doFilter(html)).isEqualTo(html);
    }

    @ParameterizedTest
    @ValueSource(strings = {"p", "br", "b"})
    void removesAllAttributesFromAllowedTags(String tag) {
        String html = "<" + tag
            + " onclick=\"alert(1)\" style=\"color:red\" class=\"example\" data-value=\"test\">";

        assertThat(filter.doFilter(html)).isEqualTo("<" + tag + ">");
    }

    @ParameterizedTest
    @ValueSource(strings = {"script", "img", "iframe", "svg", "a", "div", "strong"})
    void doesNotAllowOtherTags(String tag) {
        String html = "<" + tag + ">test</" + tag + ">";

        assertThat(filter.doFilter(html)).doesNotContain("<" + tag, "</" + tag);
    }

    @Test
    void preservesOrdinaryKoreanText() {
        assertThat(filter.doFilter("기관명과 일반 텍스트")).isEqualTo("기관명과 일반 텍스트");
    }
}
