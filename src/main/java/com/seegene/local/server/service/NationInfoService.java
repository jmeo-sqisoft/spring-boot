package com.seegene.local.server.service;

import com.seegene.local.base.util.LocaleUtil;
import com.seegene.local.server.dto.response.NationInfoResponse;
import com.seegene.local.server.dto.vo.NationInfoVO;
import com.seegene.local.server.mapper.NationInfoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class NationInfoService {
	private final NationInfoMapper nationInfoMapper;

	public List<NationInfoResponse> getNationInfo(Locale locale) {
		List<NationInfoResponse> nations = nationInfoMapper.getNationInfo(LocaleUtil.getLocationLang(LocaleUtil.getLanguage(locale)));
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
