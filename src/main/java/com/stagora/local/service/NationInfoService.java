package com.stagora.local.service;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.stagora.common.util.LocaleUtil;
import com.stagora.local.dto.converter.NationInfoConverter;
import com.stagora.local.dto.response.NationInfoResponse;
import com.stagora.local.dto.vo.NationInfoVO;
import com.stagora.local.mapper.NationInfoMapper;

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
