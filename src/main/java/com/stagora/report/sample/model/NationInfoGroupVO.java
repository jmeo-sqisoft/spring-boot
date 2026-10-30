package com.stagora.report.sample.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NationInfoGroupVO {

	private String areaCode;
	private String areaName;
	private List<NationInfoVO> nationInfoVOs;
}
