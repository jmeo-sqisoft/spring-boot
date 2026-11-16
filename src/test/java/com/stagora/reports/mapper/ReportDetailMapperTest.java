package com.stagora.reports.mapper;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.util.Map;

import javax.sql.rowset.RowSetMetaDataImpl;
import javax.sql.rowset.RowSetProvider;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.executor.resultset.DefaultResultSetHandler;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.scripting.defaults.DefaultParameterHandler;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.RowBounds;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.AdditionalAnswers;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stagora.reports.model.PatientPageContent;

class ReportDetailMapperTest {
    private Configuration configuration;

    @BeforeEach
    void loadMapper() throws Exception {
        configuration = new Configuration();
        String resource = "mapper/reports/PatientReportMapper.xml";
        try (InputStream stream = Resources.getResourceAsStream(resource)) {
            new XMLMapperBuilder(stream, configuration, resource, configuration.getSqlFragments()).parse();
        }
    }

    @Test
    void bindsOnlyUuidForAllImagesAndKeepsPageOrdering() throws Exception {
        String id = "01a01ce1-5a44-7416-943e-598621a158a2";
        var statement = pageStatement();
        var parameters = Map.of("sampleId", id);
        var sql = statement.getBoundSql(parameters);
        var prepared = mock(PreparedStatement.class);

        new DefaultParameterHandler(statement, parameters, sql).setParameters(prepared);

        assertThat(sql.getSql()).contains("from comm_files", "a.id = ?::uuid", "order by a.page")
            .contains("a.page > 0", "octet_length(a.contents) > 0").doesNotContain("a.page = ?");
        assertThat(sql.getParameterMappings()).hasSize(1);
        assertThat(statement.getResultMaps()).singleElement()
            .satisfies(mapping -> assertThat(mapping.getType()).isEqualTo(PatientPageContent.class));
        verify(prepared).setString(1, id);
    }

    @Test
    void mapsJdbcByteaWithoutChangingImageBytes() throws Exception {
        var metadata = new RowSetMetaDataImpl();
        metadata.setColumnCount(2);
        metadata.setColumnName(1, "page");
        metadata.setColumnLabel(1, "page");
        metadata.setColumnType(1, Types.SMALLINT);
        metadata.setColumnName(2, "contents");
        metadata.setColumnLabel(2, "contents");
        metadata.setColumnType(2, Types.BINARY);
        byte[] contents = {0, 1, -1, 17};
        try (var rows = RowSetProvider.newFactory().createCachedRowSet()) {
            rows.setMetaData(metadata);
            rows.moveToInsertRow();
            rows.updateShort(1, (short)2);
            rows.updateBytes(2, contents);
            rows.insertRow();
            rows.moveToCurrentRow();
            rows.beforeFirst();
            var jdbcStatement = mock(Statement.class);
            var connection = mock(Connection.class);
            when(connection.getMetaData()).thenReturn(mock(DatabaseMetaData.class));
            when(jdbcStatement.getConnection()).thenReturn(connection);
            var resultSet = mock(ResultSet.class, AdditionalAnswers.delegatesTo(rows));
            // CachedRowSet이 구현하지 않는 isClosed만 보완한다.
            doReturn(false).when(resultSet).isClosed();
            when(jdbcStatement.getResultSet()).thenReturn(resultSet);
            when(jdbcStatement.getUpdateCount()).thenReturn(-1);
            var statement = pageStatement();
            var handler = new DefaultResultSetHandler(null, statement, null, null,
                statement.getBoundSql(Map.of("sampleId", "unused")), RowBounds.DEFAULT);

            assertThat(handler.handleResultSets(jdbcStatement)).singleElement()
                .isInstanceOfSatisfying(PatientPageContent.class, page -> {
                    assertThat(page.page()).isEqualTo(2);
                    assertThat(page.contents()).containsExactly(contents);
                });
        }
    }

    private MappedStatement pageStatement() {
        return configuration.getMappedStatement(PatientReportMapper.class.getName() + ".getPatientPageContents");
    }
}
