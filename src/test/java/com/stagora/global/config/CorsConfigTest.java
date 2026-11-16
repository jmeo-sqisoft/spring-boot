package com.stagora.global.config;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CorsConfigTest {
    private static final String PROBE_URL = "http://api.example/cors-probe";

    @ParameterizedTest
    @ValueSource(strings = {"http://localhost", "http://localhost:3000", "http://127.0.0.1",
        "http://127.0.0.1:5173", "http://[::1]", "http://[::1]:8080"})
    void defaultOriginsAllowCredentialsAndExposeAuthorization(String origin) throws Exception {
        mvc(new AppProperties()).perform(get(PROBE_URL).header(HttpHeaders.ORIGIN, origin))
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin))
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"))
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Authorization"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"https://untrusted.example", "https://localhost", "null"})
    void rejectsOriginsOutsideConfiguredPatterns(String origin) throws Exception {
        mvc(new AppProperties()).perform(get(PROBE_URL).header(HttpHeaders.ORIGIN, origin))
            .andExpect(status().isForbidden())
            .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @ParameterizedTest
    @ValueSource(strings = {"GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"})
    void preflightAllowsConfiguredMethodsAndRequestedHeaders(String method) throws Exception {
        mvc(new AppProperties()).perform(options(PROBE_URL)
                .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, method)
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "content-type,x-request-id"))
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET,POST,PUT,PATCH,DELETE,OPTIONS"))
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "content-type, x-request-id"));
    }

    @Test
    void rejectsUnconfiguredMethodAndEmptyOriginConfiguration() throws Exception {
        mvc(new AppProperties()).perform(options(PROBE_URL).header(HttpHeaders.ORIGIN, "http://localhost")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "TRACE"))
            .andExpect(status().isForbidden());
        var properties = new AppProperties();
        properties.getCors().setAllowedOriginPatterns(List.of());
        mvc(properties).perform(get(PROBE_URL).header(HttpHeaders.ORIGIN, "http://localhost"))
            .andExpect(status().isForbidden());
    }

    @Test
    void customBoundOriginsReplaceDefaults() throws Exception {
        var bound = new AtomicReference<AppProperties>();
        new ApplicationContextRunner().withUserConfiguration(PropertiesConfiguration.class)
            .withPropertyValues("app.cors.allowed-origin-patterns[0]=https://portal.example")
            .run(context -> {
                assertThat(context).hasNotFailed();
                bound.set(context.getBean(AppProperties.class));
            });
        assertThat(bound.get().getCors().getAllowedOriginPatterns()).containsExactly("https://portal.example");
        var mvc = mvc(bound.get());
        mvc.perform(get(PROBE_URL).header(HttpHeaders.ORIGIN, "https://portal.example"))
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "https://portal.example"));
        mvc.perform(get(PROBE_URL).header(HttpHeaders.ORIGIN, "http://localhost"))
            .andExpect(status().isForbidden());
    }

    private MockMvc mvc(AppProperties properties) {
        return MockMvcBuilders.standaloneSetup(new ProbeController())
            .addFilters(new CorsConfig().corsFilter(properties)).build();
    }

    @RestController
    private static class ProbeController {
        @GetMapping("/cors-probe")
        public ResponseEntity<Void> probe() {
            return ResponseEntity.ok().build();
        }
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(AppProperties.class)
    private static class PropertiesConfiguration {
    }
}
