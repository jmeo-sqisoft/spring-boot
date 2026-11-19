package com.stagora.reports.mapper;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;
import java.util.stream.Stream;

import javax.sql.rowset.RowSetMetaDataImpl;
import javax.sql.rowset.RowSetProvider;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.executor.resultset.DefaultResultSetHandler;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.RowBounds;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.AdditionalAnswers;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.stagora.reports.dto.PatientListSearchRequest;
import com.stagora.reports.model.PatientListStatistics;

class PatientListStatisticsMapperTest {
    @ParameterizedTest
    @MethodSource("statisticsCases")
    void mapsTotalDistinctSampleAndPanelCountsInCorrectOrder(PatientListStatistics expected) throws Exception {
        var configuration = new Configuration();
        String resource = "mapper/reports/PatientReportMapper.xml";
        try (InputStream stream = Resources.getResourceAsStream(resource)) {
            new XMLMapperBuilder(stream, configuration, resource, configuration.getSqlFragments()).parse();
        }
        var columns = List.of("report_count", "found_sample_id_count", "no_results_count", "not_found_count",
            "rti_report_count", "abi_report_count", "gti_report_count");
        var values = List.of(expected.reportCount(), expected.foundSampleIdCount(), expected.noResultsCount(),
            expected.notFoundCount(), expected.rtiReportCount(), expected.abiReportCount(), expected.gtiReportCount());
        var metadata = new RowSetMetaDataImpl();
        metadata.setColumnCount(columns.size());
        for (int index = 0; index < columns.size(); index++) {
            metadata.setColumnName(index + 1, columns.get(index));
            metadata.setColumnLabel(index + 1, columns.get(index));
            metadata.setColumnType(index + 1, Types.BIGINT);
        }
        try (var rows = RowSetProvider.newFactory().createCachedRowSet()) {
            rows.setMetaData(metadata);
            rows.moveToInsertRow();
            for (int index = 0; index < values.size(); index++) {
                rows.updateLong(index + 1, values.get(index));
            }
            rows.insertRow();
            rows.moveToCurrentRow();
            rows.beforeFirst();
            var jdbc = mock(Statement.class);
            var connection = mock(Connection.class);
            when(connection.getMetaData()).thenReturn(mock(DatabaseMetaData.class));
            when(jdbc.getConnection()).thenReturn(connection);
            var resultSet = mock(ResultSet.class, AdditionalAnswers.delegatesTo(rows));
            // CachedRowSet이 구현하지 않는 isClosed만 보완한다.
            doReturn(false).when(resultSet).isClosed();
            when(jdbc.getResultSet()).thenReturn(resultSet);
            when(jdbc.getUpdateCount()).thenReturn(-1);
            var statement = configuration.getMappedStatement(
                PatientReportMapper.class.getName() + ".getPatientListStatistics");
            var request = new PatientListSearchRequest("TEST_CLASSIFICATION", null, null, null, null, 1, 10)
                .normalized();
            var handler = new DefaultResultSetHandler(null, statement, null, null,
                statement.getBoundSql(request), RowBounds.DEFAULT);

            assertThat(handler.handleResultSets(jdbc)).containsExactly(expected);
        }
    }

    private static Stream<PatientListStatistics> statisticsCases() {
        return Stream.of(new PatientListStatistics(5, 4, 0, 0, 2, 2, 1),
            new PatientListStatistics(2, 2, 0, 0, 0, 2, 0), new PatientListStatistics(1, 1, 1, 1, 1, 0, 0),
            new PatientListStatistics(0, 0, 0, 0, 0, 0, 0));
    }
}
