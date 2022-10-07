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

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ch.xxx.manager.domain.model.entity.Currency;
import ch.xxx.manager.domain.model.entity.DailyQuote;
import ch.xxx.manager.domain.model.entity.DailyQuoteRepository;
import ch.xxx.manager.domain.model.entity.Portfolio;
import ch.xxx.manager.domain.model.entity.PortfolioBase;
import ch.xxx.manager.domain.model.entity.PortfolioElement;
import ch.xxx.manager.domain.model.entity.PortfolioToSymbol;
import ch.xxx.manager.domain.model.entity.Symbol;
import ch.xxx.manager.domain.model.entity.SymbolRepository;
import ch.xxx.manager.domain.model.entity.dto.PortfolioWithElements;
import ch.xxx.manager.domain.utils.CurrencyKey;
import ch.xxx.manager.domain.utils.StreamHelpers;
import ch.xxx.manager.usecase.mapping.MappingUtils;

@Service
@Transactional
public class PortfolioStatisticService extends PortfolioCalculcationBase {
	private static final Logger LOGGER = LoggerFactory.getLogger(PortfolioStatisticService.class);
	private final SymbolRepository symbolRepository;

	private record BigDecimalValues(BigDecimal daily, BigDecimal comp) {
	}

	record LinearRegressionResults(BigDecimal multiplierDaily, BigDecimal adderDaily, BigDecimal multiplierComp,
			BigDecimal adderComp) {
	}

	record CalcValuesDay(LocalDate day, BigDecimal quote, BigDecimal compQuote) {
	}

	public PortfolioStatisticService(SymbolRepository symbolRepository, CurrencyService currencyService,
			DailyQuoteRepository dailyQuoteRepository) {
		super(dailyQuoteRepository, currencyService);
		this.symbolRepository = symbolRepository;
	}

	public PortfolioWithElements calculatePortfolioWithElements(final Portfolio portfolio,
			List<DailyQuote> portfolioQuotes) {
		List<PortfolioToSymbol> portfolioToSymbols = portfolio.getPortfolioToSymbols().stream().toList();
		Set<PortfolioToSymbol> portfolioToSymbolsNoPort = portfolioToSymbols.stream()
				.filter(pts -> !pts.getSymbol().getSymbol().contains(ServiceUtils.PORTFOLIO_MARKER))
				.collect(Collectors.toSet());
		Map<String, List<DailyQuote>> dailyQuotesMap = this.createDailyQuotesKeyMap(portfolioToSymbolsNoPort);
		List<Symbol> comparisionSymbols = StreamHelpers.toStream(ComparisonIndex.values()).toList().stream()
				.map(ComparisonIndex::getSymbol)
				.flatMap(mySymbolStr -> this.symbolRepository.findBySymbolSingle(mySymbolStr).stream())
//				.filter(StreamHelpers.distinctByKey(mySymbol -> mySymbol.getSymbol()))
				.toList();
		List<PortfolioElement> portfolioElements = portfolioToSymbols.stream()
				.filter(pts -> !pts.getSymbol().getSymbol().contains(ServiceUtils.PORTFOLIO_MARKER))
				.filter(pts -> pts.getRemovedAt() == null)
				.flatMap(pts -> Stream.of(this.createPortfolioElement(portfolio,
						dailyQuotesMap.get(pts.getSymbol().getSymbol()), pts, comparisionSymbols)))
				.collect(Collectors.toList());
		updateCorrelations(portfolio, portfolio, comparisionSymbols, portfolioQuotes);
		updateLinRegReturns(portfolio, portfolio, comparisionSymbols, portfolioQuotes);
		PortfolioWithElements result = new PortfolioWithElements(portfolio, portfolioElements, List.of(),
				portfolioQuotes);
		return result;
	}

