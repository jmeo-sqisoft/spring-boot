package com.stagora.reports.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.stagora.reports.dto.PatientDetailSearchRequest;
import com.stagora.reports.dto.PatientListSearchRequest;
import com.stagora.reports.model.PatientListItem;
import com.stagora.reports.model.PatientPageContent;
import com.stagora.reports.model.PatientSearch;

@Mapper
public interface PatientReportMapper {
    List<PatientListItem> getPatientList(PatientListSearchRequest request);

    long getPatientListCount(PatientListSearchRequest request);

    PatientSearch getPatientDetail(PatientDetailSearchRequest request);

    List<PatientPageContent> getPatientPageContents(@Param("sampleId") String sampleId);
}
