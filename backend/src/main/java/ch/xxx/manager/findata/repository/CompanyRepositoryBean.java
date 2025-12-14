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

import org.springframework.stereotype.Repository;

import ch.xxx.manager.stocks.entity.CompanyReport;
import ch.xxx.manager.stocks.entity.CompanyReportRepository;

@Repository
public class CompanyRepositoryBean implements CompanyReportRepository {
    private final JpaCompanyReportRepository jpaCompanyReportRepository;

    public CompanyRepositoryBean(JpaCompanyReportRepository jpaCompanyReportRepository) {
        this.jpaCompanyReportRepository = jpaCompanyReportRepository;
    }
    @Override
    public void save(CompanyReport companyReport) {
        this.jpaCompanyReportRepository.save(companyReport);
    }

    @Override
    public Iterable<CompanyReport> findAll() {
        return this.jpaCompanyReportRepository.findAll();
    }

    @Override
    public Iterable<CompanyReport> findByReportUrlIn(Iterable<String> reportUrls) {
        return this.jpaCompanyReportRepository.findByReportUrlIn(reportUrls);
    }

    @Override
    public Iterable<CompanyReport> saveAll(Iterable<CompanyReport> companyReports) {
        return this.jpaCompanyReportRepository.saveAll(companyReports);
    }
}
