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

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Repository;

import ch.xxx.manager.domain.model.entity.IntraDayQuote;
import ch.xxx.manager.domain.model.entity.IntraDayQuoteRepository;

@Repository
public class IntraDayQuoteRepositoryBean implements IntraDayQuoteRepository{
	private final JpaIntraDayQuoteRepository jpaIntraDayQuoteRepository;
	
	public IntraDayQuoteRepositoryBean(JpaIntraDayQuoteRepository jpaIntraDayQuoteRepository) {
		this.jpaIntraDayQuoteRepository = jpaIntraDayQuoteRepository;
	}

	@Override
	public List<IntraDayQuote> findBySymbol(String symbol) {
		return this.jpaIntraDayQuoteRepository.findBySymbol(symbol);
	}

	@Override
	public List<IntraDayQuote> findBySymbolId(Long symbolId) {
		return this.jpaIntraDayQuoteRepository.findBySymbolId(symbolId);
	}

	@Override
	public List<IntraDayQuote> findBySymbolAndLocaldatetimeBetween(String symbol, LocalDateTime start,
			LocalDateTime end) {
		return this.jpaIntraDayQuoteRepository.findBySymbolAndLocaldatetimeBetween(symbol, start, end);
	}
	
	public void deleteAll(Collection<IntraDayQuote> intraDayQuotes) {
		this.jpaIntraDayQuoteRepository.deleteAll(intraDayQuotes);
	}

	@Override
	public List<IntraDayQuote> saveAll(Collection<IntraDayQuote> intraDayQuotes) {
		return this.jpaIntraDayQuoteRepository.saveAll(intraDayQuotes);
	}
}
