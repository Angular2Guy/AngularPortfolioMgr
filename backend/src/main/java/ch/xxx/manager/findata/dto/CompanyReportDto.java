package ch.xxx.manager.findata.dto;

import ch.xxx.manager.stocks.entity.CompanyReport;

import java.time.LocalDateTime;

public record CompanyReportDto(CompanyReport.ReportType reportType, LocalDateTime reportDate, String reportUrl,
                               byte[] reportBlob, String symbolSymbol, String symbolName) {
}
