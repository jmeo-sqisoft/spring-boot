package com.stagora.report.sample.mapper;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Map;

import javax.sql.rowset.RowSetMetaDataImpl;
import javax.sql.rowset.RowSetProvider;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.executor.resultset.DefaultResultSetHandler;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.reflection.ParamNameResolver;
import org.apache.ibatis.scripting.defaults.DefaultParameterHandler;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.RowBounds;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.AdditionalAnswers;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stagora.global.enums.InstitutionType;
import com.stagora.report.sample.dto.InstitutionInfoRequest;
import com.stagora.report.sample.model.Area;
import com.stagora.report.sample.model.InstitutionInfo;
import com.stagora.report.sample.model.NationInfo;
import com.stagora.report.sample.model.NationInfoGroup;
import com.stagora.report.sample.model.NationRegion;

class SampleMapperTest {
    private Configuration configuration;

    @BeforeEach
    void loadMapper() throws Exception {
        configuration = new Configuration();
        String resource = "mapper/sample/SampleMapper.xml";
        try (InputStream stream = Resources.getResourceAsStream(resource)) {
            new XMLMapperBuilder(stream, configuration, resource,
                configuration.getSqlFragments()).parse();
        }
    }

    @Test
    void bindsAllQueriesToRenamedModels() {
        assertThat(configuration.hasMapper(SampleMapper.class)).isTrue();
        assertThat(resultMap("getArea").getType()).isEqualTo(Area.class);
        assertThat(resultMap("getHospitalInfo").getType()).isEqualTo(InstitutionInfo.class);
        assertThat(resultMap("getClabInfo").getType()).isEqualTo(InstitutionInfo.class);
        assertThat(resultMap("getNationInfo").getType()).isEqualTo(NationInfoGroup.class);
    }

    @Test
    void resolvesNestedNationAndRegionMappings() {
        ResultMap countries = nestedMap(resultMap("getNationInfo"), "nationInfoVOs");
        assertThat(countries.getType()).isEqualTo(NationInfo.class);
        ResultMap regions = nestedMap(countries, "nationRegions");
        assertThat(regions.getType()).isEqualTo(NationRegion.class);
        assertThat(regions.getConstructorResultMappings())
            .extracting(mapping -> mapping.getColumn())
            .containsExactly("nation_region_no", "nation_region_code", "nation_region_name");
    }

    @Test
    void preservesNamedQueryParameters() {
        assertParameters("getHospitalInfo", "hospitalId");
        assertParameters("getClabInfo", "clabId");
        assertParameters("getNationInfo", "lang", "lang", "lang");
    }

    @ParameterizedTest
    @ValueSource(strings = {"getHospitalInfo", "getClabInfo"})
    void bindsRequestFieldsToSqlParameters(String query) throws Exception {
        var request = new InstitutionInfoRequest();
        request.setClabId("clab-id");
        request.setHospitalId("hospital-id");
        var method = SampleMapper.class.getMethod(query, InstitutionInfoRequest.class);
        var parameters = new ParamNameResolver(configuration, method)
            .getNamedParams(new Object[] {request});
        var statement = configuration.getMappedStatement(
            SampleMapper.class.getName() + "." + query);
        var boundSql = statement.getBoundSql(parameters);
        var preparedStatement = mock(PreparedStatement.class);

        new DefaultParameterHandler(statement, parameters, boundSql)
            .setParameters(preparedStatement);

        String expected = "getHospitalInfo".equals(query) ? "hospital-id" : "clab-id";
        verify(preparedStatement).setString(1, expected);
    }

    private ResultMap resultMap(String query) {
        return configuration.getMappedStatement(SampleMapper.class.getName() + "." + query)
            .getResultMaps().getFirst();
    }

    @ParameterizedTest
    @ValueSource(strings = {"getHospitalInfo", "getClabInfo"})
    void mapsJdbcColumnsToInstitutionRecord(String query) throws Exception {
        boolean hospital = "getHospitalInfo".equals(query);
        String[] columns = {hospital ? "hospital_id" : "clab_id", "institution_name",
            "institution_type", "city_name", "nation_code", "nation_name", "area_code",
            "registration_date", hospital ? "expiration_date" : "contract_expiration_date",
            "contact_email", "data_type"};
        var metadata = new RowSetMetaDataImpl();
        metadata.setColumnCount(columns.length);
        for (int index = 0; index < columns.length; index++) {
            metadata.setColumnName(index + 1, columns[index]);
            metadata.setColumnLabel(index + 1, columns[index]);
            metadata.setColumnType(index + 1, index == 7 || index == 8
                ? Types.TIMESTAMP : Types.VARCHAR);
        }
        var start = Timestamp.valueOf("2026-01-01 00:00:00");
        var end = Timestamp.valueOf("2026-12-31 00:00:00");
        InstitutionType type = hospital ? InstitutionType.HOSPITAL : InstitutionType.CLAB;
        Object[] values = {"institution-id", "Institution", type.name(), "Seoul", "KR",
            "Korea", "AS", start, end, "test@example.com", "LIS"};
        try (var rows = RowSetProvider.newFactory().createCachedRowSet()) {
            rows.setMetaData(metadata);
            rows.moveToInsertRow();
            for (int index = 0; index < values.length; index++) {
                rows.updateObject(index + 1, values[index]);
            }
            rows.insertRow();
            rows.moveToCurrentRow();
            rows.beforeFirst();
            var jdbcStatement = mock(Statement.class);
            var connection = mock(Connection.class);
            when(connection.getMetaData()).thenReturn(mock(DatabaseMetaData.class));
            when(jdbcStatement.getConnection()).thenReturn(connection);
            var resultSet = mock(ResultSet.class, AdditionalAnswers.delegatesTo(rows));
            // JDK CachedRowSet은 isClosed를 구현하지 않아 이 메서드만 보완한다.
            doReturn(false).when(resultSet).isClosed();
            when(jdbcStatement.getResultSet()).thenReturn(resultSet);
            when(jdbcStatement.getUpdateCount()).thenReturn(-1);
            var mapped = configuration.getMappedStatement(
                SampleMapper.class.getName() + "." + query);
            var handler = new DefaultResultSetHandler(null, mapped, null, null,
                mapped.getBoundSql(new InstitutionInfoRequest()), RowBounds.DEFAULT);

            assertThat(handler.handleResultSets(jdbcStatement)).containsExactly(
                new InstitutionInfo("institution-id", "Institution", type, "Seoul", "KR",
                    "Korea", "AS", start, end, "test@example.com", "LIS"));
        }
    }

    private ResultMap nestedMap(ResultMap parent, String property) {
        String nestedId = parent.getResultMappings().stream()
            .filter(mapping -> property.equals(mapping.getProperty()))
            .findFirst().orElseThrow().getNestedResultMapId();
        return configuration.getResultMap(nestedId);
    }

    private void assertParameters(String query, String... names) {
        String statementId = SampleMapper.class.getName() + "." + query;
        var statement = configuration.getMappedStatement(statementId);
        assertThat(statement.getBoundSql(Map.of()).getParameterMappings())
            .extracting(mapping -> mapping.getProperty()).containsExactly(names);
    }
}
