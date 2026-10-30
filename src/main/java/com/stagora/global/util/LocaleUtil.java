package com.stagora.global.util;

import java.util.Locale;

import com.stagora.global.constants.Language;

/**
 * 로케일의 언어 코드를 추출하고 애플리케이션에서 지원하는 언어로 정규화한다.
 */
public final class LocaleUtil {
    private static final String[] LOCATION_LANGS = {
        Language.KOREAN, Language.ENGLISH
    };

    private LocaleUtil() {
    }

    /**
     * 앞뒤 공백과 대소문자를 정리해 ko 또는 en을 반환한다.
     * 입력이 없거나 지원하지 않는 언어이면 기본값인 en을 반환한다.
     */
    public static String getLocationLang(String lang) {
        String s = lang == null ? Language.DEFAULT : lang.trim().toLowerCase();
        for (var item : LOCATION_LANGS) {
            if (item.equals(s)) {
                return s;
            }
        }
        return Language.DEFAULT;
    }

    /**
     * 로케일의 언어 코드를 반환한다. 로케일이나 언어 코드가 없으면 en을 사용한다.
     * 지원 언어로 제한해야 하는 경우 getLocationLang으로 정규화한다.
     */
    public static String getLanguage(Locale locale) {
        String lang = locale == null ? "" : locale.getLanguage();
        if (StringUtil.isBlank(lang)) {
            lang = Language.DEFAULT;
        }
        return lang;
    }
}
