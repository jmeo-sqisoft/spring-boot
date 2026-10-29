package com.stagora.local.service;

import org.springframework.stereotype.Service;

import com.stagora.local.dto.converter.InstitutionInfoConverter;
import com.stagora.local.dto.response.InstitutionInfoResponse;
import com.stagora.local.dto.vo.InstitutionInfoVO;
import com.stagora.local.mapper.InstitutionMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InstitutionService {
	private final InstitutionMapper institutionMapper;
	private final InstitutionInfoConverter institutionInfoConverter;

	public InstitutionInfoResponse getInstitutionInfo(String clabId, String hospitalId) {
		InstitutionInfoVO info;

		if (hospitalId != null && !hospitalId.isEmpty()) {
			info = institutionMapper.getHospitalInfo(hospitalId);
		} else {
			info = institutionMapper.getClabInfo(clabId);
		}
		return institutionInfoConverter.toResponse(info);
	}
}
