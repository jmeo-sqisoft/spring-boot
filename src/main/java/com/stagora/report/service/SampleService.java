package com.stagora.report.service;

import com.stagora.global.util.LocaleUtil;
import com.stagora.report.dto.converter.AreasConverter;
import com.stagora.report.dto.converter.InstitutionInfoConverter;
import com.stagora.report.dto.converter.NationInfoConverter;
import com.stagora.report.dto.response.AreasResponse;
import com.stagora.report.dto.response.InstitutionInfoResponse;
import com.stagora.report.dto.response.NationInfoResponse;
import com.stagora.report.dto.vo.AreaVO;
import com.stagora.report.dto.vo.InstitutionInfoVO;
import com.stagora.report.dto.vo.NationInfoVO;
import com.stagora.report.mapper.SampleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class SampleService {
	private final SampleMapper sampleMapper;
	private final AreasConverter areasConverter;
	private final InstitutionInfoConverter institutionInfoConverter;
	private final NationInfoConverter nationInfoConverter;

	public List<AreasResponse> getArea() {
		List<AreaVO> areaInfo = sampleMapper.getArea();
		return areasConverter.toResponses(areaInfo);
	}

	public InstitutionInfoResponse getInstitutionInfo(String clabId, String hospitalId) {
		InstitutionInfoVO info;

		if (hospitalId != null && !hospitalId.isEmpty()) {
			info = sampleMapper.getHospitalInfo(hospitalId);
		} else {
			info = sampleMapper.getClabInfo(clabId);
		}
		return institutionInfoConverter.toResponse(info);
	}

	public List<NationInfoResponse> getNationInfo(Locale locale) {
		List<NationInfoResponse> nations = nationInfoConverter.toResponses(
			sampleMapper.getNationInfo(LocaleUtil.getLocationLang(LocaleUtil.getLanguage(locale))));
		nations.forEach(nation -> {
			if (nation.getNationInfoVOs() != null) {
				nation.setNationInfoVOs(nation.getNationInfoVOs().stream()
					.sorted(Comparator.comparing(NationInfoVO::getNationName, Comparator.nullsLast(Comparator.naturalOrder())))
					.toList());
			}
		});
		return nations.stream()
			.sorted(Comparator.comparing(NationInfoResponse::getAreaName, Comparator.nullsLast(Comparator.naturalOrder())))
			.toList();
	}
}
