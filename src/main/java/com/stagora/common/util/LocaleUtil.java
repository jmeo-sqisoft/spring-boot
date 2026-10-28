package com.stagora.common.util;

import com.stagora.common.constants.Example;

import java.util.Locale;

public final class LocaleUtil {
	private static final String[] LOCATION_LANGS = {
		Example.LANG_KO, Example.LANG
	};

	private LocaleUtil() {
	}

	public static String getLocationLang(String lang) {
		String s = lang == null ? Example.LANG : lang.trim().toLowerCase();
		for (var item : LOCATION_LANGS) {
			if (item.equals(s)) {
				return s;
			}
		}
		return Example.LANG;
	}

	public static String getLanguage(Locale locale) {
		String lang = locale == null ? "" : locale.getLanguage();
		if (StringUtil.isBlank(lang)) {
			lang = Example.LANG;
		}
		return lang;
	}
}
