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

@Service
@RequiredArgsConstructor
public class SampleService {
    private final SampleMapper sampleMapper;
    private final AreasConverter areasConverter;
    private final InstitutionInfoConverter institutionInfoConverter;
    private final NationInfoConverter nationInfoConverter;

    public List<AreasResponse> getArea() {
        List<Area> areaInfo = sampleMapper.getArea();
        return areasConverter.toResponses(areaInfo);
    }

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
