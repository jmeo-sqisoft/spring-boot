package com.stagora.reports.controller;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mapstruct.factory.Mappers;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.MissingServletRequestParameterException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.stagora.global.exception.ApiExceptionHandler;
import com.stagora.reports.dto.PatientDetailSearchRequest;
import com.stagora.reports.dto.PatientSearchConverter;
import com.stagora.reports.mapper.PatientReportMapper;
import com.stagora.reports.model.PatientPageContent;
import com.stagora.reports.model.PatientSearch;
import com.stagora.reports.service.PatientReportService;

class PatientReportDetailControllerTest {
    private static final String ID = "01a01ce1-5a44-7416-943e-598621a158a2";
    private final PatientReportMapper resultMapper = mock(PatientReportMapper.class);
    private final MockMvc mvc = createMvc();

    @ParameterizedTest
    @MethodSource("detailCases")
    void returnsListFieldsAndOneOrTwoPngPages(String language, String message, int pageCount) throws Exception {
        when(resultMapper.getPatientDetail(new PatientDetailSearchRequest(ID))).thenReturn(report());
        byte[] first = png(0xff336699);
        byte[] second = png(0xff993366);
        var pages = new ArrayList<PatientPageContent>();
        if (pageCount == 2) {
            pages.add(new PatientPageContent(2, second));
        }
        pages.add(new PatientPageContent(1, first));
        when(resultMapper.getPatientPageContents(ID)).thenReturn(pages);

        var result = mvc.perform(get("/apis/reports/v1/patient/detail")
                .param("sampleId", " " + ID.toUpperCase(Locale.ROOT) + " ").header("Accept-Language", language))
            .andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
            .andExpect(header().string("X-Content-Type-Options", "nosniff"))
            .andExpect(jsonPath("$.code").value("S0000")).andExpect(jsonPath("$.message").value(message))
            .andExpect(jsonPath("$.data.report.sampleId").value(ID))
            .andExpect(jsonPath("$.data.report.panelCode").value("ABI"))
            .andExpect(jsonPath("$.data.report.panelName").value("호흡기 감염 종합"))
            .andExpect(jsonPath("$.data.report.testDate").value("2026-10-15"))
            .andExpect(jsonPath("$.data.report.positiveCount").value(5))
            .andExpect(jsonPath("$.data.report.reportCreatedDate").value("2026-10-15"))
            .andExpect(jsonPath("$.data.report.printCount").value(1))
            .andExpect(jsonPath("$.data.report.lastPrintedAt").value("2026-10-16T00:00:00"))
            .andExpect(jsonPath("$.data.pages.length()").value(pageCount))
            .andExpect(jsonPath("$.data.pages[0].page").value(1))
            .andExpect(jsonPath("$.data.pages[0].contentType").value("image/png"))
            .andExpect(jsonPath("$.data.pages[0].contents").value(Base64.getEncoder().encodeToString(first)));
        if (pageCount == 2) {
            result.andExpect(jsonPath("$.data.pages[1].page").value(2))
                .andExpect(jsonPath("$.data.pages[1].contentType").value("image/png"))
                .andExpect(jsonPath("$.data.pages[1].contents").value(Base64.getEncoder().encodeToString(second)));
        }
        verify(resultMapper).getPatientDetail(new PatientDetailSearchRequest(ID));
        verify(resultMapper).getPatientPageContents(ID);
        verifyNoMoreInteractions(resultMapper);
    }

    @Test
    void rejectsMissingReportBeforeLoadingImages() throws Exception {
        when(resultMapper.getPatientDetail(new PatientDetailSearchRequest(ID))).thenReturn(null);

        mvc.perform(get("/apis/reports/v1/patient/detail").param("sampleId", ID).header("Accept-Language", "en-US"))
            .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("E40401"))
            .andExpect(jsonPath("$.message").value("The requested resource was not found."))
            .andExpect(jsonPath("$.data").isEmpty());
        verify(resultMapper, never()).getPatientPageContents(anyString());
    }

    @Test
    void rejectsReportWithoutImages() throws Exception {
        when(resultMapper.getPatientDetail(new PatientDetailSearchRequest(ID))).thenReturn(report());
        when(resultMapper.getPatientPageContents(ID)).thenReturn(List.of());

        mvc.perform(get("/apis/reports/v1/patient/detail").param("sampleId", ID)).andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("E40401"));
        verify(resultMapper).getPatientPageContents(ID);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "not-a-uuid", "S26-1015-0232", "1-1-1-1-1"})
    void rejectsInvalidUuidBeforeQuerying(String id) throws Exception {
        mvc.perform(get("/apis/reports/v1/patient/detail").param("sampleId", id).header("Accept-Language", "en-US"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("E40001"))
            .andExpect(jsonPath("$.message").value("Validation failed."));
        verifyNoInteractions(resultMapper);
    }

    @Test
    void rejectsMissingSampleIdBeforeQuerying() throws Exception {
        mvc.perform(get("/apis/reports/v1/patient/detail")).andExpect(status().isBadRequest())
            .andExpect(result -> assertThat(result.getResolvedException())
                .isInstanceOfSatisfying(MissingServletRequestParameterException.class,
                    exception -> assertThat(exception.getParameterName()).isEqualTo("sampleId")));
        verifyNoInteractions(resultMapper);
    }

    private MockMvc createMvc() {
        var messages = new ResourceBundleMessageSource();
        messages.setBasename("messages/messages");
        messages.setDefaultEncoding("UTF-8");
        messages.setFallbackToSystemLocale(false);
        var service = new PatientReportService(resultMapper, Mappers.getMapper(PatientSearchConverter.class));
        return MockMvcBuilders.standaloneSetup(new PatientReportController(messages, service))
            .setControllerAdvice(new ApiExceptionHandler(messages)).build();
    }

    private static PatientSearch report() {
        return new PatientSearch(ID, "ABI", "호흡기 감염 종합", LocalDate.of(2026, 10, 15),
            5, LocalDate.of(2026, 10, 15), 1, LocalDateTime.of(2026, 10, 16, 0, 0));
    }

    private static byte[] png(int color) throws Exception {
        var image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, color);
        var output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        return output.toByteArray();
    }

    private static Stream<Arguments> detailCases() {
        return Stream.of("ko-KR", "en-US").flatMap(language -> {
            String message = "ko-KR".equals(language) ? "정상적으로 처리 되었습니다." : "Processed successfully.";
            return IntStream.of(1, 2).mapToObj(count -> Arguments.of(language, message, count));
        });
    }
}
