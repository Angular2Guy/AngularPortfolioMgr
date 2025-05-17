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
package ch.xxx.manager.usecase.mapping;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Component;

import ch.xxx.manager.domain.model.dto.RssDto;
import ch.xxx.manager.domain.model.entity.CompanyReport;
import ch.xxx.manager.domain.model.entity.dto.CompanyReportWrapper;

@Component
public class NewsFeedMapper {

    public List<CompanyReportWrapper> convert(RssDto rssDto) {  
      var result =  rssDto.channel().item().stream().filter(myItem -> 
         CompanyReport.ReportType.ANNUAL.toString().equalsIgnoreCase(myItem.xbrlFiling().formType().trim()) 
         || CompanyReport.ReportType.QUARTERLY.toString().equalsIgnoreCase(myItem.xbrlFiling().formType().trim())
      ) .map(myItem -> {
          var period = myItem.xbrlFiling().period();
          var year = Long.getLong(period.substring(0, 3));
          var month = Long.getLong(period.substring(4, 5));
          var day = Long.getLong(period.substring(6, 7));
          var localDateTime = LocalDateTime.of(year.intValue(), month.intValue(), day.intValue(), 0, 0);
          var companyReport = new CompanyReport();
          companyReport.setReportDate(localDateTime);
          companyReport.setReportType(CompanyReport.ReportType.valueOf(myItem.xbrlFiling().formType().trim()));
          myItem.xbrlFiling().xbrlFiles().stream()
          .filter(xbrlFile -> xbrlFile.type().trim().equalsIgnoreCase(myItem.xbrlFiling().formType().trim()))
          .findFirst().ifPresent(xbrlFile -> {
              companyReport.setReportUrl(xbrlFile.url());
          });          
          return new CompanyReportWrapper(myItem.xbrlFiling().cikNumber().trim(), myItem.enclosure().url(), companyReport);
        }).toList();
        return result;
    }
}