	private void updateLinRegReturns(final Portfolio portfolio, final PortfolioBase portfolioBase,
			Collection<Symbol> comparisonSymbols, List<DailyQuote> portfolioQuotes) {
		final Symbol es50Symbol = this.filterSymbol(ComparisonIndex.EUROSTOXX50, comparisonSymbols);
		final Symbol msciChinaSymbol = this.filterSymbol(ComparisonIndex.MSCI_CHINA, comparisonSymbols);
		final Symbol sp500Symbol = this.filterSymbol(ComparisonIndex.SP500, comparisonSymbols);
		portfolioBase.setYear10LinRegReturnEuroStoxx50(this.calcLinRegReturn(portfolio, LocalDate.now().minusYears(10L),
				portfolioQuotes, es50Symbol.getDailyQuotes()));
		portfolioBase.setYear10LinRegReturnMsciChina(this.calcLinRegReturn(portfolio, LocalDate.now().minusYears(10L),
				portfolioQuotes, msciChinaSymbol.getDailyQuotes()));
		portfolioBase.setYear10LinRegReturnSp500(this.calcLinRegReturn(portfolio, LocalDate.now().minusYears(10L),
				portfolioQuotes, sp500Symbol.getDailyQuotes()));
		portfolioBase.setYear5LinRegReturnEuroStoxx50(this.calcLinRegReturn(portfolio, LocalDate.now().minusYears(5L),
				portfolioQuotes, es50Symbol.getDailyQuotes()));
		portfolioBase.setYear5LinRegReturnMsciChina(this.calcLinRegReturn(portfolio, LocalDate.now().minusYears(5L),
				portfolioQuotes, msciChinaSymbol.getDailyQuotes()));
		portfolioBase.setYear5LinRegReturnSp500(this.calcLinRegReturn(portfolio, LocalDate.now().minusYears(5L),
				portfolioQuotes, msciChinaSymbol.getDailyQuotes()));
		portfolioBase.setYear2LinRegReturnEuroStoxx50(this.calcLinRegReturn(portfolio, LocalDate.now().minusYears(2L),
				portfolioQuotes, es50Symbol.getDailyQuotes()));
		portfolioBase.setYear2LinRegReturnMsciChina(this.calcLinRegReturn(portfolio, LocalDate.now().minusYears(2L),
				portfolioQuotes, msciChinaSymbol.getDailyQuotes()));
		portfolioBase.setYear2LinRegReturnSp500(this.calcLinRegReturn(portfolio, LocalDate.now().minusYears(2L),
				portfolioQuotes, msciChinaSymbol.getDailyQuotes()));
		portfolioBase.setYear1LinRegReturnEuroStoxx50(this.calcLinRegReturn(portfolio, LocalDate.now().minusYears(1L),
				portfolioQuotes, es50Symbol.getDailyQuotes()));
		portfolioBase.setYear1LinRegReturnMsciChina(this.calcLinRegReturn(portfolio, LocalDate.now().minusYears(1L),
				portfolioQuotes, msciChinaSymbol.getDailyQuotes()));
		portfolioBase.setYear1LinRegReturnSp500(this.calcLinRegReturn(portfolio, LocalDate.now().minusYears(1L),
				portfolioQuotes, msciChinaSymbol.getDailyQuotes()));
	}

	private Symbol filterSymbol(ComparisonIndex comparisionIndex, Collection<Symbol> comparisonSymbols) {
		return comparisonSymbols.stream()
				.filter(mySymbol -> comparisionIndex.getSymbol().equalsIgnoreCase(mySymbol.getSymbol())).findFirst()
				.orElseThrow(() -> new RuntimeException(
						String.format("Comparison Index %s not found.", comparisionIndex.getName())));
	}

