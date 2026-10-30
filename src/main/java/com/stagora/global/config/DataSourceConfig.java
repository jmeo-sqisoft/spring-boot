package com.stagora.global.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

import com.p6spy.engine.spy.P6DataSource;
import com.p6spy.engine.spy.P6ModuleManager;
import com.zaxxer.hikari.HikariDataSource;

/**
 * MyBatis와 JPA가 공유하는 기본 DataSource와 선택적인 SQL 로깅을 구성한다.
 */
@Configuration
public class DataSourceConfig {
    /**
     * spring.datasource.local의 접속 정보를 DataSourceProperties에 바인딩한다.
     */
    @Bean
    @ConfigurationProperties("spring.datasource.local")
    public DataSourceProperties localDataSourceProperties() {
        return new DataSourceProperties();
    }

    /**
     * 접속 정보와 HikariCP 옵션으로 기본 DataSource를 생성한다.
     * app.sql-logging.enabled가 true이면 P6DataSource로 감싸 SQL 로깅을 활성화한다.
     */
    @Bean
    @Primary
    public DataSource localDataSource(
            DataSourceProperties localDataSourceProperties,
            Environment environment,
            @Value("${app.sql-logging.enabled:false}") boolean sqlLoggingEnabled) {
        HikariDataSource hikariDataSource = localDataSourceProperties.initializeDataSourceBuilder()
            .type(HikariDataSource.class)
            .build();

        // 접속 정보와 별도로 설정한 풀 옵션과 JDBC 드라이버 프로퍼티를 반영한다.
        Binder.get(environment)
            .bind("spring.datasource.local.hikari", Bindable.ofInstance(hikariDataSource));

        if (!sqlLoggingEnabled) {
            return hikariDataSource;
        }

        // P6Spy 설정은 JVM 전역에 적용되며, 모듈을 다시 로드해 변경 내용을 반영한다.
        System.setProperty("p6spy.config.appender", "com.p6spy.engine.spy.appender.Slf4JLogger");
        System.setProperty("p6spy.config.logMessageFormat",
            JpaAwareSqlLogFormatter.class.getName());
        P6ModuleManager.getInstance().reload();

        return new P6DataSource(hikariDataSource);
    }
}
