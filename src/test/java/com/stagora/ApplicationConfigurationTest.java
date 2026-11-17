package com.stagora;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Locale;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.context.ConfigurableApplicationContext;
import static org.assertj.core.api.Assertions.assertThat;

import com.p6spy.engine.spy.P6DataSource;
import com.stagora.reports.controller.PatientReportController;
import com.stagora.reports.dto.PatientSearchConverter;
import com.stagora.reports.mapper.PatientReportMapper;
import com.stagora.reports.service.PatientReportService;
import com.zaxxer.hikari.HikariDataSource;

import jakarta.persistence.EntityManagerFactory;

class ApplicationConfigurationTest {
    @ParameterizedTest
    @CsvSource({"dev, true, true, debug", "uat, true, false, debug",
        "prd, false, false, warn", "perf, false, false, debug", "onprem, false, false, warn"})
    void profilesStartWithoutExternalServices(String profile, boolean isSqlLoggingEnabled,
        boolean isSwaggerEnabled, String expectedLogLevel) throws Exception {
        SpringApplication application = new SpringApplication(Application.class);
        application.setWebApplicationType(WebApplicationType.SERVLET);
        try (ConfigurableApplicationContext context = application.run("--spring.profiles.active=" + profile,
                "--server.port=0", "--spring.datasource.local.url=jdbc:postgresql://localhost:1/config_test",
                "--spring.datasource.local.username=test", "--spring.datasource.local.password=test",
                "--spring.jpa.properties.hibernate.boot.allow_jdbc_metadata_access=false",
                "--spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect")) {
            DataSource dataSource = context.getBean(DataSource.class);
            assertThat(context.getBean(PatientReportController.class)).isNotNull();
            assertThat(context.getBean(PatientReportService.class)).isNotNull();
            assertThat(context.getBean(PatientReportMapper.class)).isNotNull();
            assertThat(context.getBean(PatientSearchConverter.class)).isNotNull();
            HikariDataSource hikari = dataSource.unwrap(HikariDataSource.class);
            assertThat(hikari.getPoolName()).isEqualTo("gmcs-be-hikari-pool");
            assertThat(hikari.getDataSourceProperties()).containsEntry("ApplicationName", "gmcs");
            assertThat(dataSource instanceof P6DataSource).isEqualTo(isSqlLoggingEnabled);
            assertThat(context.getEnvironment().getProperty("app.sql-logging.enabled", Boolean.class))
                .isEqualTo(isSqlLoggingEnabled);
            assertThat(context.getEnvironment().getProperty("logging.level.com.stagora")).isEqualTo(expectedLogLevel);
            if ("perf".equals(profile)) {
                assertThat(context.getEnvironment().getProperty("logging.level.com.stagora.reports.mapper")).isNull();
                assertThat(LoggerFactory.getLogger("com.stagora.reports.mapper").isDebugEnabled()).isTrue();
            }
            assertThat(context.getBean(SqlSessionFactory.class).getConfiguration().getEnvironment().getDataSource())
                .isSameAs(dataSource);
            assertThat(context.getBean(EntityManagerFactory.class).getMetamodel().getEntities()).isEmpty();
            assertThat(context.getEnvironment().getProperty("mybatis.type-aliases-package")).isEqualTo("com.stagora");
            assertThat(context.getEnvironment().getProperty("springdoc.swagger-ui.enabled", Boolean.class))
                .isEqualTo(isSwaggerEnabled);
            assertThat(context.getMessage("success.default", null, Locale.ENGLISH))
                .isEqualTo("Processed successfully.");
            assertThat(context.getMessage("success.default", null, Locale.KOREAN))
                .isNotEqualTo("Processed successfully.");
            assertThat(context.getEnvironment()
                .containsProperty("spring.cloud.azure.keyvault.secret.property-source-enabled")).isFalse();
            String baseUrl = "http://localhost:" + context.getEnvironment().getProperty("local.server.port");
            try (HttpClient client = HttpClient.newHttpClient()) {
                HttpResponse<String> invalidPatientDetail = client.send(HttpRequest.newBuilder(URI.create(baseUrl
                        + "/apis/reports/v1/patient/detail?sampleId=invalid"))
                        .header("Accept-Language", "en").GET().build(),
                    HttpResponse.BodyHandlers.ofString());
                assertThat(invalidPatientDetail.statusCode()).isEqualTo(400);
                assertThat(invalidPatientDetail.body()).contains("E40001", "Validation failed.");
                HttpResponse<String> docs = client.send(
                    HttpRequest.newBuilder(URI.create(baseUrl + "/local-api-docs")).GET().build(),
                    HttpResponse.BodyHandlers.ofString());
                assertThat(docs.statusCode()).isEqualTo(isSwaggerEnabled ? 200 : 404);
                if (isSwaggerEnabled) {
                    assertThat(docs.body()).contains("Local API", "openapi")
                        .contains("\"sampleIds\"", "\"testClassification\"", "\"testDate\"",
                            "\"totalCount\"", "\"detectedAnalytesCount\"", "\"enteredSampleIdCount\"",
                            "\"foundSampleIdCount\"", "\"noResultsCount\"", "\"notFoundCount\"")
                        .contains("/apis/reports/v1/patient/list",
                            "/apis/reports/v1/patient/detail").doesNotContain("\"sampleIdsText\"", "\"periodMonths\"",
                            "/apis/reports/v1/reports", "/apis/reports/v1/sample/");
                    HttpResponse<String> ui = client.send(
                        HttpRequest.newBuilder(URI.create(baseUrl + "/swagger-ui/index.html"))
                            .GET().build(), HttpResponse.BodyHandlers.ofString());
                    assertThat(ui.statusCode()).isEqualTo(200);
                    assertThat(ui.body()).contains("swagger-ui", "swagger-ui-bundle.js");
                    HttpResponse<String> config = client.send(HttpRequest.newBuilder(URI.create(
                            baseUrl + "/local-api-docs/swagger-config"))
                            .GET().build(), HttpResponse.BodyHandlers.ofString());
                    assertThat(config.statusCode()).isEqualTo(200);
                    assertThat(config.body()).contains("/local-api-docs");
                    HttpResponse<String> script = client.send(HttpRequest.newBuilder(URI.create(
                            baseUrl + "/swagger-ui/swagger-ui-bundle.js"))
                            .method("HEAD", HttpRequest.BodyPublishers.noBody()).build(),
                        HttpResponse.BodyHandlers.ofString());
                    assertThat(script.statusCode()).isEqualTo(200);
                }
                HttpResponse<String> health = client.send(
                    HttpRequest.newBuilder(URI.create(baseUrl + "/actuator/health/liveness")).GET().build(),
                    HttpResponse.BodyHandlers.ofString());
                assertThat(health.statusCode()).isEqualTo(200);
                assertThat(health.body()).contains("UP");
            }
        }
    }
}
