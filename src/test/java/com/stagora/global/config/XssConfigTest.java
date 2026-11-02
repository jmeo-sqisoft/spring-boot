package com.stagora.global.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.core.Ordered;
import static org.assertj.core.api.Assertions.assertThat;

import com.navercorp.lucy.security.xss.servletfilter.XssEscapeFilter;
import com.stagora.global.filter.LucyXssFilter;

class XssConfigTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withUserConfiguration(XssConfig.class);

    @Test
    void doesNotRegisterFilterWhenPropertyIsMissing() {
        contextRunner.run(context -> {
            assertThat(context).doesNotHaveBean("lucyXssFilter");
            assertThat(context).doesNotHaveBean(XssEscapeFilter.class);
        });
    }

    @Test
    void doesNotRegisterFilterWhenExplicitlyDisabled() {
        contextRunner.withPropertyValues("app.xss.enabled=false")
            .run(context -> {
                assertThat(context).doesNotHaveBean("lucyXssFilter");
                assertThat(context).doesNotHaveBean(XssEscapeFilter.class);
            });
    }

    @Test
    void registersFilterForAllPathsWhenEnabled() {
        contextRunner.withPropertyValues("app.xss.enabled=true").run(context -> {
            assertThat(context).hasSingleBean(FilterRegistrationBean.class);
            assertThat(context).hasSingleBean(XssEscapeFilter.class);
            FilterRegistrationBean<?> registration = context.getBean(
                "lucyXssFilter", FilterRegistrationBean.class);
            assertThat(registration.getFilter()).isInstanceOf(LucyXssFilter.class);
            assertThat(registration.getUrlPatterns()).containsExactly("/*");
            assertThat(registration.getOrder()).isEqualTo(Ordered.HIGHEST_PRECEDENCE + 10);
            assertThat(registration.isAsyncSupported()).isTrue();
        });
    }
}
