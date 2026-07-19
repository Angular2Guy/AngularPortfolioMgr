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
package ch.xxx.manager.findata.repository;


import ch.xxx.manager.stocks.entity.CompanyReport;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Collection;

public interface JpaCompanyReportRepository extends PagingAndSortingRepository<CompanyReport, Long>, CrudRepository<CompanyReport, Long> {
  Iterable<CompanyReport> findByReportUrlIn(Collection<String> reportUrl);
  Collection<CompanyReport> findBy(Sort sort, Limit limit);
}
