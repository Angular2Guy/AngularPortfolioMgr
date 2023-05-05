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
package ch.xxx.manager.domain.model.entity;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import ch.xxx.manager.domain.model.dto.SfCountryDto;
import ch.xxx.manager.domain.model.dto.SfQuarterDto;
import ch.xxx.manager.domain.model.dto.SymbolFinancialsQueryParamsDto;

public interface SymbolFinancialsRepository {
	SymbolFinancials save(SymbolFinancials symbolfinancials);
	List<SymbolFinancials> saveAll(Iterable<SymbolFinancials> symbolfinancials);
    Optional<SymbolFinancials> findById(Long id);
    void deleteAllBatch();
    List<SfQuarterDto> findCommonSfQuarters();
    List<SfCountryDto> findCommonSfCountries();
    List<SymbolFinancials> findSymbolFinancials(SymbolFinancialsQueryParamsDto symbolFinancialsQueryParams);
    List<SymbolFinancials> findByName(String companyName);
    List<SymbolFinancials> findBySymbol(String symbol);
    List<SymbolFinancials> findAllByIdFetchEager(Collection<Long> ids);
}
