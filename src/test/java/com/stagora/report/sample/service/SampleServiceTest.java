package com.stagora.report.sample.service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mapstruct.factory.Mappers;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.stagora.global.enums.InstitutionType;
import com.stagora.report.sample.dto.AreasConverter;
import com.stagora.report.sample.dto.AreasResponse;
import com.stagora.report.sample.dto.InstitutionInfoConverter;
import com.stagora.report.sample.dto.InstitutionInfoRequest;
import com.stagora.report.sample.dto.InstitutionInfoResponse;
import com.stagora.report.sample.dto.NationInfoConverter;
import com.stagora.report.sample.dto.NationInfoResponse;
import com.stagora.report.sample.mapper.SampleMapper;
import com.stagora.report.sample.model.Area;
import com.stagora.report.sample.model.InstitutionInfo;
import com.stagora.report.sample.model.NationInfo;
import com.stagora.report.sample.model.NationInfoGroup;

class SampleServiceTest {
    private final SampleMapper mapper = mock(SampleMapper.class);
    private final SampleService service = new SampleService(mapper,
        Mappers.getMapper(AreasConverter.class),
        Mappers.getMapper(InstitutionInfoConverter.class),
        Mappers.getMapper(NationInfoConverter.class));
    @Test
    void mapsAreaFieldsAndPreservesOrderAndNullNames() {
        when(mapper.getArea()).thenReturn(List.of(new Area("AS", "Asia"), new Area("EU", null)));

        List<AreasResponse> responses = service.getArea();

        assertThat(responses).extracting(AreasResponse::getAreaCode).containsExactly("AS", "EU");
        assertThat(responses).extracting(AreasResponse::getAreaName).containsExactly("Asia", null);
    }

    @Test
    void returnsEmptyListWhenNoAreasExist() {
        when(mapper.getArea()).thenReturn(List.of());

        assertThat(service.getArea()).isEmpty();
    }

    @Test
    void prioritizesHospitalAndMapsAllResponseFields() {
        Timestamp registrationDate = Timestamp.valueOf("2026-08-03 15:54:03");
        Timestamp expirationDate = Timestamp.valueOf("2026-12-03 00:54:03");
        var request = request("clab-id", "hospital-id");
        when(mapper.getHospitalInfo(request)).thenReturn(new InstitutionInfo(
            "hospital-id", "Hospital", InstitutionType.HOSPITAL, "Seoul", "KR",
            "Republic of Korea", "AS", registrationDate, expirationDate,
            "contact@example.com", "LIS"));

        InstitutionInfoResponse response = service.getInstitutionInfo(request);

        assertThat(response).usingRecursiveComparison().isEqualTo(new InstitutionInfoResponse(
            "hospital-id", "Hospital", InstitutionType.HOSPITAL, "Seoul", "KR",
            "Republic of Korea", "AS", registrationDate, expirationDate,
            "contact@example.com", "LIS"));
        verify(mapper).getHospitalInfo(request);
        verifyNoMoreInteractions(mapper);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void queriesClabWhenHospitalIdIsMissing(String hospitalId) {
        var request = request("clab-id", hospitalId);
        when(mapper.getClabInfo(request)).thenReturn(new InstitutionInfo(
            "clab-id", "Lab", InstitutionType.AFFILIATED,
            null, null, null, null, null, null, null, "HL7"));

        InstitutionInfoResponse response = service.getInstitutionInfo(request);

        assertThat(response.getInstitutionId()).isEqualTo("clab-id");
        assertThat(response.getInstitutionType()).isEqualTo(InstitutionType.AFFILIATED);
        assertThat(response.getRegistrationDate()).isNull();
        verify(mapper).getClabInfo(request);
        verifyNoMoreInteractions(mapper);
    }

    @Test
    void returnsNullWhenInstitutionDoesNotExist() {
        assertThat(service.getInstitutionInfo(request("missing-clab", null))).isNull();
        assertThat(service.getInstitutionInfo(request(null, "missing-hospital"))).isNull();
    }

    @Test
    void sortsAreasAndCountriesWithNullNamesLast() {
        NationInfo unnamed = country(null);
        NationInfo korea = country("Korea");
        NationInfo japan = country("Japan");
        NationInfoGroup europe = area("Europe", List.of());
        NationInfoGroup asia = area("Asia", List.of(unnamed, korea, japan));
        NationInfoGroup unnamedArea = area(null, null);
        when(mapper.getNationInfo("ko")).thenReturn(List.of(europe, unnamedArea, asia));

        List<NationInfoResponse> result = service.getNationInfo(Locale.KOREAN);

        assertThat(result).extracting(NationInfoResponse::getAreaName)
            .containsExactly("Asia", "Europe", null);
        assertThat(result.getFirst().getAreaCode()).isEqualTo("AS");
        assertThat(result.getFirst().getNationInfoVOs()).containsExactly(japan, korea, unnamed);
        assertThat(result.get(1).getNationInfoVOs()).isEmpty();
        assertThat(result.get(2).getNationInfoVOs()).isNull();
        verify(mapper).getNationInfo("ko");
    }

    @Test
    void returnsEmptyListAndUsesEnglishForMissingLocale() {
        when(mapper.getNationInfo("en")).thenReturn(List.of());

        assertThat(service.getNationInfo(null)).isEmpty();
        verify(mapper).getNationInfo("en");
    }

    @Test
    void usesEnglishForUnsupportedLocale() {
        when(mapper.getNationInfo("en")).thenReturn(List.of());

        assertThat(service.getNationInfo(Locale.FRENCH)).isEmpty();
        verify(mapper).getNationInfo("en");
    }

    private static InstitutionInfoRequest request(String clabId, String hospitalId) {
        var request = new InstitutionInfoRequest();
        request.setClabId(clabId);
        request.setHospitalId(hospitalId);
        return request;
    }

    private static NationInfo country(String name) {
        NationInfo nation = new NationInfo();
        nation.setNationName(name);
        return nation;
    }

    private static NationInfoGroup area(String name, List<NationInfo> nations) {
        NationInfoGroup response = new NationInfoGroup();
        response.setAreaCode("Asia".equals(name) ? "AS" : null);
        response.setAreaName(name);
        response.setNationInfoVOs(nations);
        return response;
    }
}
