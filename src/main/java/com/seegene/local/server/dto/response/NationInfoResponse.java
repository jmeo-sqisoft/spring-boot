package com.seegene.local.server.dto.response;

import com.seegene.local.server.dto.vo.NationInfoVO;
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
