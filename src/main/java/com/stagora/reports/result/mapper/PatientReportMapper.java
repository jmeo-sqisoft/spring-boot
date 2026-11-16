package com.stagora.reports.result.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.stagora.reports.result.dto.PatientDetailSearchRequest;
import com.stagora.reports.result.dto.PatientListSearchRequest;
import com.stagora.reports.result.model.PatientPageContent;
import com.stagora.reports.result.model.PatientSearch;

@Mapper
public interface PatientReportMapper {
    List<PatientSearch> getPatientList(PatientListSearchRequest request);

    PatientSearch getPatientDetail(PatientDetailSearchRequest request);

    List<PatientPageContent> getPatientPageContents(@Param("sampleId") String sampleId);
}