	private void updateCorrelations(final Portfolio portfolio, final PortfolioBase portfolioBase,
			List<Symbol> comparisonSymbols, List<DailyQuote> portfolioQuotes) {
		final Symbol es50Symbol = this.filterSymbol(ComparisonIndex.EUROSTOXX50, comparisonSymbols);
		final Symbol msciChinaSymbol = this.filterSymbol(ComparisonIndex.MSCI_CHINA, comparisonSymbols);
		final Symbol sp500Symbol = this.filterSymbol(ComparisonIndex.SP500, comparisonSymbols);
		portfolioBase.setYear10CorrelationEuroStoxx50(this.calcCorrelation(portfolio, LocalDate.now().minusYears(10L),
				portfolioQuotes, es50Symbol.getDailyQuotes()));
		portfolioBase.setYear10CorrelationMsciChina(this.calcCorrelation(portfolio, LocalDate.now().minusYears(10L),
				portfolioQuotes, msciChinaSymbol.getDailyQuotes()));
		portfolioBase.setYear10CorrelationSp500(this.calcCorrelation(portfolio, LocalDate.now().minusYears(10L),
				portfolioQuotes, sp500Symbol.getDailyQuotes()));
		portfolioBase.setYear5CorrelationEuroStoxx50(this.calcCorrelation(portfolio, LocalDate.now().minusYears(5L),
				portfolioQuotes, es50Symbol.getDailyQuotes()));
		portfolioBase.setYear5CorrelationSp500(this.calcCorrelation(portfolio, LocalDate.now().minusYears(5L),
				portfolioQuotes, sp500Symbol.getDailyQuotes()));
		portfolioBase.setYear5CorrelationMsciChina(this.calcCorrelation(portfolio, LocalDate.now().minusYears(5L),
				portfolioQuotes, msciChinaSymbol.getDailyQuotes()));
		portfolioBase.setYear2CorrelationEuroStoxx50(this.calcCorrelation(portfolio, LocalDate.now().minusYears(2L),
				portfolioQuotes, es50Symbol.getDailyQuotes()));
		portfolioBase.setYear2CorrelationSp500(this.calcCorrelation(portfolio, LocalDate.now().minusYears(2L),
				portfolioQuotes, sp500Symbol.getDailyQuotes()));
		portfolioBase.setYear2CorrelationMsciChina(this.calcCorrelation(portfolio, LocalDate.now().minusYears(2L),
				portfolioQuotes, msciChinaSymbol.getDailyQuotes()));
		portfolioBase.setYear1CorrelationEuroStoxx50(this.calcCorrelation(portfolio, LocalDate.now().minusYears(1L),
				portfolioQuotes, es50Symbol.getDailyQuotes()));
		portfolioBase.setYear1CorrelationSp500(this.calcCorrelation(portfolio, LocalDate.now().minusYears(1L),
				portfolioQuotes, sp500Symbol.getDailyQuotes()));
		portfolioBase.setYear1CorrelationMsciChina(this.calcCorrelation(portfolio, LocalDate.now().minusYears(1L),
				portfolioQuotes, msciChinaSymbol.getDailyQuotes()));
	}

	private PortfolioElement createPortfolioElement(final Portfolio portfolio, final List<DailyQuote> dailyQuotes,
			final PortfolioToSymbol portfolioToSymbol, List<Symbol> comparisonSymbols) {
		PortfolioElement portfolioElement = portfolio.getPortfolioElements().stream()
				.filter(myPortfolioElement -> !myPortfolioElement.getSymbol().contains(ServiceUtils.PORTFOLIO_MARKER))
				.filter(myPortfolioElement -> myPortfolioElement.getSymbol()
						.equalsIgnoreCase(portfolioToSymbol.getSymbol().getSymbol()))
				.findFirst().orElse(new PortfolioElement());
		portfolioElement.setSymbol(portfolioToSymbol.getSymbol().getSymbol());
		String ptsName = Optional.ofNullable(portfolioToSymbol.getSymbol().getName()).stream().findFirst()
				.orElse("Unknown");
		Optional<CurrencyKey> symbolCurKeyOpt = Optional.ofNullable(portfolioToSymbol.getSymbol().getCurrencyKey())
				.stream().findFirst();
		String sectorName = Optional.ofNullable(portfolioToSymbol.getSymbol().getSectorStr()).stream().findFirst()
				.orElse("Unknown");
		portfolioElement.setSector(sectorName);
		portfolioElement.setWeight(Optional.ofNullable(portfolioToSymbol.getWeight()).stream().findFirst().orElse(0L));
		portfolioElement.setName(ptsName);
		portfolioElement.setCurrencyKey(portfolio.getCurrencyKey());
		portfolioElement.setLastClose(this.symbolValueAtDate(portfolio, dailyQuotes, LocalDate.now(), symbolCurKeyOpt));
		portfolioElement.setMonth1(
				this.symbolValueAtDate(portfolio, dailyQuotes, LocalDate.now().minusMonths(1L), symbolCurKeyOpt));
		portfolioElement.setMonth6(
				this.symbolValueAtDate(portfolio, dailyQuotes, LocalDate.now().minusMonths(6L), symbolCurKeyOpt));
		portfolioElement.setYear1(
				this.symbolValueAtDate(portfolio, dailyQuotes, LocalDate.now().minusYears(1L), symbolCurKeyOpt));
		portfolioElement.setYear2(
				this.symbolValueAtDate(portfolio, dailyQuotes, LocalDate.now().minusYears(2L), symbolCurKeyOpt));
		portfolioElement.setYear5(
				this.symbolValueAtDate(portfolio, dailyQuotes, LocalDate.now().minusYears(5L), symbolCurKeyOpt));
		portfolioElement.setYear10(
				this.symbolValueAtDate(portfolio, dailyQuotes, LocalDate.now().minusYears(10L), symbolCurKeyOpt));
		this.updateCorrelations(portfolio, portfolioElement, comparisonSymbols, dailyQuotes);
		this.updateLinRegReturns(portfolio, portfolioElement, comparisonSymbols, dailyQuotes);
		if (!portfolio.getPortfolioElements().contains(portfolioElement)) {
			portfolio.getPortfolioElements().add(portfolioElement);
			portfolioElement.setPortfolio(portfolio);
		}
		return portfolioElement;
	}

