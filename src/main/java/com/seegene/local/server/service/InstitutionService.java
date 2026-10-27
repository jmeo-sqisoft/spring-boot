package com.seegene.local.server.service;

import com.seegene.local.server.dto.response.InstitutionInfoResponse;
import com.seegene.local.server.dto.vo.InstitutionInfoVO;
import com.seegene.local.server.mapper.InstitutionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InstitutionService {
	private final InstitutionMapper institutionMapper;

	public InstitutionInfoResponse getInstitutionInfo(String clabId, String hospitalId) {
		InstitutionInfoVO info;

		if (hospitalId != null && !hospitalId.isEmpty()) {
			info = institutionMapper.getHospitalInfo(hospitalId);
		} else {
			info = institutionMapper.getClabInfo(clabId);
		}
		if (info == null) {
			return null;
		}
		return new InstitutionInfoResponse(
			info.InstitutionId(),
			info.InstitutionName(),
			info.InstitutionType(),
			info.InstitutionCity(),
			info.InstitutionNationCode(),
			info.InstitutionNation(),
			info.InstitutionArea(),
			info.registrationDate(),
			info.expirationDate(),
			info.contactEmail(),
			info.dataType());
	}
}
