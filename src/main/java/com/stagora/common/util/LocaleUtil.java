package com.stagora.common.util;

import java.util.Locale;

import com.stagora.common.constants.Language;

public final class LocaleUtil {
	private static final String[] LOCATION_LANGS = {
		Language.KOREAN, Language.ENGLISH
	};

	private LocaleUtil() {
	}

	public static String getLocationLang(String lang) {
		String s = lang == null ? Language.DEFAULT : lang.trim().toLowerCase();
		for (var item : LOCATION_LANGS) {
			if (item.equals(s)) {
				return s;
			}
		}
		return Language.DEFAULT;
	}

	public static String getLanguage(Locale locale) {
		String lang = locale == null ? "" : locale.getLanguage();
		if (StringUtil.isBlank(lang)) {
			lang = Language.DEFAULT;
		}
		return lang;
	}
}
