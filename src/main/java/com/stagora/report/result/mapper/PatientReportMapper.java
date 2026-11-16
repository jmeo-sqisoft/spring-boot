package com.stagora.report.result.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.stagora.report.result.dto.PatientDetailSearchRequest;
import com.stagora.report.result.dto.PatientListSearchRequest;
import com.stagora.report.result.model.PatientPageContent;
import com.stagora.report.result.model.PatientSearch;

@Mapper
public interface PatientReportMapper {
    List<PatientSearch> getPatientList(PatientListSearchRequest request);

    PatientSearch getPatientDetail(PatientDetailSearchRequest request);

    List<PatientPageContent> getPatientPageContents(@Param("sampleId") String sampleId);
}
