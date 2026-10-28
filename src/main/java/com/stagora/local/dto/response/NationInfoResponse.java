package com.stagora.local.dto.response;

import com.stagora.local.dto.vo.NationInfoVO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class NationInfoResponse {
	String areaCode;
	String areaName;

	List<NationInfoVO> nationInfoVOs;
}
