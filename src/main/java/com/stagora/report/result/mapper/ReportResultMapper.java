package com.stagora.report.result.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.stagora.report.result.dto.ReportSearchRequest;
import com.stagora.report.result.model.ReportPageContent;
import com.stagora.report.result.model.ReportSearch;

@Mapper
public interface ReportResultMapper {
    List<ReportSearch> selectReports(ReportSearchRequest request);

    ReportSearch selectReport(@Param("sampleId") String sampleId);

    List<ReportPageContent> selectPageContents(@Param("sampleId") String sampleId);
}
