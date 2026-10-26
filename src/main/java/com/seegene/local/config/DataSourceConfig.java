package com.seegene.local.config;

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

/** Shares the primary HikariCP DataSource between MyBatis and JPA. */
@Configuration
public class DataSourceConfig {

	@Bean
	@ConfigurationProperties("spring.datasource.local")
	public DataSourceProperties localDataSourceProperties() {
		return new DataSourceProperties();
	}

	@Bean
	@Primary
	public DataSource localDataSource(
			DataSourceProperties localDataSourceProperties,
			Environment environment,
			@Value("${app.sql-logging.enabled:false}") boolean sqlLoggingEnabled) {
		HikariDataSource hikariDataSource = localDataSourceProperties.initializeDataSourceBuilder()
			.type(HikariDataSource.class)
			.build();

		Binder.get(environment)
			.bind("spring.datasource.local.hikari", Bindable.ofInstance(hikariDataSource));

		if (!sqlLoggingEnabled) {
			return hikariDataSource;
		}

		System.setProperty("p6spy.config.appender", "com.p6spy.engine.spy.appender.Slf4JLogger");
		System.setProperty("p6spy.config.logMessageFormat", JpaAwareSqlLogFormatter.class.getName());
		P6ModuleManager.getInstance().reload();

		return new P6DataSource(hikariDataSource);
	}
}