	private Double calcLinRegReturn(final Portfolio portfolio, LocalDate cutOffDate, List<DailyQuote> dailyQuotes,
			Collection<DailyQuote> comparisonDailyQuotes) {
		List<CalcValuesDay> calcValuesDays = this
				.createCalcValuesDay(portfolio, cutOffDate, dailyQuotes, comparisonDailyQuotes).stream()
				.sorted((calcValues1, calcValues2) -> calcValues1.day().compareTo(calcValues2.day())).toList();
		List<CalcValuesDay> divValuesDays = new ArrayList<>();
		CalcValuesDay prevValues = null;
		for (int i = 0; i < calcValuesDays.size(); i++) {
			if (prevValues != null) {
				BigDecimal quoteReturn = calcValuesDays.get(i).quote()
						.divide(calcValuesDays.get(i - 1).quote(), 25, RoundingMode.HALF_EVEN)
						.multiply(BigDecimal.valueOf(100L));
				BigDecimal compQuoteReturn = calcValuesDays.get(i).compQuote
						.divide(calcValuesDays.get(i - 1).compQuote(), 25, RoundingMode.HALF_EVEN)
						.multiply(BigDecimal.valueOf(100L));
				CalcValuesDay myCalcValuesDay = new CalcValuesDay(calcValuesDays.get(i).day(), quoteReturn,
						compQuoteReturn);
				divValuesDays.add(myCalcValuesDay);
			}
			prevValues = calcValuesDays.get(i);
		}
		LinearRegressionResults regressionResults = this.calcLinRegReturn(divValuesDays);
		return regressionResults.multiplierDaily().subtract(regressionResults.multiplierComp).doubleValue();
	}

	LinearRegressionResults calcLinRegReturn(List<CalcValuesDay> calcValuesDays) {
		BigDecimalValues meanValues = this.calculateMeans(calcValuesDays);
		BigDecimal yMean = BigDecimal.valueOf(IntStream.range(0, calcValuesDays.size()).sum()).divide(
				BigDecimal.valueOf(calcValuesDays.size() < 1 ? 1 : calcValuesDays.size()), 25, RoundingMode.HALF_EVEN);
		final int[] yValue = new int[1];
		yValue[0] = 0;
		BigDecimal crossDiviationDailyQuotes = calcValuesDays.stream()
				.map(myValue -> myValue.quote().multiply(BigDecimal.valueOf(yValue[0]))).peek(myValue -> {
					yValue[0] = yValue[0] + 1;
				}).reduce(BigDecimal.ZERO, BigDecimal::add)
				.subtract(BigDecimal.valueOf(calcValuesDays.size()).multiply(meanValues.daily()).multiply(yMean));
		yValue[0] = 0;
		BigDecimal crossDiviationCompDailyQuotes = calcValuesDays.stream()
				.map(myValue -> myValue.compQuote().multiply(BigDecimal.valueOf(yValue[0]))).peek(myValue -> {
					yValue[0] = yValue[0] + 1;
				}).reduce(BigDecimal.ZERO, BigDecimal::add)
				.subtract(BigDecimal.valueOf(calcValuesDays.size()).multiply(meanValues.comp()).multiply(yMean));
		BigDecimal diviationDailyQuotes = calcValuesDays.stream().map(CalcValuesDay::quote)
				.map(myValue -> myValue.multiply(myValue)).reduce(BigDecimal.ZERO, BigDecimal::add).subtract(BigDecimal
						.valueOf(calcValuesDays.size()).multiply(meanValues.daily()).multiply(meanValues.daily()));
		BigDecimal diviationCompQuotes = calcValuesDays.stream().map(CalcValuesDay::compQuote)
				.map(myValue -> myValue.multiply(myValue)).reduce(BigDecimal.ZERO, BigDecimal::add).subtract(BigDecimal
						.valueOf(calcValuesDays.size()).multiply(meanValues.comp()).multiply(meanValues.comp()));
		BigDecimal multiplierDailyQuotes = crossDiviationDailyQuotes.divide(
				(diviationDailyQuotes.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ONE : diviationDailyQuotes), 25,
				RoundingMode.HALF_EVEN);
		BigDecimal multiplierCompQuotes = crossDiviationCompDailyQuotes.divide(
				(diviationCompQuotes.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ONE : diviationCompQuotes), 25,
				RoundingMode.HALF_EVEN);
		BigDecimal adderDailyQuotes = yMean.subtract(multiplierDailyQuotes.multiply(meanValues.daily()));
		BigDecimal adderCompQuotes = yMean.subtract(multiplierCompQuotes.multiply(meanValues.comp()));
		return new LinearRegressionResults(multiplierDailyQuotes, adderDailyQuotes, multiplierCompQuotes,
				adderCompQuotes);
	}

