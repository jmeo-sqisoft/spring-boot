package com.stagora.global.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 문자열 유틸리티.
 */
public final class StringUtil {
    private StringUtil() {
    }

    /**
     * null, 빈 문자열 또는 공백 문자만 있는 문자열이면 true를 반환한다.
     */
    public static boolean isBlank(String str) {
        return str == null || str.isBlank();
    }

    /**
     * 밑줄을 제거하고 바로 뒤 문자를 대문자로, 나머지 문자를 소문자로 변환한다.
     * null이나 공백뿐인 문자열은 입력 그대로 반환한다.
     */
    public static String snakeToCamel(String snake) {
        if (isBlank(snake)) {
            return snake;
        }
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = false;
        for (char c : snake.toCharArray()) {
            if (c == '_') {
                capitalizeNext = true;
            } else {
                result.append(capitalizeNext ? Character.toUpperCase(c) : Character.toLowerCase(c));
                capitalizeNext = false;
            }
        }
        return result.toString();
    }

    /**
     * 쉼표로 구분한 문자열을 숫자 비교 규칙으로 정렬해 다시 쉼표로 연결한다.
     * null이면 null을 반환한다. 항목의 공백은 제거하지 않는다.
     */
    public static String sortNumericList(String list) {
        if (list == null) {
            return null;
        }

        var sortedList = sortNumericList(Arrays.asList(list.split(",")));
        return String.join(",", sortedList.toArray(new String[0]));
    }

    /**
     * 두 항목을 double로 변환해 오름차순으로 비교한다.
     * 비교하는 값 중 숫자로 변환할 수 없는 값이 있으면 두 문자열을 사전순으로 비교한다.
     * 원본 목록을 변경하지 않으며 null 목록은 빈 목록으로 반환한다.
     */
    public static List<String> sortNumericList(List<String> list) {
        if (list == null) {
            return Collections.emptyList();
        }
        return list.stream()
            .sorted((a, b) -> {
                try {
                    return Double.compare(Double.parseDouble(a), Double.parseDouble(b));
                } catch (NumberFormatException e) {
                    return a.compareTo(b);
                }
            })
            .toList();
    }

    /**
     * 문자열의 자연 순서로 정렬한 새 목록을 반환한다.
     * 원본 목록을 변경하지 않으며 null 목록은 빈 목록으로 반환한다.
     */
    public static List<String> sortStrList(List<String> list) {
        if (list == null) {
            return Collections.emptyList();
        }
        return list.stream().sorted().toList();
    }
}
