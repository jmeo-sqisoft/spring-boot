package com.stagora.local.dto.vo;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class NationInfoGroupVO {

	private String areaCode;
	private String areaName;
	private List<NationInfoVO> nationInfoVOs;
}
