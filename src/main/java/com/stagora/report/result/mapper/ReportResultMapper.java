package com.stagora.report.result.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.stagora.report.result.dto.ReportSearchRequest;
import com.stagora.report.result.model.ReportSearch;

@Mapper
public interface ReportResultMapper {
    List<ReportSearch> findReports(ReportSearchRequest request);
}
