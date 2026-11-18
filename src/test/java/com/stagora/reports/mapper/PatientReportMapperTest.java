package com.stagora.reports.mapper;

import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
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
    @ParameterizedTest
    @MethodSource("searchConditions")
    void listAndCountShareTabAndDateFiltersAndBindPagination(PatientListSearchRequest request) throws Exception {
        var configuration = configuration();
        var statement = configuration.getMappedStatement(PatientReportMapper.class.getName() + ".getPatientList");
        var count = configuration.getMappedStatement(PatientReportMapper.class.getName() + ".getPatientListStatistics");
        var bound = statement.getBoundSql(request);
        String sql = bound.getSql().replaceAll("\\s+", " ").trim();
        String countSql = count.getBoundSql(request).getSql().replaceAll("\\s+", " ").trim();
        String filters = sql.substring(sql.lastIndexOf("where a.test_date"), sql.indexOf("order by a.test_date"));
        String listSource = sql.substring(sql.indexOf("with cte_reports"), sql.indexOf("select a.report_id")).trim();
        String statisticsSource = countSql.substring(countSql.indexOf("with cte_reports"),
            countSql.indexOf(", cte_filtered")).trim();

        assertThat(statisticsSource).isEqualTo(listSource);
        assertThat(countSql).contains(filters.trim()).doesNotContain("limit", "offset")
            .contains("count(1) as report_count", "count(distinct a.sample_id) as found_sample_id_count");
        assertThat(sql).containsOnlyOnce("/* PatientReportMapper.getPatientList */")
            .contains("a.detected_analytes_count")
            .endsWith("order by a.test_date desc , a.sample_id , a.report_id limit ? offset ((?::bigint - 1) * ?)");
        assertThat(statement.getResultMaps()).singleElement().satisfies(result ->
            assertThat(result.getType()).isEqualTo(PatientListItem.class));
        assertDateFilters(filters, request.testDate());
        var prepared = mock(PreparedStatement.class);
        new DefaultParameterHandler(statement, request, bound).setParameters(prepared);
        int parameter = 1;
        if ("SAMPLE_ID".equals(request.searchType())) {
            assertThat(filters).contains("a.sample_id in").doesNotContain("a.disease =");
            for (String id : request.sampleIds()) {
                verify(prepared).setString(parameter++, id);
            }
        } else {
            assertThat(sql).doesNotContain("cte_input_samples", "unnest(array[");
            assertThat(countSql).doesNotContain("cte_input_samples", "unnest(array[");
            assertThat(filters).doesNotContain("a.sample_id in");
            if (!"ALL".equals(request.testClassification())) {
                assertThat(filters).contains("a.test_classification = ?");
                verify(prepared).setString(parameter++, request.testClassification());
            } else {
                assertThat(filters).doesNotContain("a.test_classification =");
            }
            if (!"ALL".equals(request.disease())) {
                assertThat(filters).contains("a.disease = ?");
                verify(prepared).setString(parameter++, request.disease());
            } else {
                assertThat(filters).doesNotContain("a.disease =");
            }
        }
        verify(prepared).setInt(parameter++, request.size());
        verify(prepared).setInt(parameter++, request.page());
        verify(prepared).setInt(parameter, request.size());
        assertThat(bound.getParameterMappings()).hasSize(parameter);
        assertStatisticsBindings(count, request);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, Integer.MAX_VALUE})
    void bindsPageAndSizeForBigintOffsetCalculation(int page) throws Exception {
        var request = new PatientListSearchRequest("TEST_CLASSIFICATION", null, null, null, null, page, 100)
            .normalized();
        var statement = configuration().getMappedStatement(PatientReportMapper.class.getName() + ".getPatientList");
        var bound = statement.getBoundSql(request);
        var prepared = mock(PreparedStatement.class);

        new DefaultParameterHandler(statement, request, bound).setParameters(prepared);

        assertThat(bound.getSql().replaceAll("\\s+", " ").trim())
            .endsWith("limit ? offset ((?::bigint - 1) * ?)");
        assertThat(bound.getParameterMappings()).extracting(ParameterMapping::getProperty)
            .containsExactly("size", "page", "size");
        verify(prepared).setInt(1, 100);
        verify(prepared).setInt(2, page);
        verify(prepared).setInt(3, 100);
    }

    private void assertDateFilters(String filters, String period) {
        assertThat(filters).contains("a.test_date <= current_date");
        switch (period) {
            case "TODAY" -> assertThat(filters).contains("a.test_date >= current_date ");
            case "LAST_7_DAYS" -> assertThat(filters).contains("a.test_date >= current_date - 6");
            case "LAST_1_MONTH" -> assertThat(filters).contains("a.test_date >= current_date - interval '1 month'");
            default -> assertThat(filters).doesNotContain("a.test_date >=");
        }
    }

    private void assertStatisticsBindings(MappedStatement count, PatientListSearchRequest request)
        throws SQLException {
        if (!"SAMPLE_ID".equals(request.searchType())) {
            var statisticsSql = count.getBoundSql(request);
            var statisticsParameters = mock(PreparedStatement.class);
            new DefaultParameterHandler(count, request, statisticsSql).setParameters(statisticsParameters);
            int index = 1;
            if (!"ALL".equals(request.testClassification())) {
                verify(statisticsParameters).setString(index++, request.testClassification());
            }
            if (!"ALL".equals(request.disease())) {
                verify(statisticsParameters).setString(index++, request.disease());
            }
            assertThat(statisticsSql.getParameterMappings()).hasSize(index - 1);
        }
    }

    @Test
    void loadsDetailAndBindsSingleIdWithoutListFilters() throws Exception {
        var configuration = configuration();
        String id = "01a01ce1-5a44-7416-943e-598621a158a2";
        var statement = configuration.getMappedStatement(PatientReportMapper.class.getName() + ".getPatientDetail");
        var parameters = new PatientDetailSearchRequest(id);
        var sql = statement.getBoundSql(parameters);
        var prepared = mock(PreparedStatement.class);
        new DefaultParameterHandler(statement, parameters, sql).setParameters(prepared);
        assertThat(sql.getSql()).contains("'" + id + "'", "a.panel_name", "a.sample_id = ?")
            .containsOnlyOnce("/* PatientReportMapper.getPatientDetail */").doesNotContain("current_date", "limit");
        assertThat(sql.getParameterMappings()).hasSize(1);
        assertThat(statement.getResultMaps()).singleElement()
            .satisfies(mapping -> assertThat(mapping.getType()).isEqualTo(PatientSearch.class));
        verify(prepared).setString(1, id);
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
                "S26-1015-9999"), null, null, "TODAY", 2, 10).normalized();
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
        var request = new PatientListSearchRequest("SAMPLE_ID", ids, null, null, null, 1, 10).normalized();
        var configuration = configuration();
        for (String method : List.of("getPatientList", "getPatientListStatistics")) {
            var statement = configuration.getMappedStatement(PatientReportMapper.class.getName() + "." + method);
            var bound = statement.getBoundSql(request);
            String sql = bound.getSql().replaceAll("\\s+", " ").trim();
            var prepared = mock(PreparedStatement.class);

            new DefaultParameterHandler(statement, request, bound).setParameters(prepared);

            assertThat(sql).containsOnlyOnce("cte_input_samples as (").containsOnlyOnce("unnest(array[")
                .contains("from cte_input_samples as z");
            int paginationParameters = "getPatientList".equals(method) ? 3 : 0;
            assertThat(bound.getParameterMappings()).hasSize(count + paginationParameters);
            for (int index = 0; index < count; index++) {
                verify(prepared).setString(index + 1, ids.get(index));
            }
            if ("getPatientListStatistics".equals(method)) {
                assertThat(sql).contains("from cte_input_samples as x where not exists (");
            } else {
                verify(prepared).setInt(count + 1, 10);
                verify(prepared).setInt(count + 2, 1);
                verify(prepared).setInt(count + 3, 10);
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
        var request = new PatientListSearchRequest("TEST_CLASSIFICATION", null, null, null, null, 1, 10).normalized();
        var configuration = configuration();
        for (String method : List.of("getPatientList", "getPatientListStatistics")) {
            var statement = configuration.getMappedStatement(PatientReportMapper.class.getName() + "." + method);
            String sql = statement.getBoundSql(request).getSql();
            int end = "getPatientList".equals(method) ? sql.indexOf("select\n      a.report_id")
                : sql.indexOf(", cte_filtered");
            String panels = sql.substring(sql.indexOf("with cte_reports"), end);
            var sampleIds = Pattern.compile("'([^']+)' as sample_id").matcher(panels).results()
                .map(match -> match.group(1)).toList();

            assertThat(sampleIds).hasSize(10).containsExactlyElementsOf(expected);
            assertThat(sampleIds.stream().distinct().toList()).hasSize(9);
            assertThat(sql).contains("from cte_reports as a", "'c2eb2821-5623-3b5d-8272-19971c8b04c2' as report_id")
                .doesNotContain("generate_series", "cross join", "batch_no", "cte_panels", "cte_samples", "S26-1015-0244");
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
        return Stream.of("ALL_TIME", "TODAY", "LAST_7_DAYS", "LAST_1_MONTH").flatMap(period -> Stream.of(
                new PatientListSearchRequest("SAMPLE_ID", List.of("S1", "S2"), null, null, period, 3, 10),
                new PatientListSearchRequest("TEST_CLASSIFICATION", null, "ALL", "ALL", period, 3, 10),
                new PatientListSearchRequest("TEST_CLASSIFICATION", null, "COMPREHENSIVE_PANEL", "ALL", period, 3, 10),
                new PatientListSearchRequest("TEST_CLASSIFICATION", null, "OTHER_PANELS", "ALL", period, 3, 10),
                new PatientListSearchRequest("TEST_CLASSIFICATION", null, "COMPREHENSIVE_PANEL", "RP", period, 3, 10),
                new PatientListSearchRequest("TEST_CLASSIFICATION", null, "OTHER_PANELS", "HPV", period, 3, 10))
                .map(request -> Arguments.of(request.normalized())));
    }
}
