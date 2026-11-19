package com.stagora.reports.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.stagora.global.exception.ApiExceptionHandler;
import com.stagora.reports.dto.PatientClassificationSummaryResponse;
import com.stagora.reports.dto.PatientItemResponse;
import com.stagora.reports.dto.PatientListResponse;
import com.stagora.reports.dto.PatientListSearchRequest;
import com.stagora.reports.dto.PatientSampleSummaryResponse;
import com.stagora.reports.service.PatientReportService;

class PatientReportControllerTest {
    private static final String FIRST_SAMPLE_ID = "d1bad87f41a03fe91c0733a132de1e919b9c72e3dd8884005fffe79578f002ad";
    private static final PatientSampleSummaryResponse ZERO_SAMPLE = new PatientSampleSummaryResponse(0, 0, 0, 0, 0);
    private static final PatientClassificationSummaryResponse ZERO_CLASSIFICATION =
        new PatientClassificationSummaryResponse(0, 0, 0, 0, 0);
    private final PatientReportService service = mock(PatientReportService.class);
    private final MockMvc mvc = createMvc();

    @ParameterizedTest
    @CsvSource({"ko-KR, 정상적으로 처리 되었습니다.", "en-US, Processed successfully."})
    void sampleTabDefaultsToAllTimeAndReturnsScreenFields(String language, String message) throws Exception {
        var request = new PatientListSearchRequest("SAMPLE_ID", List.of(FIRST_SAMPLE_ID, "S26-1015-9999"),
            "COMPREHENSIVE_PANEL", "ALL", "ALL_TIME");
        var item = firstRtiItem();
        var summary = new PatientSampleSummaryResponse(2, 1, 1, 0, 1);
        when(service.getPatientList(request))
            .thenReturn(new PatientListResponse(1, summary, ZERO_CLASSIFICATION, List.of(item)));

        var result = mvc.perform(post("/apis/reports/v1/patient/list")
                .header("Accept-Language", language).contentType(MediaType.APPLICATION_JSON).content("""
                    {"sampleIds":[" %s, %s ",
                      "S26-1015-9999"]}
                    """.formatted(FIRST_SAMPLE_ID, FIRST_SAMPLE_ID)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.code").value("S0000"))
            .andExpect(jsonPath("$.message").value(message)).andExpect(jsonPath("$.data.totalCount").value(1))
            .andExpect(jsonPath("$.data.totalPages").doesNotExist()).andExpect(jsonPath("$.data.page").doesNotExist())
            .andExpect(jsonPath("$.data.size").doesNotExist()).andExpect(jsonPath("$.data.items.length()").value(1))
            .andExpect(jsonPath("$.data.sampleSummary.enteredSampleIdCount").value(2))
            .andExpect(jsonPath("$.data.sampleSummary.foundSampleIdCount").value(1))
            .andExpect(jsonPath("$.data.sampleSummary.reportCount").value(1))
            .andExpect(jsonPath("$.data.sampleSummary.noResultsCount").value(0))
            .andExpect(jsonPath("$.data.sampleSummary.notFoundCount").value(1))
            .andExpect(jsonPath("$.data.summary").doesNotExist())
            .andExpect(jsonPath("$.data.classificationSummary.reportCount").value(0))
            .andExpect(jsonPath("$.data.classificationSummary.foundSampleIdCount").value(0))
            .andExpect(jsonPath("$.data.items[0].reportId").value("c2eb2821-5623-3b5d-8272-19971c8b04c2"))
            .andExpect(jsonPath("$.data.items[0].sampleId").value(FIRST_SAMPLE_ID))
            .andExpect(jsonPath("$.data.items[0].testClassification").value("COMPREHENSIVE_PANEL"))
            .andExpect(jsonPath("$.data.items[0].disease").value("HPV_STI"))
            .andExpect(jsonPath("$.data.items[0].diseaseName")
                .value("Human Papillomavirus Infections + Sexually Transmitted Infections"))
            .andExpect(jsonPath("$.data.items[0].panelCode").value("RTI"))
            .andExpect(jsonPath("$.data.items[0].panelName").value("HPV + STI Comprehensive Panel"))
            .andExpect(jsonPath("$.data.items[0].testDate").value("2026-11-16"))
            .andExpect(jsonPath("$.data.items[0].detectedAnalytesCount").value(10))
            .andExpect(jsonPath("$.data.items[0].reportCreatedDate").value("2026-11-16"))
            .andExpect(jsonPath("$.data.items[0].printCount").value(0))
            .andExpect(jsonPath("$.data.items[0].lastPrintedAt").value(nullValue()));
        assertPanelCounts(result, 0, 0, 0);
        verify(service).getPatientList(request);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ALL_TIME", "TODAY", "LAST_7_DAYS", "LAST_1_MONTH"})
    void classificationTabForwardsClassificationDiseaseAndPeriod(String date) throws Exception {
        var request = new PatientListSearchRequest("TEST_CLASSIFICATION", List.of(), "COMPREHENSIVE_PANEL", "GI", date);
        long total = "TODAY".equals(date) ? 0 : 1;
        var reportDate = LocalDate.of(2026, 11, 14);
        var item = new PatientItemResponse("6085db41-848b-e6eb-82c2-6abd7b0d0c03",
            "0b83f2fc4bf07a1cb34ff69c96758aec9e6d0d168d8e069db2e69d4872068909",
            "COMPREHENSIVE_PANEL", "GI", "Gastrointestinal Infections", "GTI", "Gastrointestinal Comprehensive Panel",
            reportDate, 10, reportDate, 0, null);
        var items = total > 0 ? List.of(item) : List.<PatientItemResponse>of();
        var classificationSummary = new PatientClassificationSummaryResponse(total, total, 0, 0, total);
        when(service.getPatientList(request))
            .thenReturn(new PatientListResponse(total, ZERO_SAMPLE, classificationSummary, items));
        String body = """
            {"searchType":"test_classification","testClassification":"comprehensive_panel",
             "disease":" gi ","testDate":"%s"}
            """.formatted(date);
        var result = mvc.perform(post("/apis/reports/v1/patient/list")
                .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.totalCount").value(total))
            .andExpect(jsonPath("$.data.summary").doesNotExist())
            .andExpect(jsonPath("$.data.sampleSummary.enteredSampleIdCount").value(0))
            .andExpect(jsonPath("$.data.sampleSummary.foundSampleIdCount").value(0))
            .andExpect(jsonPath("$.data.sampleSummary.reportCount").value(0))
            .andExpect(jsonPath("$.data.sampleSummary.noResultsCount").value(0))
            .andExpect(jsonPath("$.data.sampleSummary.notFoundCount").value(0))
            .andExpect(jsonPath("$.data.classificationSummary.reportCount").value(total))
            .andExpect(jsonPath("$.data.classificationSummary.foundSampleIdCount").value(total))
            .andExpect(jsonPath("$.data.items.length()").value(items.size()));
        assertPanelCounts(result, 0, 0, total);
        verify(service).getPatientList(request);
    }

    @ParameterizedTest
    @CsvSource({"SAMPLE_ID,ALL", "SAMPLE_ID,HPV_STI", "SAMPLE_ID,RP", "SAMPLE_ID,GI",
        "TEST_CLASSIFICATION,ALL", "TEST_CLASSIFICATION,HPV_STI", "TEST_CLASSIFICATION,RP", "TEST_CLASSIFICATION,GI"})
    void bothTabsAcceptSupportedDiseasesAndDefaultToComprehensivePanel(String type, String disease) throws Exception {
        boolean sampleTab = "SAMPLE_ID".equals(type);
        var ids = sampleTab ? List.of("S1") : List.<String>of();
        var request = new PatientListSearchRequest(type, ids, "COMPREHENSIVE_PANEL", disease, "TODAY");
        long total = !sampleTab && ("ALL".equals(disease) || "HPV_STI".equals(disease)) ? 1 : 0;
        var summary = sampleTab ? new PatientSampleSummaryResponse(1, 0, 0, 0, 1) : ZERO_SAMPLE;
        var classificationSummary = new PatientClassificationSummaryResponse(total, total, total, 0, 0);
        when(service.getPatientList(request))
            .thenReturn(new PatientListResponse(total, summary, classificationSummary,
                total == 0 ? List.of() : List.of(firstRtiItem())));
        String body = """
            {"searchType":"%s","sampleIds":%s,"disease":"%s","testDate":"TODAY"}
            """.formatted(type, sampleTab ? "[\"S1\"]" : "[]", disease);

        var result = mvc.perform(post("/apis/reports/v1/patient/list")
                .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isOk()).andExpect(jsonPath("$.code").value("S0000"))
            .andExpect(jsonPath("$.data.totalCount").value(total))
            .andExpect(jsonPath("$.data.summary").doesNotExist())
            .andExpect(jsonPath("$.data.sampleSummary.enteredSampleIdCount").value(sampleTab ? 1 : 0))
            .andExpect(jsonPath("$.data.sampleSummary.foundSampleIdCount").value(0))
            .andExpect(jsonPath("$.data.sampleSummary.reportCount").value(0))
            .andExpect(jsonPath("$.data.sampleSummary.noResultsCount").value(0))
            .andExpect(jsonPath("$.data.sampleSummary.notFoundCount").value(sampleTab ? 1 : 0))
            .andExpect(jsonPath("$.data.classificationSummary.reportCount").value(total))
            .andExpect(jsonPath("$.data.classificationSummary.foundSampleIdCount").value(total))
            .andExpect(jsonPath("$.data.items.length()").value(total));
        assertPanelCounts(result, total, 0, 0);
        verify(service).getPatientList(request);
    }

    @ParameterizedTest
    @MethodSource({"invalidRequests", "fixedFilterViolations"})
    void rejectsInvalidOrMixedTabConditionsBeforeCallingService(String body) throws Exception {
        mvc.perform(post("/apis/reports/v1/patient/list")
                .header("Accept-Language", "en-US").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("E40001"))
            .andExpect(jsonPath("$.message").value("Validation failed."))
            .andExpect(jsonPath("$.data").value(nullValue()));
        verifyNoInteractions(service);
    }

    private static Stream<String> invalidRequests() {
        return Stream.of("", "null", "{}", "{\"sampleIds\":[]}", "{\"sampleIds\":[null]}",
            "{\"sampleIds\":[\" \"]}", "{\"sampleIds\":[\"x' or 1=1--\"]}", "{\"searchType\":\"UNKNOWN\"}",
            "{\"sampleIds\":[\"S1\"],\"testDate\":\"LAST_3_MONTHS\"}",
            "{\"sampleIds\":[\"S1\"],\"testClassification\":\"OTHER_PANELS\"}",
            "{\"searchType\":\"TEST_CLASSIFICATION\",\"sampleIds\":[\"S1\"]}",
            "{\"searchType\":\"TEST_CLASSIFICATION\",\"disease\":\"HPV\"}",
            "{\"searchType\":\"TEST_CLASSIFICATION\",\"testClassification\":\"UNKNOWN\"}");
    }

    private static Stream<String> fixedFilterViolations() {
        return Stream.of("SAMPLE_ID", "TEST_CLASSIFICATION").flatMap(type -> {
            String ids = "SAMPLE_ID".equals(type) ? ",\"sampleIds\":[\"S1\"]" : "";
            String prefix = "{\"searchType\":\"" + type + "\"" + ids;
            return Stream.concat(
                Stream.of("ALL", "OTHER_PANELS", "UNKNOWN", "").map(classification ->
                    prefix + ",\"testClassification\":\"" + classification + "\"}"),
                Stream.of("HPV", "STI", "OTHERS", "UNKNOWN", "").map(disease ->
                    prefix + ",\"disease\":\"" + disease + "\"}"));
        });
    }

    private PatientItemResponse firstRtiItem() {
        var date = LocalDate.of(2026, 11, 16);
        return new PatientItemResponse("c2eb2821-5623-3b5d-8272-19971c8b04c2", FIRST_SAMPLE_ID,
            "COMPREHENSIVE_PANEL", "HPV_STI", "Human Papillomavirus Infections + Sexually Transmitted Infections",
            "RTI", "HPV + STI Comprehensive Panel", date, 10, date, 0, null);
    }

    private void assertPanelCounts(ResultActions result, long panelRtiCount, long panelAbiCount,
        long panelGtiCount) throws Exception {
        result.andExpect(jsonPath("$.data.classificationSummary.panelCounts").doesNotExist())
            .andExpect(jsonPath("$.data.classificationSummary.panelRtiCount").value(panelRtiCount))
            .andExpect(jsonPath("$.data.classificationSummary.panelAbiCount").value(panelAbiCount))
            .andExpect(jsonPath("$.data.classificationSummary.panelGtiCount").value(panelGtiCount));
    }

    private MockMvc createMvc() {
        var messages = new ResourceBundleMessageSource();
        messages.setBasename("messages/messages");
        messages.setDefaultEncoding("UTF-8");
        messages.setFallbackToSystemLocale(false);
        return MockMvcBuilders.standaloneSetup(new PatientReportController(messages, service))
            .setControllerAdvice(new ApiExceptionHandler(messages)).build();
    }

    @ParameterizedTest
    @ValueSource(strings = {"{", "{\"sampleIds\":{}}", "{\"searchType\":{}}"})
    void rejectsUnreadableJsonBeforeCallingService(String body) throws Exception {
        mvc.perform(post("/apis/reports/v1/patient/list").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isBadRequest()).andExpect(result -> assertThat(result.getResolvedException())
                .isInstanceOf(HttpMessageNotReadableException.class));
        verifyNoInteractions(service);
    }
}
