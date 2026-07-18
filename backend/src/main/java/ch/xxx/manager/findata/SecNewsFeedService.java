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
package ch.xxx.manager.findata;

import ch.xxx.manager.findata.repository.JpaCompanyReportRepository;
import ch.xxx.manager.stocks.NewsFeedClient;
import ch.xxx.manager.stocks.SymbolService;
import ch.xxx.manager.stocks.dto.CompanyReportWrapper;
import ch.xxx.manager.stocks.entity.CompanyReport;
import ch.xxx.manager.stocks.entity.Symbol;
import jakarta.transaction.Transactional;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.StreamSupport;

@Service
public class SecNewsFeedService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SecNewsFeedService.class);
    private final JpaCompanyReportRepository companyReportRepository;
    private final NewsFeedClient newsFeedClient;
    private final SymbolService symbolService;

    public SecNewsFeedService(JpaCompanyReportRepository companyReportRepository, NewsFeedClient newsFeedClient, SymbolService symbolService) {
        this.companyReportRepository = companyReportRepository;
        this.newsFeedClient = newsFeedClient;
        this.symbolService = symbolService;
    }

    @Transactional
    public void updateSecEdgarUsGaapNewsFeedSync() {
        this.updateSecEdgarUsGaapNewsFeed();
    }

    private void updateSecEdgarUsGaapNewsFeed() {
        var start = Instant.now();
        var cikToCompanyReport = this.newsFeedClient.importSecEdgarUsGaapNewsFeed();
        var ciks = cikToCompanyReport.stream().map(CompanyReportWrapper::cik).toList();
        var symbols = this.symbolService.findByCikIn(ciks);

        final var companyReports = cikToCompanyReport.stream().filter(item -> CompanyReport.ReportType.ANNUAL.equals(item.companyReport().getReportType())
                        || CompanyReport.ReportType.QUARTERLY.equals(item.companyReport().getReportType())).map(entry -> this.getCompanyReport(entry, symbols))
                .filter(myCompanyReport -> Optional.ofNullable(myCompanyReport.getSymbol()).stream().anyMatch(Objects::nonNull)).toList();

        var companyReportsFiltered = companyReports.stream().filter(myCompanyReport
                -> StreamSupport.stream(this.companyReportRepository.findByReportUrlIn(companyReports.stream().map(CompanyReport::getReportUrl).toList()).spliterator(), false)
                .map(CompanyReport::getReportUrl).noneMatch(myCompanyReport.getReportUrl()::equals)).toList();

        companyReportsFiltered = StreamSupport.stream(this.companyReportRepository.saveAll(companyReportsFiltered).spliterator(), false).toList();

        LOGGER.info("Sec Company Reports imported: {} in {}ms", companyReportsFiltered.size(), Instant.now().toEpochMilli() - start.toEpochMilli());
    }

    private @NonNull CompanyReport getCompanyReport(CompanyReportWrapper entry, Collection<Symbol> symbols) {
        var symbol = symbols.stream().filter(mySymbol -> mySymbol.getCik().equals(entry.cik())).findFirst().orElse(null);
        var companyReport = entry.companyReport();
        companyReport.setSymbol(symbol);
        companyReport.setReportBlob(this.newsFeedClient.loadCompanyReportZip(entry.reportZipUrl()));
        return companyReport;
    }
}
