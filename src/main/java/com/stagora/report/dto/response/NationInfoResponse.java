package com.stagora.report.dto.response;

import java.util.List;

import com.stagora.report.dto.vo.NationInfoVO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NationInfoResponse {
	private String areaCode;
	private String areaName;

	private List<NationInfoVO> nationInfoVOs;
}
