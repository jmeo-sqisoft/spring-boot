package com.seegene.local.base.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 문자열 유틸리티. dateRange.py, utils.py 마이그레이션.
 * <p>설계 기준: doc/public/spec/architecture/00-master-design-report.md §2.2</p>
 */
public final class StringUtil {
	private StringUtil() {
	}

	/**
	 * null 또는 blank(빈 문자열·공백만) 여부.
	 *
	 * @param str 검사할 문자열
	 * @return null이거나 blank이면 true
	 */
	public static boolean isBlank(String str) {
		return str == null || str.isBlank();
	}

	/**
	 * snake_case → camelCase 변환.
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
			}
			else {
				result.append(capitalizeNext ? Character.toUpperCase(c) : Character.toLowerCase(c));
				capitalizeNext = false;
			}
		}
		return result.toString();
	}

	public static String sortNumericList(String list) {
		if (list == null) {
			return null;
		}

		var sortedList = sortNumericList(Arrays.asList(list.split(",")));
		return String.join(",", sortedList.toArray(new String[0]));
	}

	/**
	 * 숫자 리스트 정렬.
	 */
	public static List<String> sortNumericList(List<String> list) {
		if (list == null) {
			return Collections.emptyList();
		}
		return list.stream()
			.sorted((a, b) -> {
				try {
					return Double.compare(Double.parseDouble(a), Double.parseDouble(b));
				}
				catch (NumberFormatException e) {
					return a.compareTo(b);
				}
			})
			.toList();
	}

	/**
	 * 문자열 리스트 정렬.
	 */
	public static List<String> sortStrList(List<String> list) {
		if (list == null) {
			return Collections.emptyList();
		}
		return list.stream().sorted().toList();
	}
}
