package com.stagora.global.config;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.assertj.core.api.Assertions.assertThat;

class JpaAwareSqlLogFormatterTest {

	@ParameterizedTest
	@ValueSource(booleans = {false, true})
	void logsOnlyBoundSqlForMyBatisAndJpa(boolean jpa) {
		JpaAwareSqlLogFormatter formatter = new JpaAwareSqlLogFormatter(() -> jpa);

		String message = formatter.formatMessage(1, "now", 10L, "statement",
			"select * from example_rows where name = ?",
			"select * from example_rows where name = 'question? O''Brien'", "jdbc:postgresql:test");

		assertThat(message).containsOnlyOnce("example_rows")
			.contains("'question? O''Brien'", "statement", "10")
			.doesNotContain("name = ?");
	}

	@ParameterizedTest
	@ValueSource(booleans = {false, true})
	void emptyEventsDoNotFallBackToPreparedSql(boolean jpa) {
		JpaAwareSqlLogFormatter formatter = new JpaAwareSqlLogFormatter(() -> jpa);

		String message = formatter.formatMessage(1, "now", 10L, "commit", "select ?", "", "jdbc:postgresql:test");

		assertThat(message).contains("commit").doesNotContain("select ?");
	}
}
