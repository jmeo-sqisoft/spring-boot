package com.stagora;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
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
import tools.jackson.databind.json.JsonMapper;

class ApplicationConfigurationTest {
    private static final String KEY_VAULT_PROPERTY_SOURCE = "spring.cloud.azure.keyvault.secret.property-source-enabled";

    @ParameterizedTest
    @CsvSource({"dev, true, true, debug", "uat, true, false, debug",
        "prd, false, false, warn", "perf, false, false, debug", "onprem, false, false, warn"})
    void profilesStartWithoutExternalServices(String profile, boolean isSqlLoggingEnabled,
        boolean isSwaggerEnabled, String expectedLogLevel) throws Exception {
        SpringApplication application = new SpringApplication(Application.class);
        application.setWebApplicationType(WebApplicationType.SERVLET);
        var arguments = new ArrayList<>(List.of("--spring.profiles.active=" + profile,
                "--server.port=0", "--spring.datasource.local.url=jdbc:postgresql://localhost:1/config_test",
                "--spring.datasource.local.username=test", "--spring.datasource.local.password=test",
                "--spring.jpa.properties.hibernate.boot.allow_jdbc_metadata_access=false",
                "--spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect"));
        if ("dev".equals(profile)) {
            arguments.add("--" + KEY_VAULT_PROPERTY_SOURCE + "=false");
        }
        try (ConfigurableApplicationContext context = application.run(arguments.toArray(String[]::new))) {
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
            assertThat(context.getEnvironment().getProperty(KEY_VAULT_PROPERTY_SOURCE, Boolean.class))
                .isEqualTo("dev".equals(profile) ? Boolean.FALSE : null);
            String baseUrl = "http://localhost:" + context.getEnvironment().getProperty("local.server.port");
            try (HttpClient client = HttpClient.newHttpClient()) {
                HttpResponse<String> invalidPatientDetail = client.send(HttpRequest.newBuilder(URI.create(baseUrl
                        + "/apis/reports/v1/patient/detail?sampleId=bad%20id"))
                        .header("Accept-Language", "en").GET().build(),
                    HttpResponse.BodyHandlers.ofString());
                assertThat(invalidPatientDetail.statusCode()).isEqualTo(400);
                assertThat(invalidPatientDetail.body()).contains("E40001", "Validation failed.");
                HttpResponse<String> docs = client.send(
                    HttpRequest.newBuilder(URI.create(baseUrl + "/local-api-docs")).GET().build(),
                    HttpResponse.BodyHandlers.ofString());
                assertThat(docs.statusCode()).isEqualTo(isSwaggerEnabled ? 200 : 404);
                if (isSwaggerEnabled) {
                    var schemas = JsonMapper.builder().build().readTree(docs.body()).path("components").path("schemas");
                    var schema = schemas.path("PatientListResponse");
                    var properties = schema.path("properties");
                    for (String field : List.of("page", "size", "totalPages")) {
                        assertThat(properties.has(field)).isFalse();
                    }
                    var requestProperties = schemas.path("PatientListSearchRequest").path("properties");
                    assertThat(requestProperties.has("page")).isFalse();
                    assertThat(requestProperties.has("size")).isFalse();
                    assertThat(schemas.path("PatientPageImageResponse").path("properties").has("page")).isTrue();
                    assertThat(properties.has("summary")).isFalse();
                    assertThat(properties.path("sampleSummary").path("$ref").asString())
                        .isEqualTo("#/components/schemas/PatientSampleSummaryResponse");
                    assertThat(properties.path("classificationSummary").path("$ref").asString())
                        .isEqualTo("#/components/schemas/PatientClassificationSummaryResponse");
                    assertThat(schema.path("required").toString()).contains("sampleSummary", "classificationSummary");
                    var classificationProperties = schemas.path("PatientClassificationSummaryResponse")
                        .path("properties");
                    assertThat(classificationProperties.has("panelCounts")).isFalse();
                    for (String panel : List.of("panelRtiCount", "panelAbiCount", "panelGtiCount")) {
                        assertThat(classificationProperties.path(panel).path("format").asString()).isEqualTo("int64");
                    }
                    assertThat(schemas.has("PatientPanelCountResponse")).isFalse();
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
