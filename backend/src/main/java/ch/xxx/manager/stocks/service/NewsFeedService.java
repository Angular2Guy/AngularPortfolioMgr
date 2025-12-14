/**
 *    Copyright 2019 Sven Loesekann
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.xxx.manager.stocks.service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Gatherers;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;

import ch.xxx.manager.stocks.dto.SymbolToCikWrapperDto;
import ch.xxx.manager.stocks.entity.CompanyReport;
import ch.xxx.manager.stocks.entity.CompanyReportRepository;
import ch.xxx.manager.stocks.entity.SymbolRepository;
import ch.xxx.manager.stocks.entity.dto.CompanyReportWrapper;
import jakarta.transaction.Transactional;

@Service
public class NewsFeedService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NewsFeedService.class);

    private final NewsFeedClient newsFeedClient;
    private final SecSymbolClient secSymbolClient;
    private final SymbolRepository symbolRepository;
    private final CompanyReportRepository companyReportRepository;
    private volatile Optional<SyndFeed> seekingAlphaNewsFeedOptional = Optional.empty();
    private volatile Optional<SyndFeed> cnbcFinanceNewsFeedOptional = Optional.empty();

    public NewsFeedService(NewsFeedClient newsFeedClient, SecSymbolClient secSymbolClient, SymbolRepository symbolRepository, CompanyReportRepository companyReportRepository) {
        this.newsFeedClient = newsFeedClient;
        this.secSymbolClient = secSymbolClient;
        this.symbolRepository = symbolRepository;
        this.companyReportRepository = companyReportRepository;
    }

    @Async
    public void updateSeekingAlphaNewsFeed() {
        var start = Instant.now();
        this.seekingAlphaNewsFeedOptional = Optional.ofNullable(this.newsFeedClient.importSeekingAlphaFeed());
        LOGGER.info("Seeking Alpha news imported in: {}ms", Instant.now().toEpochMilli() - start.toEpochMilli());
    }

    @Async
    public void updateCnbcFinanceNewsFeed() {
        var start = Instant.now();
        this.cnbcFinanceNewsFeedOptional = Optional.ofNullable(this.newsFeedClient.importCnbcFinanceNewsFeed());
        LOGGER.info("Cnbc news imported in: {}ms", Instant.now().toEpochMilli() - start.toEpochMilli());
    }

	@Async
    @Transactional
	public void importCompanyReports() {
		this.updateCompanyToSymbolJson();
		//this.updateSecEdgarUsGaapNewsFeed();
	}
    
    private void updateSecEdgarUsGaapNewsFeed() {
        var start = Instant.now();
        var cikToCompanyReport = this.newsFeedClient.importSecEdgarUsGaapNewsFeed();
        var ciks = cikToCompanyReport.stream().map(CompanyReportWrapper::cik).toList();
        var symbols = this.symbolRepository.findByCikIn(ciks);

        final var companyReports = cikToCompanyReport.stream().filter(item -> CompanyReport.ReportType.ANNUAL.equals(item.companyReport().getReportType())
                || CompanyReport.ReportType.QUARTERLY.equals(item.companyReport().getReportType())).map(entry -> {
            var symbol = symbols.stream().filter(mySymbol -> mySymbol.getCik().equals(entry.cik())).findFirst().orElse(null);
            var companyReport = entry.companyReport();
            companyReport.setSymbol(symbol);
            companyReport.setReportBlob(this.newsFeedClient.loadCompanyReportZip(entry.reportZipUrl()));
            return companyReport;
        }).filter(myCompanyReport -> myCompanyReport.getSymbol() != null).toList();

        var companyReportsFiltered = companyReports.stream().filter(myCompanyReport
                -> StreamSupport.stream(this.companyReportRepository.findByReportUrlIn(companyReports.stream().map(CompanyReport::getReportUrl).toList()).spliterator(), false)
                        .map(CompanyReport::getReportUrl).noneMatch(myCompanyReport.getReportUrl()::equals)).toList();

        companyReportsFiltered = StreamSupport.stream(this.companyReportRepository.saveAll(companyReportsFiltered).spliterator(), false).toList();

        LOGGER.info("Sec Company Reports imported: {} in {}ms", companyReportsFiltered.size(), Instant.now().toEpochMilli() - start.toEpochMilli());
    }
    
    private void updateCompanyToSymbolJson() {
        var start = Instant.now();
        /*
        var json = this.secSymbolClient.importSymbolJson();
        LOGGER.info(json.substring(0, 10000));
        */        
        final var cikToCompanyReport = this.secSymbolClient.importSymbols();
        final var companyReportsList = cikToCompanyReport.entrySet().stream().map(Map.Entry::getValue).toList();
        final var companyReports = companyReportsList.stream().gather(Gatherers.windowFixed(999)).toList();
        final var symbols = companyReports.stream()
                .flatMap(myCompanyReports -> this.symbolRepository.findBySymbolIn(myCompanyReports.stream().map(SymbolToCikWrapperDto.CompanySymbolDto::getTicker).toList()).stream())
                .map(mySymbol -> {
                    mySymbol.setCik(companyReportsList.stream().filter(myCompanyReport -> myCompanyReport.getTicker().equals(mySymbol.getSymbol()))
					  .map(SymbolToCikWrapperDto.CompanySymbolDto::getCik_str).map(String::valueOf).findFirst().orElse(null));
                    return mySymbol;
                }).toList();

        var result = this.symbolRepository.saveAll(symbols);
        LOGGER.info("Sec Company to Symbol Json: {} imported in: {}ms", result.size(), Instant.now().toEpochMilli() - start.toEpochMilli());        
    }

    public List<SyndEntry> getSeekingAlphaNewsFeed() {
        return this.seekingAlphaNewsFeedOptional.stream().flatMap(myFeed -> myFeed.getEntries().stream()).map(myEntry -> {
            myEntry.getForeignMarkup().clear();
            return myEntry;
        }).toList();
    }

    public List<SyndEntry> getCnbcFinanceNewsFeed() {
        return this.cnbcFinanceNewsFeedOptional.stream().flatMap(myFeed -> myFeed.getEntries().stream()).map(myEntry -> {
            myEntry.getForeignMarkup().clear();
            return myEntry;
        }).toList();
    }
}
