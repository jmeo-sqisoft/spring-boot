package com.stagora;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import static org.assertj.core.api.Assertions.assertThat;

import com.p6spy.engine.spy.P6DataSource;
import com.stagora.global.filter.LucyXssFilter;
import com.stagora.report.sample.controller.SampleController;
import com.stagora.report.sample.dto.AreasConverter;
import com.stagora.report.sample.dto.InstitutionInfoConverter;
import com.stagora.report.sample.dto.NationInfoConverter;
import com.stagora.report.sample.mapper.SampleMapper;
import com.stagora.report.sample.service.SampleService;
import com.zaxxer.hikari.HikariDataSource;

import jakarta.persistence.EntityManagerFactory;

class ApplicationConfigurationTest {
    @ParameterizedTest
    @CsvSource({"dev,true", "dev,false", "uat,true", "uat,false", "prd,true", "prd,false"})
    void profilesStartWithoutExternalServices(String profile, boolean xssEnabled) throws Exception {
        SpringApplication application = new SpringApplication(Application.class);
        application.setWebApplicationType(WebApplicationType.SERVLET);
        try (ConfigurableApplicationContext context = application.run(
                "--spring.profiles.active=" + profile,
                "--server.port=0",
                "--app.xss.enabled=" + xssEnabled,
                "--spring.datasource.local.url=jdbc:postgresql://localhost:1/config_test",
                "--spring.jpa.properties.hibernate.boot.allow_jdbc_metadata_access=false",
                "--spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect")) {
            DataSource dataSource = context.getBean(DataSource.class);
            assertThat(context.containsBean("lucyXssFilter")).isEqualTo(xssEnabled);
            if (xssEnabled) {
                FilterRegistrationBean<?> xssRegistration =
                    context.getBean("lucyXssFilter", FilterRegistrationBean.class);
                assertThat(xssRegistration.getFilter()).isInstanceOf(LucyXssFilter.class);
                assertThat(xssRegistration.getUrlPatterns()).containsExactly("/*");
                assertThat(xssRegistration.getOrder()).isEqualTo(Ordered.HIGHEST_PRECEDENCE + 10);
            }
            assertThat(context.getBean(SampleController.class)).isNotNull();
            assertThat(context.getBean(SampleService.class)).isNotNull();
            assertThat(context.getBean(SampleMapper.class)).isNotNull();
            assertThat(context.getBean(AreasConverter.class)).isNotNull();
            assertThat(context.getBean(InstitutionInfoConverter.class)).isNotNull();
            assertThat(context.getBean(NationInfoConverter.class)).isNotNull();
            HikariDataSource hikari = dataSource.unwrap(HikariDataSource.class);
            assertThat(hikari.getPoolName()).isEqualTo("gmcs-be-hikari-pool");
            assertThat(hikari.getDataSourceProperties()).containsEntry("ApplicationName", "gmcs");
            assertThat(dataSource instanceof P6DataSource).isEqualTo(!"prd".equals(profile));
            assertThat(context.getBean(SqlSessionFactory.class)
                .getConfiguration().getEnvironment().getDataSource())
                .isSameAs(dataSource);
            assertThat(context.getBean(EntityManagerFactory.class).getMetamodel().getEntities())
                .isEmpty();
            assertThat(context.getEnvironment().getProperty("mybatis.type-aliases-package"))
                .isEqualTo("com.stagora");
            assertThat(context.getEnvironment()
                .getProperty("springdoc.swagger-ui.enabled", Boolean.class))
                .isEqualTo("dev".equals(profile));
            assertThat(context.getMessage("success.default", null, Locale.ENGLISH))
                .isEqualTo("Processed successfully.");
            assertThat(context.getMessage("success.default", null, Locale.KOREAN))
                .isNotEqualTo("Processed successfully.");
            assertThat(context.getEnvironment()
                .containsProperty("spring.cloud.azure.keyvault.secret.property-source-enabled"))
                .isFalse();
            String baseUrl = "http://localhost:" + context.getEnvironment().getProperty("local.server.port");
            registerXssProbe(context);
            try (HttpClient client = HttpClient.newHttpClient()) {
                String input = "<script>alert(1)</script>한글";
                String encoded = URLEncoder.encode(input, StandardCharsets.UTF_8);
                String expected = xssEnabled ? "&lt;script&gt;alert(1)&lt;/script&gt;한글" : input;
                HttpResponse<String> query = client.send(
                    HttpRequest.newBuilder(URI.create(baseUrl + "/_test/xss?value=" + encoded))
                        .GET().build(), HttpResponse.BodyHandlers.ofString());
                assertThat(query.statusCode()).isEqualTo(200);
                assertThat(query.body()).isEqualTo(expected);
                HttpResponse<String> form = client.send(
                    HttpRequest.newBuilder(URI.create(baseUrl + "/_test/xss"))
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .POST(HttpRequest.BodyPublishers.ofString("value=" + encoded)).build(),
                    HttpResponse.BodyHandlers.ofString());
                assertThat(form.statusCode()).isEqualTo(200);
                assertThat(form.body()).isEqualTo(expected);
                HttpResponse<String> docs = client.send(
                    HttpRequest.newBuilder(URI.create(baseUrl + "/local-api-docs")).GET().build(),
                    HttpResponse.BodyHandlers.ofString());
                assertThat(docs.statusCode()).isEqualTo("dev".equals(profile) ? 200 : 404);
                if ("dev".equals(profile)) {
                    assertThat(docs.body()).contains("Local API", "openapi");
                    assertThat(docs.body()).contains("/apis/report/v1/sample/areas",
                        "/apis/report/v1/sample/institutioninfo", "/apis/report/v1/sample/nations");
                }
                HttpResponse<String> health = client.send(
                    HttpRequest.newBuilder(URI.create(baseUrl + "/actuator/health/liveness"))
                        .GET().build(),
                    HttpResponse.BodyHandlers.ofString());
                assertThat(health.statusCode()).isEqualTo(200);
                assertThat(health.body()).contains("UP");
            }
        }
    }

    private void registerXssProbe(ConfigurableApplicationContext context) throws Exception {
        var mapping = context.getBean("requestMappingHandlerMapping",
            RequestMappingHandlerMapping.class);
        var info = RequestMappingInfo.paths("/_test/xss")
            .methods(RequestMethod.GET, RequestMethod.POST)
            .produces("text/plain;charset=UTF-8")
            .options(mapping.getBuilderConfiguration()).build();
        mapping.registerMapping(info, new XssProbe(),
            XssProbe.class.getMethod("echo", String.class));
    }

    public static class XssProbe {
        @ResponseBody
        public String echo(@RequestParam("value") String value) {
            return value;
        }
    }
}
