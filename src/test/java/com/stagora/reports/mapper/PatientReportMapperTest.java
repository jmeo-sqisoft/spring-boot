package com.stagora.reports.mapper;

import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.scripting.defaults.DefaultParameterHandler;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.stagora.reports.dto.PatientDetailSearchRequest;
import com.stagora.reports.dto.PatientListSearchRequest;
import com.stagora.reports.model.PatientListItem;
import com.stagora.reports.model.PatientListStatistics;
import com.stagora.reports.model.PatientSearch;

class PatientReportMapperTest {
    private static final String FIRST_SAMPLE_ID = "d1bad87f41a03fe91c0733a132de1e919b9c72e3dd8884005fffe79578f002ad";
    private static final String LIST_SORT_ORDER = """
        order by c.expt_dy desc
            , a.sample_id desc
            , case a.gmcs_test_cd
                  when 'RTI' then 0
                  when 'ABI' then 1
                  when 'GTI' then 2
                  else 3
              end
            , a.gmcs_test_cd
            , a.target_id
        """.replaceAll("\\s+", " ").trim();

    @ParameterizedTest
    @MethodSource("searchConditions")
    void listAndStatisticsApplyTabAndDateFilters(PatientListSearchRequest request) throws Exception {
        var configuration = configuration();
        var statement = configuration.getMappedStatement(PatientReportMapper.class.getName() + ".getPatientList");
        var count = configuration.getMappedStatement(PatientReportMapper.class.getName() + ".getPatientListStatistics");
        var bound = statement.getBoundSql(request);
        String sql = bound.getSql().replaceAll("\\s+", " ").trim();
        String countSql = count.getBoundSql(request).getSql().replaceAll("\\s+", " ").trim();
        String where = "where nullif(a.sample_id, '') is not null";
        assertThat(sql).containsOnlyOnce(where).endsWith(LIST_SORT_ORDER);
        String filters = sql.substring(sql.indexOf(where), sql.indexOf(LIST_SORT_ORDER));

        assertThat(countSql).doesNotContain("limit", "offset")
            .contains("count(1) as report_count", "count(distinct a.sample_id) as found_sample_id_count");
        for (String panel : List.of("RTI", "ABI", "GTI")) {
            assertThat(countSql).containsOnlyOnce("count(1) filter (where a.panel_code = '" + panel + "') as "
                + panel.toLowerCase(Locale.ROOT) + "_report_count");
        }
        assertThat(sql).containsOnlyOnce("/* PatientReportMapper.getPatientList */")
            .contains("from compre_sample_report_status a");
        assertThat(filters).doesNotContain("c.expt_dy <", "a.test_date", "limit", "offset");
        if ("ALL_TIME".equals(request.testDate())) {
            assertThat(filters).doesNotContain("c.expt_dy");
        }
        assertThat(statement.getResultMaps()).singleElement().satisfies(result ->
            assertThat(result.getType()).isEqualTo(PatientListItem.class));
        assertDateLowerBound(filters, "c.expt_dy", request.testDate());
        assertThat(countSql).contains("a.test_date <= current_date");
        assertDateLowerBound(countSql, "a.test_date", request.testDate());
        var prepared = mock(PreparedStatement.class);
        new DefaultParameterHandler(statement, request, bound).setParameters(prepared);
        int parameter = 1;
        if ("SAMPLE_ID".equals(request.searchType())) {
            assertThat(filters).contains("a.sample_id in")
                .doesNotContain("'COMPREHENSIVE_PANEL' =", "a.gmcs_test_cd =");
            assertThat(countSql).contains("a.sample_id in").doesNotContain("a.test_classification =", "a.disease =");
            for (String id : request.sampleIds()) {
                verify(prepared).setString(parameter++, id);
            }
        } else {
            assertThat(sql).doesNotContain("cte_input_samples", "unnest(array[");
            assertThat(countSql).doesNotContain("cte_input_samples", "unnest(array[");
            assertThat(filters).doesNotContain("a.sample_id in");
            assertThat(filters).containsOnlyOnce("'COMPREHENSIVE_PANEL' = ?");
            assertThat(countSql).containsOnlyOnce("a.test_classification = ?");
            verify(prepared).setString(parameter++, request.testClassification());
            if (!"ALL".equals(request.disease())) {
                assertThat(filters).contains("a.gmcs_test_cd = case ? when 'HPV_STI' then 'RTI' "
                    + "when 'RP' then 'ABI' when 'GI' then 'GTI' end");
                assertThat(countSql).contains("a.disease = ?");
                verify(prepared).setString(parameter++, request.disease());
            } else {
                assertThat(filters).doesNotContain("a.gmcs_test_cd =");
                assertThat(countSql).doesNotContain("a.disease =");
            }
        }
        assertThat(bound.getParameterMappings()).hasSize(parameter - 1);
        assertStatisticsBindings(count, request);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ALL", "COMPREHENSIVE_PANEL", "OTHER_PANELS"})
    void mapperBindsClassificationOrOmitsFilterForAll(String classification) throws Exception {
        // API의 현재 허용값과 별개로 Mapper의 분류 조건 확장 경로를 검증한다.
        var request = new PatientListSearchRequest("TEST_CLASSIFICATION", List.of(), classification, "ALL", "ALL_TIME");
        var configuration = configuration();
        for (String method : List.of("getPatientList", "getPatientListStatistics")) {
            var statement = configuration.getMappedStatement(PatientReportMapper.class.getName() + "." + method);
            var bound = statement.getBoundSql(request);
            var prepared = mock(PreparedStatement.class);
            String sql = bound.getSql().replaceAll("\\s+", " ").trim();

            new DefaultParameterHandler(statement, request, bound).setParameters(prepared);

            var properties = bound.getParameterMappings().stream().map(ParameterMapping::getProperty).toList();
            String predicate = "getPatientList".equals(method) ? "'COMPREHENSIVE_PANEL' = ?"
                : "a.test_classification = ?";
            if ("ALL".equals(classification)) {
                assertThat(sql).doesNotContain(predicate);
                assertThat(properties).doesNotContain("testClassification");
            } else {
                assertThat(sql).containsOnlyOnce(predicate);
                assertThat(properties).containsOnlyOnce("testClassification");
                verify(prepared).setString(1, classification);
            }
        }
    }

    private void assertDateLowerBound(String filters, String column, String period) {
        String lowerBound = column + " >= current_date";
        switch (period) {
            case "TODAY" -> assertThat(filters).contains(lowerBound + " ").doesNotContain(lowerBound + " -");
            case "LAST_7_DAYS" -> assertThat(filters).contains(lowerBound + " - 6");
            case "LAST_1_MONTH" -> assertThat(filters).contains(lowerBound + " - interval '1 month'");
            default -> assertThat(filters).doesNotContain(column + " >=");
        }
    }

    private void assertStatisticsBindings(MappedStatement count, PatientListSearchRequest request)
        throws SQLException {
        var statisticsSql = count.getBoundSql(request);
        var prepared = mock(PreparedStatement.class);
        new DefaultParameterHandler(count, request, statisticsSql).setParameters(prepared);
        int index = 1;
        if ("SAMPLE_ID".equals(request.searchType())) {
            for (String id : request.sampleIds()) {
                verify(prepared).setString(index++, id);
            }
        } else {
            verify(prepared).setString(index++, request.testClassification());
            if (!"ALL".equals(request.disease())) {
                verify(prepared).setString(index++, request.disease());
            }
        }
        assertThat(statisticsSql.getParameterMappings()).hasSize(index - 1);
    }

    @Test
    void loadsDetailAndBindsSingleIdWithoutListFilters() throws Exception {
        var configuration = configuration();
        String id = "e5e024838ed6cbb8a89c1008877a2e4a9e6e52a19e9379b76af9339f0f14cdd2";
        var statement = configuration.getMappedStatement(PatientReportMapper.class.getName() + ".getPatientDetail");
        var parameters = new PatientDetailSearchRequest(id);
        var sql = statement.getBoundSql(parameters);
        var prepared = mock(PreparedStatement.class);
        new DefaultParameterHandler(statement, parameters, sql).setParameters(prepared);
        assertThat(sql.getSql()).contains("'" + id + "'", "a.panel_name", "a.sample_id = ?")
            .containsOnlyOnce("/* PatientReportMapper.getPatientDetail */")
            .contains("current_date - 1 as test_date", "a.detected_analytes_count as positive_count")
            .doesNotContain("limit", "?::uuid");
        assertThat(sql.getParameterMappings()).hasSize(1);
        assertThat(statement.getResultMaps()).singleElement()
            .satisfies(mapping -> assertThat(mapping.getType()).isEqualTo(PatientSearch.class));
        verify(prepared).setString(1, id);
    }

    @Test
    void detailAndStatisticsUseTheSameTenFixtureReports() throws Exception {
        var configuration = configuration();
        String namespace = PatientReportMapper.class.getName();
        var listRequest = new PatientListSearchRequest("TEST_CLASSIFICATION", null, null, null, null)
            .normalized();
        String statisticsSql = configuration.getMappedStatement(namespace + ".getPatientListStatistics")
            .getBoundSql(listRequest).getSql().replaceAll("\\s+", " ").trim();
        var detailRequest = new PatientDetailSearchRequest(FIRST_SAMPLE_ID).normalized();
        String detailSql = configuration.getMappedStatement(namespace + ".getPatientDetail").getBoundSql(detailRequest)
            .getSql().replaceAll("\\s+", " ").trim();
        String statisticsSource = statisticsSql.substring(statisticsSql.indexOf("with cte_reports"),
            statisticsSql.indexOf(", cte_filtered")).trim();
        String detailSource = detailSql.substring(detailSql.indexOf("with cte_reports"),
            detailSql.indexOf("select a.sample_id"));

        assertThat(detailSource.trim()).isEqualTo(statisticsSource);
        assertThat(detailSql).endsWith("from cte_reports as a where a.sample_id = ?");
    }

    private Configuration configuration() throws Exception {
        var configuration = new Configuration();
        String resource = "mapper/reports/PatientReportMapper.xml";
        try (InputStream stream = Resources.getResourceAsStream(resource)) {
            new XMLMapperBuilder(stream, configuration, resource, configuration.getSqlFragments()).parse();
        }
        return configuration;
    }

    @Test
    void statisticsSeparatesExistingSamplesWithoutReportsFromUnknownIdsAndBindsAllInput() throws Exception {
        var request = new PatientListSearchRequest("SAMPLE_ID",
            List.of(FIRST_SAMPLE_ID, "e5e024838ed6cbb8a89c1008877a2e4a9e6e52a19e9379b76af9339f0f14cdd2",
                "S26-1015-9999"), null, null, "TODAY").normalized();
        var statement = configuration().getMappedStatement(
            PatientReportMapper.class.getName() + ".getPatientListStatistics");
        var bound = statement.getBoundSql(request);
        String sql = bound.getSql().replaceAll("\\s+", " ").trim();
        assertThat(sql).contains("count(distinct x.sample_id) from cte_reports as x",
                "count(distinct a.sample_id)",
                "not exists", "y.sample_id = x.sample_id", "as no_results_count", "as not_found_count",
                "from cte_input_samples as x where not exists ( select 1 from cte_reports as y")
            .containsOnlyOnce("cte_input_samples as (").containsOnlyOnce("unnest(array[ ? , ? , ? ]::text[])")
            .doesNotContain("limit", "offset", "S26-1015-9999");
        assertThat(statement.getResultMaps()).singleElement()
            .satisfies(result -> assertThat(result.getType()).isEqualTo(PatientListStatistics.class));
        assertThat(bound.getParameterMappings()).hasSize(3).extracting(ParameterMapping::getProperty)
            .doesNotContain("enteredCount");
        var prepared = mock(PreparedStatement.class);
        new DefaultParameterHandler(statement, request, bound).setParameters(prepared);
        for (int index = 0; index < 3; index++) {
            verify(prepared).setString(index + 1, request.sampleIds().get(index));
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 100, 500})
    void bindsEachInputIdOncePerQueryAndReusesInputCte(int count) throws Exception {
        var ids = IntStream.range(0, count).mapToObj(index -> "S" + index).toList();
        var request = new PatientListSearchRequest("SAMPLE_ID", ids, null, null, null).normalized();
        var configuration = configuration();
        for (String method : List.of("getPatientList", "getPatientListStatistics")) {
            var statement = configuration.getMappedStatement(PatientReportMapper.class.getName() + "." + method);
            var bound = statement.getBoundSql(request);
            String sql = bound.getSql().replaceAll("\\s+", " ").trim();
            var prepared = mock(PreparedStatement.class);

            new DefaultParameterHandler(statement, request, bound).setParameters(prepared);

            assertThat(sql).containsOnlyOnce("cte_input_samples as (").containsOnlyOnce("unnest(array[")
                .contains("from cte_input_samples as z");
            assertThat(bound.getParameterMappings()).hasSize(count)
                .extracting(ParameterMapping::getProperty).doesNotContain("testClassification", "disease");
            for (int index = 0; index < count; index++) {
                verify(prepared).setString(index + 1, ids.get(index));
            }
            if ("getPatientListStatistics".equals(method)) {
                assertThat(sql).contains("from cte_input_samples as x where not exists (");
            }
        }
    }

    @Test
    void reportRowsUseProvidedSampleIdsInOrderIncludingDuplicate() throws Exception {
        var expected = List.of(
            FIRST_SAMPLE_ID,
            "e5e024838ed6cbb8a89c1008877a2e4a9e6e52a19e9379b76af9339f0f14cdd2",
            "0b83f2fc4bf07a1cb34ff69c96758aec9e6d0d168d8e069db2e69d4872068909",
            "b009a5f7798b5fdc35935933cfbd7e01930a7aeb63b907db5876ff4e12f48953",
            "b009a5f7798b5fdc35935933cfbd7e01930a7aeb63b907db5876ff4e12f48953",
            "dfae5c9fe9508c46fb5ced6bc5c885594dcf9f1670664abb3359ab6e48c5b0fb",
            "e72f66026b3e166321f10c17c21b5cd57145dab3943e67d093627035020cf800",
            "6796a717a4a617c8c639ba595e331dec135b6121e28ec23a0f8b517b779fbe51",
            "4e2b85248d473cf46d9a684c5d4a2d7260d81e5bf0b6b656b43754b206a9bef1",
            "617a79c70e5449b52404a764d6b72f1d5c0622e6ab07c06a6b25df923e25cc20");
        var request = new PatientListSearchRequest("TEST_CLASSIFICATION", null, null, null, null).normalized();
        var configuration = configuration();
        for (String method : List.of("getPatientDetail", "getPatientListStatistics")) {
            var statement = configuration.getMappedStatement(PatientReportMapper.class.getName() + "." + method);
            Object parameters = "getPatientDetail".equals(method) ? new PatientDetailSearchRequest(FIRST_SAMPLE_ID)
                : request;
            String sql = statement.getBoundSql(parameters).getSql().replaceAll("\\s+", " ").trim();
            int end = "getPatientDetail".equals(method) ? sql.indexOf("select a.sample_id")
                : sql.indexOf(", cte_filtered");
            String panels = sql.substring(sql.indexOf("with cte_reports"), end);
            var sampleIds = Pattern.compile("'([^']+)' as sample_id").matcher(panels).results()
                .map(match -> match.group(1)).toList();

            assertThat(sampleIds).hasSize(10).containsExactlyElementsOf(expected);
            assertThat(sampleIds.stream().distinct().toList()).hasSize(9);
            assertThat(sql).contains("from cte_reports as a", "'c2eb2821-5623-3b5d-8272-19971c8b04c2' as report_id")
                .doesNotContain("generate_series", "cross join", "batch_no", "cte_panels", "cte_samples",
                    "S26-1015-0244");
            var rows = List.of(panels.split("union all"));
            assertThat(rows).hasSize(10);
            for (String row : rows) {
                if (row.contains("0 as print_count")) {
                    assertThat(row).contains("null::timestamp as last_printed_at");
                } else {
                    assertThat(row).contains("(current_date - 1)::timestamp as last_printed_at");
                }
            }
            assertThat(panels).contains("0 as print_count", "1 as print_count", "2 as print_count");
        }
    }

    private static Stream<Arguments> searchConditions() {
        return Stream.of("ALL_TIME", "TODAY", "LAST_7_DAYS", "LAST_1_MONTH").flatMap(period ->
            Stream.of("ALL", "HPV_STI", "RP", "GI").flatMap(disease -> Stream.of(
                new PatientListSearchRequest("SAMPLE_ID", List.of("S1", "S2"), null, disease, period),
                new PatientListSearchRequest("TEST_CLASSIFICATION", null, null, disease, period))
                .map(request -> Arguments.of(request.normalized()))));
    }
}
