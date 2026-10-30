package com.stagora.report.sample.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.stagora.report.sample.model.NationInfo;
import com.stagora.report.sample.model.NationInfoGroup;
import com.stagora.report.sample.model.NationRegion;

class SampleConvertersTest {

    @Test
    void preservesAllNestedNationFieldsAfterModelRename() {
        NationInfo nation = new NationInfo();
        nation.setNationInfoNo(1L);
        nation.setNationCode("KR");
        nation.setNationName("Korea");
        nation.setLatitude(new BigDecimal("36.549855"));
        nation.setLongitude(new BigDecimal("127.83961"));
        nation.setIsParticipating(true);
        nation.setNationRegions(List.of(new NationRegion(11, "11", "Seoul")));
        NationInfoGroup group = new NationInfoGroup();
        group.setAreaCode("AS");
        group.setAreaName("Asia");
        group.setNationInfoVOs(List.of(nation));

        var response = Mappers.getMapper(NationInfoConverter.class).toResponse(group);

        assertThat(response.getAreaCode()).isEqualTo("AS");
        assertThat(response.getAreaName()).isEqualTo("Asia");
        assertThat(response.getNationInfoVOs()).hasSize(1);
        assertThat(response.getNationInfoVOs().getFirst())
            .usingRecursiveComparison().isEqualTo(nation);
    }

    @Test
    void convertsMissingModelsToNullResponses() {
        assertThat(Mappers.getMapper(AreasConverter.class).toResponse(null)).isNull();
        assertThat(Mappers.getMapper(InstitutionInfoConverter.class).toResponse(null)).isNull();
        assertThat(Mappers.getMapper(NationInfoConverter.class).toResponse(null)).isNull();
    }

    @Test
    void preservesEmptyAndMissingCollections() {
        var areas = Mappers.getMapper(AreasConverter.class);
        var nations = Mappers.getMapper(NationInfoConverter.class);

        assertThat(areas.toResponses(List.of())).isEmpty();
        assertThat(nations.toResponses(List.of())).isEmpty();
        assertThat(areas.toResponses(null)).isNull();
        assertThat(nations.toResponses(null)).isNull();
        assertThat(nations.toResponse(new NationInfoGroup()).getNationInfoVOs()).isNull();
    }
}
