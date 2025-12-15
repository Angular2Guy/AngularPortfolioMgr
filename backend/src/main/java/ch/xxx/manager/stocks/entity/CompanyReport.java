/**
 *    Copyright 2019 Sven Loesekann
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
       http://www.apache.org/licenses/LICENSE-2.0
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package ch.xxx.manager.stocks.entity;

import java.time.LocalDateTime;

import ch.xxx.manager.common.entity.EntityBase;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;

@Entity
public class CompanyReport extends EntityBase {
    public enum ReportType {
        ANNUAL("10-K"), QUARTERLY("10-Q");
        private final String reportType;

        ReportType(String reportType) {
            this.reportType = reportType;
        }
        public String getReportType() {
            return reportType;
        }
    }

    @Enumerated(EnumType.STRING)
    private ReportType reportType;
    private LocalDateTime reportDate;
    private String reportUrl;
    @Lob
    private byte[] reportBlob;
    @ManyToOne
    private Symbol symbol;

    public CompanyReport() {
        super();
    }

    public CompanyReport(ReportType reportType, LocalDateTime reportDate, String reportUrl, byte[] reportBlob) {
        super();
        this.reportType = reportType;
        this.reportDate = reportDate;
        this.reportUrl = reportUrl;
        this.reportBlob = reportBlob;
    }    

    public ReportType getReportType() {
        return reportType;
    }
    public void setReportType(ReportType reportType) {
        this.reportType = reportType;
    }
    public LocalDateTime getReportDate() {
        return reportDate;
    }
    public void setReportDate(LocalDateTime reportDate) {
        this.reportDate = reportDate;
    }
    public String getReportUrl() {
        return reportUrl;
    }
    public void setReportUrl(String reportUrl) {
        this.reportUrl = reportUrl;
    }
    public byte[] getReportBlob() {
        return reportBlob;
    }
    public void setReportBlob(byte[] reportBlob) {
        this.reportBlob = reportBlob;
    }
    public Symbol getSymbol() {
        return symbol;
    }

    public void setSymbol(Symbol symbol) {
        this.symbol = symbol;
    }
} 
