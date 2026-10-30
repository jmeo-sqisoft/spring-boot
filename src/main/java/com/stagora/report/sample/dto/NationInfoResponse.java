package com.stagora.report.sample.dto;

import java.util.List;

import com.stagora.report.sample.model.NationInfo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NationInfoResponse {
	private String areaCode;
	private String areaName;

	private List<NationInfo> nationInfoVOs;
}
