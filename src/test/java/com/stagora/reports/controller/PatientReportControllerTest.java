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
import com.stagora.reports.dto.PatientItemResponse;
import com.stagora.reports.dto.PatientListResponse;
import com.stagora.reports.dto.PatientListSearchRequest;
import com.stagora.reports.dto.PatientListSummaryResponse;
import com.stagora.reports.service.PatientReportService;

class PatientReportControllerTest {
    private static final String FIRST_SAMPLE_ID = "d1bad87f41a03fe91c0733a132de1e919b9c72e3dd8884005fffe79578f002ad";
    private final PatientReportService service = mock(PatientReportService.class);
    private final MockMvc mvc = createMvc();

    @ParameterizedTest
    @CsvSource({"ko-KR, 정상적으로 처리 되었습니다.", "en-US, Processed successfully."})
    void sampleTabDefaultsToAllTimeAndReturnsScreenFields(String language, String message) throws Exception {
        var request = new PatientListSearchRequest("SAMPLE_ID",
            List.of(FIRST_SAMPLE_ID, "S26-1015-9999"),
            "COMPREHENSIVE_PANEL", "ALL", "ALL_TIME", 1, 1);
        var date = LocalDate.of(2026, 11, 16);
        var item = new PatientItemResponse("c2eb2821-5623-3b5d-8272-19971c8b04c2", FIRST_SAMPLE_ID,
            "COMPREHENSIVE_PANEL", "HPV_STI", "Human Papillomavirus Infections + Sexually Transmitted Infections",
            "RTI", "HPV + STI Comprehensive Panel", date, 10, date, 0, null);
        var summary = new PatientListSummaryResponse(2, 1, 1, 0, 1);
        when(service.getPatientList(request)).thenReturn(new PatientListResponse(1, 1, 1, 1, summary, List.of(item)));

        mvc.perform(post("/apis/reports/v1/patient/list")
                .header("Accept-Language", language).contentType(MediaType.APPLICATION_JSON).content("""
                    {"sampleIds":[" %s, %s ",
                      "S26-1015-9999"],"size":1}
                    """.formatted(FIRST_SAMPLE_ID, FIRST_SAMPLE_ID)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.code").value("S0000"))
            .andExpect(jsonPath("$.message").value(message)).andExpect(jsonPath("$.data.totalCount").value(1))
            .andExpect(jsonPath("$.data.totalPages").value(1)).andExpect(jsonPath("$.data.page").value(1))
            .andExpect(jsonPath("$.data.size").value(1)).andExpect(jsonPath("$.data.items.length()").value(1))
            .andExpect(jsonPath("$.data.summary.enteredSampleIdCount").value(2))
            .andExpect(jsonPath("$.data.summary.foundSampleIdCount").value(1))
            .andExpect(jsonPath("$.data.summary.reportCount").value(1))
            .andExpect(jsonPath("$.data.summary.noResultsCount").value(0))
            .andExpect(jsonPath("$.data.summary.notFoundCount").value(1))
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
        verify(service).getPatientList(request);
    }

    @ParameterizedTest
    @CsvSource({"ALL_TIME,1,10", "TODAY,2,20", "LAST_7_DAYS,3,50", "LAST_1_MONTH,4,100"})
    void classificationTabForwardsClassificationDiseasePeriodAndPage(String date, int page, int size) throws Exception {
        var request = new PatientListSearchRequest("TEST_CLASSIFICATION", List.of(),
            "COMPREHENSIVE_PANEL", "GI", date, page, size);
        long total = "TODAY".equals(date) ? 0 : 1;
        var reportDate = LocalDate.of(2026, 11, 14);
        var item = new PatientItemResponse("6085db41-848b-e6eb-82c2-6abd7b0d0c03",
            "0b83f2fc4bf07a1cb34ff69c96758aec9e6d0d168d8e069db2e69d4872068909",
            "COMPREHENSIVE_PANEL", "GI", "Gastrointestinal Infections", "GTI", "Gastrointestinal Comprehensive Panel",
            reportDate, 10, reportDate, 0, null);
        var items = total > 0 && page == 1 ? List.of(item) : List.<PatientItemResponse>of();
        when(service.getPatientList(request))
            .thenReturn(new PatientListResponse(total, page, size, total, null, items));
        String body = """
            {"searchType":"test_classification","testClassification":"comprehensive_panel",
             "disease":" gi ","testDate":"%s","page":%d,"size":%d}
            """.formatted(date, page, size);
        mvc.perform(post("/apis/reports/v1/patient/list").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.totalCount").value(total))
            .andExpect(jsonPath("$.data.summary").value(nullValue()))
            .andExpect(jsonPath("$.data.items.length()").value(items.size()));
        verify(service).getPatientList(request);
    }

    @ParameterizedTest
    @CsvSource({"SAMPLE_ID,ALL", "SAMPLE_ID,HPV_STI", "SAMPLE_ID,RP", "SAMPLE_ID,GI",
        "TEST_CLASSIFICATION,ALL", "TEST_CLASSIFICATION,HPV_STI", "TEST_CLASSIFICATION,RP", "TEST_CLASSIFICATION,GI"})
    void bothTabsAcceptSupportedDiseasesAndDefaultToComprehensivePanel(String type, String disease) throws Exception {
        boolean sampleTab = "SAMPLE_ID".equals(type);
        var ids = sampleTab ? List.of("S1") : List.<String>of();
        var request = new PatientListSearchRequest(type, ids, "COMPREHENSIVE_PANEL", disease, "TODAY", 2, 10);
        long total = !sampleTab && ("ALL".equals(disease) || "HPV_STI".equals(disease)) ? 1 : 0;
        var summary = sampleTab ? new PatientListSummaryResponse(1, 0, 0, 0, 1) : null;
        // 두 번째 페이지이므로 조회 가능한 오늘의 리포트가 있어도 현재 페이지는 비어 있다.
        when(service.getPatientList(request))
            .thenReturn(new PatientListResponse(total, 2, 10, total, summary, List.of()));
        String body = """
            {"searchType":"%s","sampleIds":%s,"disease":"%s","testDate":"TODAY","page":2}
            """.formatted(type, sampleTab ? "[\"S1\"]" : "[]", disease);

        mvc.perform(post("/apis/reports/v1/patient/list").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isOk()).andExpect(jsonPath("$.code").value("S0000"))
            .andExpect(jsonPath("$.data.totalCount").value(total))
            .andExpect(jsonPath("$.data.items").isEmpty());
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
            "{\"searchType\":\"TEST_CLASSIFICATION\",\"testClassification\":\"UNKNOWN\"}",
            "{\"sampleIds\":[\"S1\"],\"page\":0}", "{\"sampleIds\":[\"S1\"],\"size\":0}",
            "{\"sampleIds\":[\"S1\"],\"size\":101}");
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

    private MockMvc createMvc() {
        var messages = new ResourceBundleMessageSource();
        messages.setBasename("messages/messages");
        messages.setDefaultEncoding("UTF-8");
        messages.setFallbackToSystemLocale(false);
        return MockMvcBuilders.standaloneSetup(new PatientReportController(messages, service))
            .setControllerAdvice(new ApiExceptionHandler(messages)).build();
    }

    @ParameterizedTest
    @ValueSource(strings = {"{", "{\"sampleIds\":{}}", "{\"sampleIds\":[\"S1\"],\"size\":\"ten\"}"})
    void rejectsUnreadableJsonBeforeCallingService(String body) throws Exception {
        mvc.perform(post("/apis/reports/v1/patient/list").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isBadRequest()).andExpect(result -> assertThat(result.getResolvedException())
                .isInstanceOf(HttpMessageNotReadableException.class));
        verifyNoInteractions(service);
    }
}
