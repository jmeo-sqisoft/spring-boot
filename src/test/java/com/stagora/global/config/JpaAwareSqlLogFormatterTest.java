package com.stagora.global.config;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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

        assertThat(message).containsOnlyOnce("example_rows").contains("'question? O''Brien'", "statement", "10")
            .doesNotContain("name = ?");
    }

    @ParameterizedTest
    @MethodSource("emptyEvents")
    void emptyEventsDoNotFallBackToPreparedSql(boolean jpa, String sql) {
        JpaAwareSqlLogFormatter formatter = new JpaAwareSqlLogFormatter(() -> jpa);

        String message = formatter.formatMessage(1, "now", 10L, "commit", "select ?", sql, "jdbc:postgresql:test");

        assertThat(message).contains("commit").doesNotContain("select ?");
    }

    private static Stream<Arguments> emptyEvents() {
        return Stream.of(false, true).flatMap(jpa -> Stream.of(null, "", " \t ")
            .map(sql -> Arguments.of(jpa, sql)));
    }
}
