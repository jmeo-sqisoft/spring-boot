package com.stagora.report.sample.service;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.stagora.global.util.LocaleUtil;
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

import lombok.RequiredArgsConstructor;

/**
 * 조회 결과를 API 응답 객체로 변환
 */
@Service
@RequiredArgsConstructor
public class SampleService {
    private final SampleMapper sampleMapper;
    private final AreasConverter areasConverter;
    private final InstitutionInfoConverter institutionInfoConverter;
    private final NationInfoConverter nationInfoConverter;

    /**
     * 조회 순서를 유지해 지역 목록을 API 응답 객체 목록으로 변환한다.
     */
    public List<AreasResponse> getArea() {
        List<Area> areaInfo = sampleMapper.getArea();
        return areasConverter.toResponses(areaInfo);
    }

    /**
     * 병원 ID가 null이나 빈 문자열이 아니면 병원, 그 외에는 검사실을 조회한다.
     * 요청 객체를 Mapper에 그대로 전달하며, 조회 결과가 없으면 null을 반환한다.
     */
    public InstitutionInfoResponse getInstitutionInfo(InstitutionInfoRequest request) {
        String hospitalId = request.getHospitalId();
        InstitutionInfo info;

        if (hospitalId != null && !hospitalId.isEmpty()) {
            info = sampleMapper.getHospitalInfo(request);
        } else {
            info = sampleMapper.getClabInfo(request);
        }
        return institutionInfoConverter.toResponse(info);
    }

    /**
     * 한국어 또는 영어로 지역별 국가 목록을 조회하고, 미지원 언어는 영어로 처리한다.
     * 지역명과 국가명을 오름차순으로 정렬하며 이름이 null인 항목은 마지막에 배치한다.
     * 국가 하위 지역 목록은 조회 순서를 유지한다.
     */
    public List<NationInfoResponse> getNationInfo(Locale locale) {
        List<NationInfoResponse> nations = nationInfoConverter.toResponses(
            sampleMapper.getNationInfo(LocaleUtil.getLocationLang(LocaleUtil.getLanguage(locale))));
        nations.forEach(nation -> {
            if (nation.getNationInfoVOs() != null) {
                nation.setNationInfoVOs(nation.getNationInfoVOs().stream()
                    .sorted(Comparator.comparing(NationInfo::getNationName,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                    .toList());
            }
        });
        return nations.stream()
            .sorted(Comparator.comparing(NationInfoResponse::getAreaName,
                Comparator.nullsLast(Comparator.naturalOrder())))
            .toList();
    }
}
