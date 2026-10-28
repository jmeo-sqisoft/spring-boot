package com.stagora.local.dto.response;

import java.util.List;

import com.stagora.local.dto.vo.NationInfoVO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NationInfoResponse {
	private String areaCode;
	private String areaName;

	private List<NationInfoVO> nationInfoVOs;
}
