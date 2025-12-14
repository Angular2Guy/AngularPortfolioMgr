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
package ch.xxx.manager.stocks.service;
import static org.mockito.ArgumentMatchers.any;

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import ch.xxx.manager.common.exception.ResourceNotFoundException;
import ch.xxx.manager.common.entity.AppUserRepository;
import ch.xxx.manager.stocks.entity.DailyQuoteRepository;
import ch.xxx.manager.stocks.entity.Portfolio;
import ch.xxx.manager.stocks.entity.PortfolioElement;
import ch.xxx.manager.stocks.entity.PortfolioElementRepository;
import ch.xxx.manager.stocks.entity.PortfolioRepository;
import ch.xxx.manager.stocks.entity.PortfolioToSymbolRepository;
import ch.xxx.manager.stocks.entity.SymbolRepository;
import ch.xxx.manager.stocks.entity.dto.PortfolioWithElements;

@ExtendWith(MockitoExtension.class)
public class PortfolioServiceTest {
	@Mock
	private PortfolioRepository portfolioRepository;
	@Mock
	private PortfolioElementRepository portfolioElementRepository;
	@Mock
	private PortfolioToSymbolRepository portfolioToSymbolRepository;
	@Mock
	private SymbolRepository symbolRepository;
	@Mock
	private AppUserRepository appUserRepository;
	@Mock
	private PortfolioCalculationService portfolioCalculationService;
	@Mock
	private PortfolioToIndexService portfolioToIndexService;
	@Mock
	private DailyQuoteRepository dailyQuoteRepository;
	@InjectMocks
	private PortfolioService portfolioService;

	@Test
	public void getPortfolioByIdFound() throws Exception {
		Portfolio myPortfolio = this.createPortfolioEntity();
		PortfolioElement myPortfolioElement = new PortfolioElement();
		Mockito.when(this.portfolioRepository.findById(any(Long.class))).thenReturn(Optional.of(myPortfolio));
		Mockito.when(this.portfolioElementRepository.findByPortfolioId(any(Long.class))).thenReturn(Optional.of(myPortfolioElement));
		PortfolioWithElements result = this.portfolioService.getPortfolioById(1L);
		Assertions.assertNotNull(result);
		Assertions.assertEquals(myPortfolio.getId(), result.portfolio().getId());
		Assertions.assertEquals(myPortfolio.getName(), result.portfolio().getName());
	}
	
	@Test
	public void getPortfolioByIdNotFound() throws Exception {
		Mockito.when(this.portfolioRepository.findById(any(Long.class))).thenReturn(Optional.empty());
		Assertions.assertThrows(ResourceNotFoundException.class, () -> this.portfolioService.getPortfolioById(1L));		
	}
	
	private Portfolio createPortfolioEntity() {
		Portfolio myPortfolio = new Portfolio();
		myPortfolio.setId(1L);
		myPortfolio.setName("XXX");
		return myPortfolio;
	}
}