	private Double calcCorrelation(final Portfolio portfolio, LocalDate cutOffDate, List<DailyQuote> dailyQuotes,
			Collection<DailyQuote> comparisonDailyQuotes) {
		List<CalcValuesDay> calcValuesDays = this.createCalcValuesDay(portfolio, cutOffDate, dailyQuotes,
				comparisonDailyQuotes);
		return this.calculateCorrelation(calcValuesDays);
	}

	private List<CalcValuesDay> createCalcValuesDay(final Portfolio portfolio, LocalDate cutOffDate,
			List<DailyQuote> dailyQuotes, Collection<DailyQuote> comparisonDailyQuotes) {
		Map<LocalDate, DailyQuote> dailyQuotesMap = dailyQuotes.stream()
				.filter(myQuote -> myQuote.getLocalDay().isAfter(cutOffDate))
				.filter(StreamHelpers.distinctByKey(myQuote -> myQuote.getLocalDay()))
				.collect(Collectors.toMap(DailyQuote::getLocalDay, dq -> dq));
		Map<LocalDate, DailyQuote> comparisonDailyQuotesMap = comparisonDailyQuotes.stream()
				.filter(myQuote -> myQuote.getLocalDay().isAfter(cutOffDate))
				.filter(StreamHelpers.distinctByKey(myQuote -> myQuote.getLocalDay()))
				.collect(Collectors.toMap(DailyQuote::getLocalDay, dq -> dq));
		List<CalcValuesDay> calcValuesDays = dailyQuotesMap.keySet().stream()
				.filter(myDate -> Optional.ofNullable(comparisonDailyQuotesMap.get(myDate)).isPresent())
				.filter(myDate -> checkCurrencyQuotes(portfolio, dailyQuotesMap, comparisonDailyQuotesMap, myDate))
				.map(myDate -> this.createCalcValuesDay(myDate, dailyQuotesMap.get(myDate),
						comparisonDailyQuotesMap.get(myDate), portfolio))
				.toList();
		return calcValuesDays;
	}

	private boolean checkCurrencyQuotes(final Portfolio portfolio, Map<LocalDate, DailyQuote> dailyQuotesMap,
			Map<LocalDate, DailyQuote> comparisonDailyQuotesMap, LocalDate myDate) {
		return this.currencyService
				.getCurrencyQuote(myDate, portfolio.getCurrencyKey(), dailyQuotesMap.get(myDate).getCurrencyKey())
				.isPresent()
				&& this.currencyService.getCurrencyQuote(myDate, portfolio.getCurrencyKey(),
						comparisonDailyQuotesMap.get(myDate).getCurrencyKey()).isPresent();
	}

