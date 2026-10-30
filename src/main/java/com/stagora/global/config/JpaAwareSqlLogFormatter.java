package com.stagora.global.config;

import java.util.function.BooleanSupplier;

import org.hibernate.engine.jdbc.internal.FormatStyle;

import com.p6spy.engine.spy.appender.MessageFormattingStrategy;
import com.p6spy.engine.spy.appender.MultiLineFormat;

/**
 * 바인딩된 SQL을 한 번만 출력하고, Hibernate 호출 스택이 있으면 SQL에 줄바꿈을 적용한다.
 */
public class JpaAwareSqlLogFormatter implements MessageFormattingStrategy {
    private static final StackWalker STACK_WALKER = StackWalker.getInstance();
    private final MultiLineFormat delegate = new MultiLineFormat();
    private final BooleanSupplier jpaExecution;

    /**
     * P6Spy가 직접 생성할 포맷터를 구성한다. Hibernate 실행 여부는 호출 스택으로 판별한다.
     */
    public JpaAwareSqlLogFormatter() {
        this(() -> STACK_WALKER.walk(frames -> frames.anyMatch(
            frame -> frame.getClassName().startsWith("org.hibernate."))));
    }

    // 테스트에서 호출 스택과 무관하게 Hibernate 실행 여부를 지정할 수 있도록 한다.
    JpaAwareSqlLogFormatter(BooleanSupplier jpaExecution) {
        this.jpaExecution = jpaExecution;
    }

    /**
     * 값이 치환된 SQL을 출력한다. 빈 SQL은 prepared SQL로 대체하지 않는다.
     */
    @Override
    public String formatMessage(int connectionId, String now, long elapsed,
        String category, String prepared, String sql, String url) {
        if (!jpaExecution.getAsBoolean()) {
            return delegate.formatMessage(connectionId, now, elapsed, category, "", sql, url);
        }
        // delegate에는 prepared SQL을 전달하지 않아 SQL이 중복 출력되지 않도록 한다.
        String formattedSql = sql == null || sql.isBlank()
            ? sql : FormatStyle.BASIC.getFormatter().format(sql);
        return delegate.formatMessage(connectionId, now, elapsed, category, "", formattedSql, url);
    }
}
