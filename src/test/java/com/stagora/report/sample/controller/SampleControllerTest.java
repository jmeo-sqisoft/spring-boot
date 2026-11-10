package com.stagora.report.sample.controller;

import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.stagora.global.api.ApiResponseCode;
import com.stagora.global.exception.ApiException;
import com.stagora.global.exception.ApiExceptionHandler;
import com.stagora.report.sample.dto.AreasResponse;
import com.stagora.report.sample.dto.InstitutionInfoRequest;
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
                assertThat(exception.getErrorCode()).isEqualTo(ApiResponseCode.E40001.getCode());
            });
        verifyNoInteractions(service);
    }

    @ParameterizedTest
    @MethodSource("validIdentifiers")
    void forwardsValidIdentifiers(String clabId, String hospitalId) {
        var institution = new InstitutionInfoResponse();
        when(service.getInstitutionInfo(any(InstitutionInfoRequest.class))).thenReturn(institution);

        var response = controller.institutionInfo(clabId, hospitalId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().data()).isSameAs(institution);
        assertServiceRequest(clabId, hospitalId);
    }

    @ParameterizedTest
    @CsvSource({"ko-KR, 정상적으로 처리 되었습니다.", "en-US, Processed successfully."})
    void passesQueryParametersToServiceAsRequestDto(String language, String expectedMessage)
        throws Exception {
        var institution = new InstitutionInfoResponse();
        institution.setInstitutionId("hospital-id");
        when(service.getInstitutionInfo(any(InstitutionInfoRequest.class))).thenReturn(institution);

        var httpController = new SampleController(messageSource(), service);
        MockMvcBuilders.standaloneSetup(httpController).build()
            .perform(get("/apis/report/v1/sample/institutioninfo")
                .header("Accept-Language", language)
                .param("clabId", "clab-id").param("hospitalId", "hospital-id"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").value(aMapWithSize(3)))
            .andExpect(jsonPath("$.code").value("S0000"))
            .andExpect(jsonPath("$.message").value(expectedMessage))
            .andExpect(jsonPath("$.data.institutionId").value("hospital-id"));

        assertServiceRequest("clab-id", "hospital-id");
    }

    @ParameterizedTest
    @MethodSource("localizedMissingIdentifiers")
    void missingIdentifiersReturnBadRequestOverHttp(String language, String expectedMessage,
        String clabId, String hospitalId) throws Exception {
        var bundle = messageSource();
        var httpController = new SampleController(bundle, service);
        var request = get("/apis/report/v1/sample/institutioninfo")
            .header("Accept-Language", language);
        if (clabId != null) {
            request.param("clabId", clabId);
        }
        if (hospitalId != null) {
            request.param("hospitalId", hospitalId);
        }

        MockMvcBuilders.standaloneSetup(httpController)
            .setControllerAdvice(new ApiExceptionHandler(bundle)).build()
            .perform(request)
            .andExpect(status().isBadRequest())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").value(aMapWithSize(3)))
            .andExpect(jsonPath("$.code").value("E40001"))
            .andExpect(jsonPath("$.message").value(expectedMessage))
            .andExpect(jsonPath("$.data").value(nullValue()));
        verifyNoInteractions(service);
    }

    @Test
    void normalizesWhitespaceBeforeForwardingIdentifiers() {
        controller.institutionInfo(" clab-id ", " \t ");

        assertServiceRequest("clab-id", null);
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

    private void assertServiceRequest(String clabId, String hospitalId) {
        var captor = ArgumentCaptor.forClass(InstitutionInfoRequest.class);
        verify(service).getInstitutionInfo(captor.capture());
        assertThat(captor.getValue().getClabId()).isEqualTo(clabId);
        assertThat(captor.getValue().getHospitalId()).isEqualTo(hospitalId);
    }

    private static Stream<Arguments> missingIdentifiers() {
        return Stream.of(null, "", " ", "\t").flatMap(clabId ->
            Stream.of(null, "", " ", "\t").map(hospitalId -> Arguments.of(clabId, hospitalId)));
    }

    private static Stream<Arguments> localizedMissingIdentifiers() {
        return Stream.of("ko-KR", "en-US").flatMap(language -> {
            String expectedMessage = "ko-KR".equals(language)
                ? "입력값 검증에 실패했습니다." : "Validation failed.";
            return Stream.of(null, "", " ", "\t").flatMap(clabId ->
                Stream.of(null, "", " ", "\t").map(hospitalId ->
                    Arguments.of(language, expectedMessage, clabId, hospitalId)));
        });
    }

    private static ResourceBundleMessageSource messageSource() {
        var bundle = new ResourceBundleMessageSource();
        bundle.setBasename("messages/messages");
        bundle.setDefaultEncoding("UTF-8");
        bundle.setFallbackToSystemLocale(false);
        return bundle;
    }

    private static Stream<Arguments> validIdentifiers() {
        return Stream.of(Arguments.of("clab-id", null), Arguments.of(null, "hospital-id"),
            Arguments.of("clab-id", "hospital-id"));
    }
}