	private BigDecimalValues calculateMeans(List<CalcValuesDay> calcValuesDays) {
		BigDecimal meanDailyQuotes = calcValuesDays.stream().map(myValue -> myValue.quote)
				.reduce(BigDecimal.ZERO, BigDecimal::add)
				.divide(BigDecimal.valueOf(calcValuesDays.size() < 1 ? 1 : calcValuesDays.size()), 25,
						RoundingMode.HALF_EVEN);
		BigDecimal meanCompQuotes = calcValuesDays.stream().map(myValue -> myValue.compQuote)
				.reduce(BigDecimal.ZERO, BigDecimal::add)
				.divide(BigDecimal.valueOf(calcValuesDays.size() < 1 ? 1 : calcValuesDays.size()), 25,
						RoundingMode.HALF_EVEN);
		return new BigDecimalValues(meanDailyQuotes, meanCompQuotes);
	}

	double calculateCorrelation(List<CalcValuesDay> calcValuesDays) {
		BigDecimalValues meanValues = this.calculateMeans(calcValuesDays);
		BigDecimal sumMultQuotes = calcValuesDays.stream()
				.map(myValue -> new BigDecimalValues(myValue.quote.subtract(meanValues.daily),
						myValue.compQuote.subtract(meanValues.comp)))
				.map(myRecord -> myRecord.daily.multiply(myRecord.comp)).reduce(BigDecimal.ZERO, BigDecimal::add).abs();
		BigDecimal squaredMeanDailyQuotes = calcValuesDays.stream().map(CalcValuesDay::quote)
				.map(myValue -> myValue.subtract(meanValues.daily)).map(myValue -> myValue.multiply(myValue))
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal squaredMeanCompQuotes = calcValuesDays.stream().map(CalcValuesDay::compQuote)
				.map(myValue -> myValue.subtract(meanValues.comp)).map(myValue -> myValue.multiply(myValue))
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal devisor = squaredMeanCompQuotes.multiply(squaredMeanDailyQuotes).sqrt(MathContext.DECIMAL128);
		double correlation = sumMultQuotes.divide((devisor.compareTo(BigDecimal.ZERO) <= 0 ? BigDecimal.ONE : devisor),
				25, RoundingMode.HALF_EVEN).doubleValue();
		return correlation;
	}

	private CalcValuesDay createCalcValuesDay(LocalDate day, DailyQuote dailyQuote, DailyQuote comparisonQuote,
			Portfolio portfolio) {
		Currency comparisonCurrency = this.currencyService
				.getCurrencyQuote(day, portfolio.getCurrencyKey(), comparisonQuote.getCurrencyKey()).get();
		Currency dailyQuoteCurrency = this.currencyService
				.getCurrencyQuote(day, portfolio.getCurrencyKey(), dailyQuote.getCurrencyKey()).get();
		BigDecimal comparisonValue = this.calcValue(Currency::getClose, comparisonCurrency, DailyQuote::getClose,
				comparisonQuote, portfolio);
		BigDecimal dailyQuoteValue = this.calcValue(Currency::getClose, dailyQuoteCurrency, DailyQuote::getClose,
				dailyQuote, portfolio);
		return new CalcValuesDay(day, dailyQuoteValue, comparisonValue);
	}

	private BigDecimal symbolValueAtDate(final Portfolio portfolio, List<DailyQuote> dailyQuotes, LocalDate cutOffDate,
			Optional<CurrencyKey> symbolCurrencyKeyOpt) {
		return dailyQuotes.stream().filter(myDailyQuote -> myDailyQuote.getLocalDay().isBefore(cutOffDate))
				.max(Comparator.comparing(DailyQuote::getLocalDay))
				.map(myDailyQuote -> this.calcValue(Currency::getClose,
						this.getCurrencyValue(portfolio, myDailyQuote, symbolCurrencyKeyOpt), DailyQuote::getClose,
						myDailyQuote, portfolio))
				.orElse(BigDecimal.ZERO);
	}

	private Currency getCurrencyValue(final Portfolio portfolio, DailyQuote myDailyQuote,
			Optional<CurrencyKey> symbolCurrencyKeyOpt) {
		return symbolCurrencyKeyOpt.stream()
				.map(symbolCurrencyKey -> this.currencyService.getCurrencyQuote(myDailyQuote.getLocalDay(),
						portfolio.getCurrencyKey(), symbolCurrencyKey))
				.filter(Optional::isPresent).map(Optional::get).findFirst()
				.orElse(new Currency(myDailyQuote.getLocalDay(), portfolio.getCurrencyKey(), portfolio.getCurrencyKey(),
						BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE));
	}
}
