package com.stagora.global.api;

import java.util.Locale;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.http.HttpStatus;
import static org.assertj.core.api.Assertions.assertThat;

class AbstractApiControllerTest {
    private final StaticMessageSource messages = new StaticMessageSource();
    private final AbstractApiController controller = new AbstractApiController() {
        @Override
        protected MessageSource getMessageSource() {
            return messages;
        }
    };

    @AfterEach
    void resetLocale() {
        LocaleContextHolder.resetLocaleContext();
    }

    @Test
    void createsLocalizedSuccessResponseWithoutChangingData() {
        messages.addMessage("success.default", Locale.KOREAN, "정상 처리되었습니다.");
        LocaleContextHolder.setLocale(Locale.KOREAN);
        var data = Map.of("institutionId", "hospital-id");

        var response = controller.success(data);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("S0000");
        assertThat(response.getBody().message()).isEqualTo("정상 처리되었습니다.");
        assertThat(response.getBody().data()).isSameAs(data);
    }

    @Test
    void fallsBackToMessageKeyAndSupportsNullData() {
        var response = controller.success(null);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("success.default");
        assertThat(response.getBody().data()).isNull();
    }

    @Test
    void extractsLanguageAndDefaultsMissingLanguageToEnglish() {
        assertThat(controller.getLanguage(Locale.KOREA)).isEqualTo("ko");
        assertThat(controller.getLanguage(Locale.FRENCH)).isEqualTo("fr");
        assertThat(controller.getLanguage(Locale.ROOT)).isEqualTo("en");
        assertThat(controller.getLanguage(null)).isEqualTo("en");
    }
}
