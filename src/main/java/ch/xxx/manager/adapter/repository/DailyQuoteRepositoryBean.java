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
package ch.xxx.manager.adapter.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Repository;

import ch.xxx.manager.domain.model.entity.DailyQuote;
import ch.xxx.manager.domain.model.entity.DailyQuoteRepository;

@Repository
public class DailyQuoteRepositoryBean implements DailyQuoteRepository {
	private final JpaDailyQuoteRepository jpaDailyQuoteRepository;
	
	public DailyQuoteRepositoryBean(JpaDailyQuoteRepository jpaDailyQuoteRepository) {
		this.jpaDailyQuoteRepository = jpaDailyQuoteRepository;
	}

	@Override
	public List<DailyQuote> findBySymbol(String symbol) {
		return this.jpaDailyQuoteRepository.findBySymbol(symbol);
	}

	@Override
	public List<DailyQuote> findBySymbolId(Long symbolId) {
		return this.jpaDailyQuoteRepository.findBySymbolId(symbolId);
	}

	@Override
	public List<DailyQuote> findBySymbolAndDayBetween(String symbol, LocalDate start, LocalDate end) {
		return this.jpaDailyQuoteRepository.findBySymbolAndDayBetween(symbol, start, end);
	}

	@Override
	public List<DailyQuote> saveAll(List<DailyQuote> dailyquotes) {
		return this.jpaDailyQuoteRepository.saveAll(dailyquotes);
	}
}
