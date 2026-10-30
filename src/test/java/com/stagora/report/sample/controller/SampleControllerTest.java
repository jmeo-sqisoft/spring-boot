package com.stagora.report.sample.controller;

import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.http.HttpStatus;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.stagora.global.api.ApiResponseCode;
import com.stagora.global.exception.ApiException;
import com.stagora.report.sample.dto.AreasResponse;
import com.stagora.report.sample.dto.InstitutionInfoResponse;
import com.stagora.report.sample.dto.NationInfoResponse;
import com.stagora.report.sample.service.SampleService;

class SampleControllerTest {

    private final SampleService service = mock(SampleService.class);
    private final StaticMessageSource messages = new StaticMessageSource();
    private final SampleController controller = new SampleController(messages, service);

    @AfterEach
    void resetLocale() {
        LocaleContextHolder.resetLocaleContext();
    }

    @Test
    void wrapsAreasWithLocalizedSuccessMessage() {
        messages.addMessage("success.default", Locale.KOREAN, "조회 성공");
        LocaleContextHolder.setLocale(Locale.KOREAN);
        var areas = List.of(new AreasResponse("AS", "Asia"));
        when(service.getArea()).thenReturn(areas);

        var response = controller.getAreas();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("S0000");
        assertThat(response.getBody().message()).isEqualTo("조회 성공");
        assertThat(response.getBody().data()).isSameAs(areas);
    }

    @ParameterizedTest
    @MethodSource("missingIdentifiers")
    void rejectsMissingIdentifiersBeforeCallingService(String clabId, String hospitalId) {
        assertThatThrownBy(() -> controller.institutionInfo(clabId, hospitalId))
            .isInstanceOfSatisfying(ApiException.class, exception -> {
                assertThat(exception.getStatusCode()).isEqualTo(400);
                assertThat(exception.getErrorCode()).isEqualTo(ApiResponseCode.E00400.getCode());
            });
        verifyNoInteractions(service);
    }

    @ParameterizedTest
    @MethodSource("validIdentifiers")
    void forwardsValidIdentifiers(String clabId, String hospitalId) {
        var institution = new InstitutionInfoResponse();
        when(service.getInstitutionInfo(clabId, hospitalId)).thenReturn(institution);

        var response = controller.institutionInfo(clabId, hospitalId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().data()).isSameAs(institution);
        verify(service).getInstitutionInfo(clabId, hospitalId);
    }

    @Test
    void forwardsNationLocaleAndReturnsSuccessEnvelope() {
        var nations = List.of(new NationInfoResponse());
        when(service.getNationInfo(Locale.KOREAN)).thenReturn(nations);

        var response = controller.getNations(Locale.KOREAN);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("S0000");
        assertThat(response.getBody().data()).isSameAs(nations);
        verify(service).getNationInfo(Locale.KOREAN);
    }

    private static Stream<Arguments> missingIdentifiers() {
        return Stream.of(null, "", " ", "\t").flatMap(clabId ->
            Stream.of(null, "", " ", "\t").map(hospitalId -> Arguments.of(clabId, hospitalId)));
    }

    private static Stream<Arguments> validIdentifiers() {
        return Stream.of(Arguments.of("clab-id", null), Arguments.of(null, "hospital-id"),
            Arguments.of("clab-id", "hospital-id"));
    }
}
