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
package ch.xxx.manager.stocks.mapping.open;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ch.xxx.manager.stocks.entity.CompanyReport;
import ch.xxx.manager.stocks.dto.CompanyReportWrapper;
import ch.xxx.manager.stocks.entity.dto.RssDto;

@Component
public class NewsFeedMapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(NewsFeedMapper.class);

    public List<CompanyReportWrapper> convert(RssDto rssDto) {  
      var result =  rssDto.channel().item().stream().filter(myItem ->
                      Optional.ofNullable(myItem.xbrlFiling().formType()).stream().anyMatch(value -> value.trim().toUpperCase().contains(CompanyReport.ReportType.ANNUAL.getReportType()))
              || Optional.ofNullable(myItem.xbrlFiling().formType()).stream().anyMatch(value -> value.trim().toUpperCase().contains(CompanyReport.ReportType.QUARTERLY.getReportType()))
      ) .map(myItem -> {
          var period = myItem.xbrlFiling().period();
          LocalDateTime localDateTime = null;
          try {
              var year = Integer.parseInt(period.substring(0, 4));
              var month = Integer.parseInt(period.substring(4, 6));
              var day = Integer.parseInt(period.substring(6, 8));
              localDateTime = LocalDateTime.of(year, month, day, 0, 0);
          }catch (Exception e) {
              LOGGER.info(period, e);
          }
          var companyReport = new CompanyReport();
          companyReport.setReportDate(localDateTime);
          companyReport.setReportType(Optional.ofNullable(myItem.xbrlFiling().formType()).stream().map(NewsFeedMapper::mapReportType).filter(Objects::nonNull).findFirst().orElseThrow());
          myItem.xbrlFiling().xbrlFiles().stream()
          .filter(xbrlFile -> xbrlFile.type().trim().equalsIgnoreCase(myItem.xbrlFiling().formType().trim()))
          .findFirst().ifPresent(xbrlFile -> {
              companyReport.setReportUrl(xbrlFile.url());
          });          
          return new CompanyReportWrapper(myItem.xbrlFiling().cikNumber().trim(), myItem.enclosure().url(), companyReport);
        }).toList();
        return result;
    }

    private static CompanyReport.ReportType mapReportType(String value) {
        CompanyReport.ReportType result = null;
        if(value.trim().toUpperCase().contains(CompanyReport.ReportType.QUARTERLY.getReportType())) {
            result = CompanyReport.ReportType.QUARTERLY;
        } else if(value.trim().toUpperCase().contains(CompanyReport.ReportType.ANNUAL.getReportType())) {
            result = CompanyReport.ReportType.ANNUAL;
        }
        return result;
    }
}