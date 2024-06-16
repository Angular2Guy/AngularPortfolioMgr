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
package ch.xxx.manager.usecase.service;

import java.util.Optional;

import ch.xxx.manager.domain.model.dto.AlphaOverviewImportDto;
import ch.xxx.manager.domain.model.dto.DailyFxWrapperImportDto;
import ch.xxx.manager.domain.model.dto.DailyWrapperImportDto;
import ch.xxx.manager.domain.model.dto.IntraDayWrapperImportDto;
import ch.xxx.manager.usecase.service.QuoteImportService.UserKeys;

public interface AlphavatageClient {
	Optional<AlphaOverviewImportDto> importCompanyProfile(String symbol);
	Optional<IntraDayWrapperImportDto> getTimeseriesIntraDay(String symbol, UserKeys userKeys);
	Optional<DailyWrapperImportDto> getTimeseriesDailyHistory(String symbol, boolean fullSeries, UserKeys userKeys);
	Optional<DailyFxWrapperImportDto> getFxTimeseriesDailyHistory(String to_currency, boolean fullSeries);
}
