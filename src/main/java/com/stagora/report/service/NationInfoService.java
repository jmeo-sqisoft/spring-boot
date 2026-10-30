package com.stagora.report.service;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.stagora.global.util.LocaleUtil;
import com.stagora.report.dto.converter.NationInfoConverter;
import com.stagora.report.dto.response.NationInfoResponse;
import com.stagora.report.dto.vo.NationInfoVO;
import com.stagora.report.mapper.NationInfoMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NationInfoService {
	private final NationInfoMapper nationInfoMapper;
	private final NationInfoConverter nationInfoConverter;

	public List<NationInfoResponse> getNationInfo(Locale locale) {
		List<NationInfoResponse> nations = nationInfoConverter.toResponses(
			nationInfoMapper.getNationInfo(LocaleUtil.getLocationLang(LocaleUtil.getLanguage(locale))));
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
