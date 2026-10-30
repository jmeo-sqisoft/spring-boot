package com.stagora.report.sample.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.stagora.report.sample.dto.InstitutionInfoRequest;
import com.stagora.report.sample.model.Area;
import com.stagora.report.sample.model.InstitutionInfo;
import com.stagora.report.sample.model.NationInfoGroup;

/**
 * mapper/sample/SampleMapper.xml의 SQL로 지역, 기관, 국가 정보를 조회한다.
 */
@Mapper
public interface SampleMapper {
    /**
     * 사용 중인 지역 코드를 정렬 순서 값에 따라 조회한다.
     */
    List<Area> getArea();

    /**
     * 요청의 hospitalId로 병원 정보를 조회한다. 결과가 없으면 null을 반환한다.
     */
    InstitutionInfo getHospitalInfo(InstitutionInfoRequest request);

    /**
     * 요청의 clabId로 검사실 정보와 제휴 병원 표시 정보를 조회한다.
     * 결과가 없으면 null을 반환한다.
     */
    InstitutionInfo getClabInfo(InstitutionInfoRequest request);

    /**
     * lang에 따라 국가·하위 지역 이름을 조회해 지역별로 묶는다.
     * ko는 한국어를 사용하고 그 외에는 영어를 사용한다.
     */
    List<NationInfoGroup> getNationInfo(@Param("lang") String lang);
}
