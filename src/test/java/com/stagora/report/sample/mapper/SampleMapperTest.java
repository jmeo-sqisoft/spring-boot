package com.stagora.report.sample.mapper;

import java.io.InputStream;
import java.sql.PreparedStatement;
import java.util.Map;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.reflection.ParamNameResolver;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.scripting.defaults.DefaultParameterHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

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
        var statement = configuration.getMappedStatement(SampleMapper.class.getName() + "." + query);
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
