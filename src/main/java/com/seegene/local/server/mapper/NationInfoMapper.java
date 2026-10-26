package com.seegene.local.server.mapper;

import com.seegene.local.server.dto.response.NationInfoResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NationInfoMapper {

	List<NationInfoResponse> getNationInfo(@Param("lang") String lang);
}
