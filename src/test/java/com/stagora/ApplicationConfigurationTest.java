package com.stagora;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Locale;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.context.ConfigurableApplicationContext;
import static org.assertj.core.api.Assertions.assertThat;

import com.p6spy.engine.spy.P6DataSource;
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
    @ValueSource(strings = {"dev", "uat", "prd"})
    void profilesStartWithoutExternalServices(String profile) throws Exception {
        SpringApplication application = new SpringApplication(Application.class);
        application.setWebApplicationType(WebApplicationType.SERVLET);
        try (ConfigurableApplicationContext context = application.run(
                "--spring.profiles.active=" + profile,
                "--server.port=0",
                "--spring.datasource.local.url=jdbc:postgresql://localhost:1/config_test",
                "--spring.jpa.properties.hibernate.boot.allow_jdbc_metadata_access=false",
                "--spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect")) {
            DataSource dataSource = context.getBean(DataSource.class);
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
            try (HttpClient client = HttpClient.newHttpClient()) {
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
}
