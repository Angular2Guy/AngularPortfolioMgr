/**
 *    Copyright 2018 Sven Loesekann
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ch.xxx.manager.stocks.entity.DailyQuoteRepository;
import ch.xxx.manager.stocks.service.PortfolioStatisticService.CalcValuesDay;
import ch.xxx.manager.stocks.service.PortfolioStatisticService.LinearRegressionResults;

@ExtendWith(MockitoExtension.class)
public class PortfolioStatisticServiceTest {
	@Mock
	DailyQuoteRepository dailyQuoteRepository;
	@Mock
    CurrencyService currencyService;

	@InjectMocks
    PortfolioStatisticService service;

	private List<CalcValuesDay> calcValuesDays = new ArrayList<>();

	@BeforeEach
	public void createTestData() {
		this.calcValuesDays
				.add(new CalcValuesDay(LocalDate.now().minusDays(5L), BigDecimal.valueOf(1L), BigDecimal.valueOf(2L)));
		this.calcValuesDays
				.add(new CalcValuesDay(LocalDate.now().minusDays(4L), BigDecimal.valueOf(3L), BigDecimal.valueOf(4L)));
		this.calcValuesDays
				.add(new CalcValuesDay(LocalDate.now().minusDays(3L), BigDecimal.valueOf(5L), BigDecimal.valueOf(6L)));
		this.calcValuesDays
				.add(new CalcValuesDay(LocalDate.now().minusDays(2L), BigDecimal.valueOf(7L), BigDecimal.valueOf(8L)));
		this.calcValuesDays
				.add(new CalcValuesDay(LocalDate.now().minusDays(1L), BigDecimal.valueOf(9L), BigDecimal.valueOf(10L)));
		this.calcValuesDays.add(new CalcValuesDay(LocalDate.now(), BigDecimal.valueOf(11L), BigDecimal.valueOf(12L)));
	}

	@Test
	public void calcCorrelationValues() {
		Double correlation = this.service.calculateCorrelation(this.calcValuesDays);
		Assertions.assertEquals(BigDecimal.valueOf(correlation).setScale(25, RoundingMode.HALF_EVEN),
				BigDecimal.valueOf(1.0D).setScale(25, RoundingMode.HALF_EVEN));
	}

	@Test
	public void calcLinRegReturnValues() {
		LinearRegressionResults calcLinRegReturn = this.service.calcLinRegReturn(this.calcValuesDays);
		Assertions.assertNotNull(calcLinRegReturn);
		Assertions.assertEquals(BigDecimal.valueOf(-1L).setScale(25, RoundingMode.HALF_EVEN),
				calcLinRegReturn.adderComp().setScale(25, RoundingMode.HALF_EVEN));
		Assertions.assertEquals(BigDecimal.valueOf(-0.5D).setScale(25, RoundingMode.HALF_EVEN),
				calcLinRegReturn.adderDaily().setScale(25, RoundingMode.HALF_EVEN));
		Assertions.assertEquals(BigDecimal.valueOf(0.5D).setScale(25, RoundingMode.HALF_EVEN),
				calcLinRegReturn.multiplierDaily().setScale(25, RoundingMode.HALF_EVEN));
		Assertions.assertEquals(BigDecimal.valueOf(0.5D).setScale(25, RoundingMode.HALF_EVEN),
				calcLinRegReturn.multiplierComp().setScale(25, RoundingMode.HALF_EVEN));
	}

	@Test
	public void calcCorrelationEmpty() {
		Double correlation = this.service.calculateCorrelation(List.of());
		Assertions.assertEquals(BigDecimal.valueOf(correlation).setScale(25, RoundingMode.HALF_EVEN),
				BigDecimal.ZERO.setScale(25, RoundingMode.HALF_EVEN));
	}

	@Test
	public void calcLinRegReturnEmpty() {
		LinearRegressionResults calcLinRegReturn = this.service.calcLinRegReturn(List.of());
		Assertions.assertNotNull(calcLinRegReturn);
		Assertions.assertEquals(BigDecimal.ZERO.setScale(25, RoundingMode.HALF_EVEN),
				calcLinRegReturn.adderComp().setScale(25, RoundingMode.HALF_EVEN));
		Assertions.assertEquals(BigDecimal.ZERO.setScale(25, RoundingMode.HALF_EVEN),
				calcLinRegReturn.adderDaily().setScale(25, RoundingMode.HALF_EVEN));
		Assertions.assertEquals(BigDecimal.ZERO.setScale(25, RoundingMode.HALF_EVEN),
				calcLinRegReturn.multiplierComp().setScale(25, RoundingMode.HALF_EVEN));
		Assertions.assertEquals(BigDecimal.ZERO.setScale(25, RoundingMode.HALF_EVEN),
				calcLinRegReturn.multiplierDaily().setScale(25, RoundingMode.HALF_EVEN));
	}

	@Test
	public void calcCorrelationOne() {
		Double correlation = this.service.calculateCorrelation(List.of(this.calcValuesDays.get(0)));
		Assertions.assertEquals(BigDecimal.valueOf(correlation).setScale(25, RoundingMode.HALF_EVEN),
				BigDecimal.ZERO.setScale(25, RoundingMode.HALF_EVEN));
	}
	
	@Test
	public void calcLinRegReturnOne() {
		LinearRegressionResults calcLinRegReturn = this.service.calcLinRegReturn(List.of(this.calcValuesDays.get(0)));
		Assertions.assertNotNull(calcLinRegReturn);
		Assertions.assertEquals(BigDecimal.ZERO.setScale(25, RoundingMode.HALF_EVEN),
				calcLinRegReturn.adderComp().setScale(25, RoundingMode.HALF_EVEN));
		Assertions.assertEquals(BigDecimal.ZERO.setScale(25, RoundingMode.HALF_EVEN),
				calcLinRegReturn.adderDaily().setScale(25, RoundingMode.HALF_EVEN));
		Assertions.assertEquals(BigDecimal.ZERO.setScale(25, RoundingMode.HALF_EVEN),
				calcLinRegReturn.multiplierComp().setScale(25, RoundingMode.HALF_EVEN));
		Assertions.assertEquals(BigDecimal.ZERO.setScale(25, RoundingMode.HALF_EVEN),
				calcLinRegReturn.multiplierDaily().setScale(25, RoundingMode.HALF_EVEN));
	}
}
