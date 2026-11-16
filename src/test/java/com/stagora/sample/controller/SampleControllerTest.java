package com.stagora.sample.controller;

import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.stagora.global.exception.ApiExceptionHandler;
import com.stagora.sample.dto.AreasResponse;
import com.stagora.sample.dto.InstitutionInfoRequest;
import com.stagora.sample.dto.InstitutionInfoResponse;
import com.stagora.sample.dto.NationInfoResponse;
import com.stagora.sample.service.SampleService;

class SampleControllerTest {
    private final SampleService service = mock(SampleService.class);
    private final MockMvc mvc = createMvc();

    @Test
    void areasEndpointReturnsFieldsAndLocalizedSuccess() throws Exception {
        when(service.getArea()).thenReturn(List.of(new AreasResponse("AS", "Asia")));
        mvc.perform(get("/apis/reports/v1/sample/areas").header("Accept-Language", "ko"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.code").value("S0000"))
            .andExpect(jsonPath("$.message").value("정상적으로 처리 되었습니다."))
            .andExpect(jsonPath("$.data.length()").value(1))
            .andExpect(jsonPath("$.data[0].areaCode").value("AS"))
            .andExpect(jsonPath("$.data[0].areaName").value("Asia"));
        verify(service).getArea();
        verifyNoMoreInteractions(service);
    }

    @ParameterizedTest
    @MethodSource("validIdentifiers")
    void passesQueryParametersToServiceAfterNormalization(String language, String expectedMessage,
        String clabId, String hospitalId, String expectedClabId, String expectedHospitalId) throws Exception {
        var institution = new InstitutionInfoResponse();
        String expectedId = expectedHospitalId == null ? expectedClabId : expectedHospitalId;
        institution.setInstitutionId(expectedId);
        when(service.getInstitutionInfo(any(InstitutionInfoRequest.class))).thenReturn(institution);
        var request = get("/apis/reports/v1/sample/institutioninfo").header("Accept-Language", language);
        if (clabId != null) {
            request.param("clabId", clabId);
        }
        if (hospitalId != null) {
            request.param("hospitalId", hospitalId);
        }
        mvc.perform(request).andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").value(aMapWithSize(3)))
            .andExpect(jsonPath("$.code").value("S0000"))
            .andExpect(jsonPath("$.message").value(expectedMessage))
            .andExpect(jsonPath("$.data.institutionId").value(expectedId));
        var captor = ArgumentCaptor.forClass(InstitutionInfoRequest.class);
        verify(service).getInstitutionInfo(captor.capture());
        assertThat(captor.getValue().getClabId()).isEqualTo(expectedClabId);
        assertThat(captor.getValue().getHospitalId()).isEqualTo(expectedHospitalId);
        verifyNoMoreInteractions(service);
    }

    @ParameterizedTest
    @MethodSource("localizedMissingIdentifiers")
    void missingIdentifiersReturnBadRequestOverHttp(String language, String expectedMessage,
        String clabId, String hospitalId) throws Exception {
        var request = get("/apis/reports/v1/sample/institutioninfo").header("Accept-Language", language);
        if (clabId != null) {
            request.param("clabId", clabId);
        }
        if (hospitalId != null) {
            request.param("hospitalId", hospitalId);
        }
        mvc.perform(request).andExpect(status().isBadRequest())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").value(aMapWithSize(3))).andExpect(jsonPath("$.code").value("E40001"))
            .andExpect(jsonPath("$.message").value(expectedMessage)).andExpect(jsonPath("$.data").value(nullValue()));
        verifyNoInteractions(service);
    }

    @Test
    void nationsEndpointForwardsAcceptLanguageLocaleAndReturnsFields() throws Exception {
        var nation = new NationInfoResponse();
        nation.setAreaCode("AS");
        nation.setAreaName("Asia");
        when(service.getNationInfo(Locale.KOREA)).thenReturn(List.of(nation));
        mvc.perform(get("/apis/reports/v1/sample/nations").header("Accept-Language", "ko-KR"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.code").value("S0000"))
            .andExpect(jsonPath("$.data.length()").value(1))
            .andExpect(jsonPath("$.data[0].areaCode").value("AS"))
            .andExpect(jsonPath("$.data[0].areaName").value("Asia"));
        verify(service).getNationInfo(Locale.KOREA);
        verifyNoMoreInteractions(service);
    }

    private MockMvc createMvc() {
        var messages = new ResourceBundleMessageSource();
        messages.setBasename("messages/messages");
        messages.setDefaultEncoding("UTF-8");
        messages.setFallbackToSystemLocale(false);
        return MockMvcBuilders.standaloneSetup(new SampleController(messages, service))
            .setControllerAdvice(new ApiExceptionHandler(messages)).build();
    }

    private static Stream<Arguments> localizedMissingIdentifiers() {
        return Stream.of("ko-KR", "en-US").flatMap(language -> {
            String message = "ko-KR".equals(language) ? "입력값 검증에 실패했습니다." : "Validation failed.";
            return Stream.of(null, "", " ", "\t").flatMap(clabId -> Stream.of(null, "", " ", "\t")
                .map(hospitalId -> Arguments.of(language, message, clabId, hospitalId)));
        });
    }

    private static Stream<Arguments> validIdentifiers() {
        return Stream.of("ko-KR", "en-US").flatMap(language -> {
            String message = "ko-KR".equals(language) ? "정상적으로 처리 되었습니다." : "Processed successfully.";
            return Stream.of(Arguments.of(language, message, "clab-id", null, "clab-id", null),
                Arguments.of(language, message, null, "hospital-id", null, "hospital-id"),
                Arguments.of(language, message, "clab-id", "hospital-id", "clab-id", "hospital-id"),
                Arguments.of(language, message, " clab-id ", " \t ", "clab-id", null));
        });
    }
}
