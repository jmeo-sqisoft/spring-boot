package com.stagora.report.sample.controller;

import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.stagora.global.api.ApiResponseCode;
import com.stagora.global.exception.ApiException;
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

    @Test
    void passesQueryParametersToServiceAsRequestDto() throws Exception {
        var institution = new InstitutionInfoResponse();
        institution.setInstitutionId("hospital-id");
        when(service.getInstitutionInfo(any(InstitutionInfoRequest.class))).thenReturn(institution);

        var result = MockMvcBuilders.standaloneSetup(controller).build()
            .perform(get("/apis/report/v1/sample/institutioninfo")
                .param("clabId", "clab-id").param("hospitalId", "hospital-id"))
            .andExpect(status().isOk()).andReturn();

        assertThat(result.getResponse().getContentAsString()).contains("hospital-id", "S0000");
        assertServiceRequest("clab-id", "hospital-id");
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

    private static Stream<Arguments> validIdentifiers() {
        return Stream.of(Arguments.of("clab-id", null), Arguments.of(null, "hospital-id"),
            Arguments.of("clab-id", "hospital-id"));
    }
}
