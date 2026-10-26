package com.seegene.local.global.config;

import com.p6spy.engine.spy.appender.MessageFormattingStrategy;
import com.p6spy.engine.spy.appender.MultiLineFormat;
import org.hibernate.engine.jdbc.internal.FormatStyle;

import java.util.function.BooleanSupplier;

/** JPA 실행에서는 바인딩된 SQL에 줄바꿈을 적용하고 나머지는 기존 P6Spy 형식을 유지한다. */
public class JpaAwareSqlLogFormatter implements MessageFormattingStrategy {

	private static final StackWalker STACK_WALKER = StackWalker.getInstance();
	private final MultiLineFormat delegate = new MultiLineFormat();
	private final BooleanSupplier jpaExecution;

	// P6Spy가 기본 생성자로 직접 생성하므로 Spring 빈 주입을 사용하지 않는다.
	public JpaAwareSqlLogFormatter() {
		this(() -> STACK_WALKER.walk(frames -> frames.anyMatch(frame -> frame.getClassName().startsWith("org.hibernate."))));
	}

	JpaAwareSqlLogFormatter(BooleanSupplier jpaExecution) {
		this.jpaExecution = jpaExecution;
	}

	@Override
	public String formatMessage(int connectionId, String now, long elapsed, String category, String prepared, String sql, String url) {
		// JDBC 실행 시점의 호출 스택으로 구분한다. SQL 문자열 모양으로 JPA 여부를 추측하지 않는다.
		if (!jpaExecution.getAsBoolean()) {
			return delegate.formatMessage(connectionId, now, elapsed, category, prepared, sql, url);
		}
		// 값이 치환된 SQL을 포맷하므로 원본 SQL이나 개별 바인딩 로그를 추가하지 않는다.
		String formattedSql = sql == null || sql.isBlank() ? sql : FormatStyle.BASIC.getFormatter().format(sql);
		return delegate.formatMessage(connectionId, now, elapsed, category, "", formattedSql, url);
	}
}
