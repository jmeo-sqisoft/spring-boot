package com.stagora.reports.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.stagora.reports.dto.PatientDetailSearchRequest;
import com.stagora.reports.dto.PatientListSearchRequest;
import com.stagora.reports.model.PatientListItem;
import com.stagora.reports.model.PatientListStatistics;
import com.stagora.reports.model.PatientPageContent;
import com.stagora.reports.model.PatientSearch;

/**
 * 환자 리포트의 목록·통계·상세 정보와 페이지 이미지를 조회한다.
 */
@Mapper
public interface PatientReportMapper {
    /**
     * 검색 조건에 맞는 전체 리포트 수와 Sample ID별 조회 현황을 집계한다.
     */
    PatientListStatistics getPatientListStatistics(PatientListSearchRequest request);

    /**
     * 검색 조건에 맞는 전체 리포트를 정렬하여 조회한다.
     */
    List<PatientListItem> getPatientList(PatientListSearchRequest request);

    /**
     * Sample ID에 해당하는 리포트의 검사 정보와 출력 현황을 조회한다.
     */
    PatientSearch getPatientDetail(PatientDetailSearchRequest request);

    /**
     * Sample ID에 해당하는 전체 PNG 페이지를 페이지 번호순으로 조회한다.
     */
    List<PatientPageContent> getPatientPageContents(@Param("sampleId") String sampleId);
}
