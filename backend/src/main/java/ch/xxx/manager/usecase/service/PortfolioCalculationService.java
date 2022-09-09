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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ch.xxx.manager.domain.exception.ResourceNotFoundException;
import ch.xxx.manager.domain.model.entity.Currency;
import ch.xxx.manager.domain.model.entity.DailyQuote;
import ch.xxx.manager.domain.model.entity.DailyQuoteRepository;
import ch.xxx.manager.domain.model.entity.Portfolio;
import ch.xxx.manager.domain.model.entity.PortfolioElement;
import ch.xxx.manager.domain.model.entity.PortfolioToSymbol;
import ch.xxx.manager.domain.model.entity.Symbol;
import ch.xxx.manager.domain.model.entity.dto.CalcPortfolioElement;
import ch.xxx.manager.domain.model.entity.dto.DailyQuoteEntityDto;
import ch.xxx.manager.domain.model.entity.dto.PortfolioWithElements;
import ch.xxx.manager.domain.utils.CurrencyKey;
import ch.xxx.manager.domain.utils.StreamHelpers;
import ch.xxx.manager.usecase.mapping.MappingUtils;

@Service
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class PortfolioCalculationService {
	private record PortfolioSymbolWithDailyQuotes(Symbol symbol, List<DailyQuote> dailyQuotes) {
	};

	private record PortfolioData(Map<Long, List<DailyQuote>> dailyQuotesMap,
			PortfolioSymbolWithDailyQuotes portfolioQuotes, List<CalcPortfolioElement> portfolioElements) {
	};

	private static final Logger LOG = LoggerFactory.getLogger(PortfolioCalculationService.class);
	private final DailyQuoteRepository dailyQuoteRepository;
	private final CurrencyService currencyService;

	public record ComparisonIndexQuotes(ComparisonIndex comparisonIndex,
			List<DailyQuoteEntityDto> dailyQuoteEntityDtos) {
	};

	public PortfolioCalculationService(DailyQuoteRepository dailyQuoteRepository, CurrencyService currencyService) {
		this.dailyQuoteRepository = dailyQuoteRepository;
		this.currencyService = currencyService;
	}

	public List<CalcPortfolioElement> calculatePortfolioBars(Portfolio portfolio, LocalDate cutOffDate,
			List<ComparisonIndexQuotes> comparisonQuotes) {
		Optional.ofNullable(portfolio).orElseThrow(() -> new ResourceNotFoundException("Portfolio not found."));
		LOG.info("Portfolio calculation bars called for: {}", portfolio.getId());
		List<PortfolioToSymbol> portfolioToSymbols = List.copyOf(portfolio.getPortfolioToSymbols());
		List<CalcPortfolioElement> portfolioElements = portfolioToSymbols.stream()
				.flatMap(portfolioPts -> StreamHelpers
						.toStream(this.dailyQuoteRepository.findBySymbolId(portfolioPts.getSymbol().getId(),
								cutOffDate.minus(1, ChronoUnit.MONTHS), LocalDate.now()))
						.map(dailyQuote -> new CalcPortfolioElement(portfolioPts.getSymbol().getId(),
								dailyQuote.getLocalDay(), dailyQuote.getClose(),
								Symbol.QuoteSource.PORTFOLIO.equals(portfolioPts.getSymbol().getQuoteSource())
										? portfolioPts.getSymbol().getName()
										: portfolioPts.getSymbol().getSymbol(),
								portfolioPts.getWeight())))
				.collect(Collectors.toList());

		List<CalcPortfolioElement> compQuotesPE = comparisonQuotes.stream()
				.flatMap(compQuotes -> compQuotes.dailyQuoteEntityDtos.stream())
				.map(dQuote -> new CalcPortfolioElement(dQuote.entity().getSymbol().getId(),
						dQuote.entity().getLocalDay(), dQuote.entity().getClose(),
						dQuote.entity().getSymbol().getSymbol(), 1L))
				.collect(Collectors.toList());

		portfolioElements.addAll(compQuotesPE);

		List<CalcPortfolioElement> cutOffPEs = portfolioElements.stream()
				.flatMap(pe -> this.findValueAtDate(portfolioElements, cutOffDate, pe.symbolId()).stream())
				.filter(StreamHelpers.distinctByKey(CalcPortfolioElement::symbolId)).toList();
		List<CalcPortfolioElement> maxPEs = portfolioElements.stream()
				.collect(Collectors.groupingBy(pe -> pe.symbolId())).entrySet().stream()
				.flatMap(entry -> StreamHelpers
						.toStream(entry.getValue().stream().max(Comparator.comparing(CalcPortfolioElement::localDate))
								.orElseThrow(NoSuchElementException::new)))
				.toList();
		List<CalcPortfolioElement> resultPortfolioElements = maxPEs.stream().map(pe -> {
			BigDecimal value = cutOffPEs.stream().filter(myPe -> myPe.symbolId().equals(pe.symbolId()))
					.map(myPe -> myPe.value()).findFirst().isEmpty()
							? BigDecimal.ZERO
							: pe.value()
									.divide(cutOffPEs.stream().filter(myPe -> myPe.symbolId().equals(pe.symbolId()))
											.map(myPe -> myPe.value()).findFirst().get(), 8, RoundingMode.HALF_EVEN)
									.subtract(BigDecimal.ONE).multiply(BigDecimal.valueOf(100));
			return new CalcPortfolioElement(pe.symbolId(), pe.localDate(), value, pe.symbolName(), pe.weight());
		}).toList();
		return resultPortfolioElements;
	}

	public PortfolioWithElements calculatePortfolio(Portfolio portfolio) {
		Optional.ofNullable(portfolio).orElseThrow(() -> new ResourceNotFoundException("Portfolio not found."));
		LOG.info("Portfolio calculation called for: {}", portfolio.getId());
		List<PortfolioToSymbol> portfolioToSymbols = List.copyOf(portfolio.getPortfolioToSymbols());
		PortfolioData myPortfolioData = this.calculatePortfolioData(portfolioToSymbols);
		LocalDate cutOffDate = LocalDate.now().minus(Period.ofMonths(1));
		portfolio.setMonth1(
				this.portfolioValueAtDate(portfolioToSymbols, myPortfolioData.portfolioElements(), cutOffDate));
		cutOffDate = LocalDate.now().minus(Period.ofMonths(6));
		portfolio.setMonth6(
				this.portfolioValueAtDate(portfolioToSymbols, myPortfolioData.portfolioElements(), cutOffDate));
		cutOffDate = LocalDate.now().minus(Period.ofYears(1));
		portfolio.setYear1(
				this.portfolioValueAtDate(portfolioToSymbols, myPortfolioData.portfolioElements(), cutOffDate));
		cutOffDate = LocalDate.now().minus(Period.ofYears(2));
		portfolio.setYear2(
				this.portfolioValueAtDate(portfolioToSymbols, myPortfolioData.portfolioElements(), cutOffDate));
		cutOffDate = LocalDate.now().minus(Period.ofYears(5));
		portfolio.setYear5(
				this.portfolioValueAtDate(portfolioToSymbols, myPortfolioData.portfolioElements(), cutOffDate));
		cutOffDate = LocalDate.now().minus(Period.ofYears(10));
		portfolio.setYear10(
				this.portfolioValueAtDate(portfolioToSymbols, myPortfolioData.portfolioElements(), cutOffDate));
		PortfolioWithElements result = this.calculatePortfolioWithElements(portfolio, portfolioToSymbols);
		return result;
	}

	private PortfolioWithElements calculatePortfolioWithElements(final Portfolio portfolio,
			List<PortfolioToSymbol> portfolioToSymbols) {
		Map<Long, List<DailyQuote>> dailyQuotesMap = this.createDailyQuotesMap(portfolioToSymbols);
		List<PortfolioElement> portfolioElements = portfolioToSymbols.stream()
				.filter(pts -> !pts.getSymbol().getSymbol().contains(ServiceUtils.PORTFOLIO_MARKER))
				.filter(pts -> pts.getRemovedAt() == null).map(pts -> pts.getSymbol().getId())
				.flatMap(symbolId -> Stream.of(dailyQuotesMap.get(symbolId))).flatMap(myDailyQuotes -> Stream
						.of(this.createPortfolioElement(portfolio, myDailyQuotes, portfolioToSymbols)))
				.toList();
		PortfolioWithElements result = new PortfolioWithElements(portfolio, portfolioElements);
		return result;
	}

	private PortfolioElement createPortfolioElement(final Portfolio portfolio, final List<DailyQuote> dailyQuotes,
			final List<PortfolioToSymbol> portfolioToSymbols) {
		PortfolioElement portfolioElement = portfolio.getPortfolioElements().stream()
				.filter(myPortfolioElement -> dailyQuotes.stream().anyMatch(
						myDailyQuote -> myDailyQuote.getSymbolKey().equalsIgnoreCase(myPortfolioElement.getSymbol())))
				.findFirst().orElse(new PortfolioElement());
		Optional<PortfolioToSymbol> ptsOpt = portfolioToSymbols.stream()
				.filter(pts -> dailyQuotes.get(0).getSymbolKey().equalsIgnoreCase(pts.getSymbol().getSymbol()))
				.findFirst();
		portfolioElement.setSymbol(dailyQuotes.get(0).getSymbolKey());
		String ptsName = ptsOpt.stream().map(pts -> pts.getSymbol().getName()).findFirst().orElse("Unkown");
		Optional<CurrencyKey> symbolCurKeyOpt = ptsOpt.stream().map(pts -> pts.getSymbol().getCurrencyKey())
				.findFirst();
		String sectorName = MappingUtils.findSectorName(ptsOpt.stream().map(PortfolioToSymbol::getSymbol).findFirst());
		portfolioElement.setSector(sectorName);
		portfolioElement.setWeight(ptsOpt.stream().map(myPts -> myPts.getWeight()).findFirst().orElse(0L));
		portfolioElement.setName(ptsName);
		portfolioElement.setPortfolio(portfolio);
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
		if (!portfolio.getPortfolioElements().contains(portfolioElement)) {
			portfolio.getPortfolioElements().add(portfolioElement);
		}
		return portfolioElement;
	}

	private BigDecimal symbolValueAtDate(final Portfolio portfolio, List<DailyQuote> dailyQuotes, LocalDate cutOffDate,
			Optional<CurrencyKey> symbolCurrencyKeyOpt) {
		return dailyQuotes.stream().filter(myDailyQuote -> myDailyQuote.getLocalDay().isBefore(cutOffDate))
				.max(Comparator.comparing(DailyQuote::getLocalDay))
				.map(myDailyQuote -> this.calcValue(Currency::getClose,
						getCurrencyValue(portfolio, myDailyQuote, symbolCurrencyKeyOpt), DailyQuote::getClose,
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

	private PortfolioData calculatePortfolioData(List<PortfolioToSymbol> portfolioToSymbols) {
		Map<Long, List<DailyQuote>> dailyQuotesMap = createDailyQuotesMap(portfolioToSymbols);
		PortfolioSymbolWithDailyQuotes portfolioQuotes = portfolioToSymbols.stream()
				.filter(pts -> pts.getSymbol().getSymbol().contains(ServiceUtils.PORTFOLIO_MARKER))
				.peek(pts -> LOG.info(pts.getSymbol().getSymbol() + " " + pts.getSymbol().getId()))
				.map(pts -> new PortfolioSymbolWithDailyQuotes(pts.getSymbol(),
						Optional.ofNullable(dailyQuotesMap.get(pts.getSymbol().getId())).stream()
								.flatMap(Collection::stream).map(myDailyQuote -> this.resetPortfolioQuote(myDailyQuote))
								.collect(Collectors.toList())))
				.findFirst().orElseThrow(() -> new ResourceNotFoundException("Portfolio Symbol not found."));
		List<CalcPortfolioElement> portfolioElements = portfolioToSymbols.stream()
				.filter(pts -> !pts.getSymbol().getSymbol().contains(ServiceUtils.PORTFOLIO_MARKER))
				.map(pts -> this.calcPortfolioQuotesForSymbol(pts, dailyQuotesMap.get(pts.getSymbol().getId()),
						portfolioQuotes))
				.flatMap(Collection::stream).sorted(Comparator.comparing(CalcPortfolioElement::localDate))
				.collect(Collectors.toList());
		return new PortfolioData(dailyQuotesMap, portfolioQuotes, portfolioElements);
	}

	private Map<Long, List<DailyQuote>> createDailyQuotesMap(List<PortfolioToSymbol> portfolioToSymbols) {
		Map<Long, List<DailyQuote>> dailyQuotesMap = this.dailyQuoteRepository
				.findBySymbolIds(portfolioToSymbols.stream().map(mySymbol -> mySymbol.getSymbol().getId())
						.collect(Collectors.toList()))
				.stream().collect(Collectors.groupingBy(myDailyQuote -> myDailyQuote.getSymbol().getId()));
		return dailyQuotesMap;
	}

	private DailyQuote resetPortfolioQuote(DailyQuote myDailyQuote) {
		myDailyQuote.setClose(BigDecimal.ZERO);
		myDailyQuote.setHigh(BigDecimal.ZERO);
		myDailyQuote.setLow(BigDecimal.ZERO);
		myDailyQuote.setOpen(BigDecimal.ZERO);
		return myDailyQuote;
	}

	private BigDecimal portfolioValueAtDate(List<PortfolioToSymbol> portfolioToSymbols,
			List<CalcPortfolioElement> portfolioElements, LocalDate cutOffDate) {
		BigDecimal result = portfolioToSymbols.stream()
				.filter(pts -> !pts.getSymbol().getSymbol().contains(ServiceUtils.PORTFOLIO_MARKER))
//				.peek(pts -> LOG.info(pts.getSymbol().getSymbol()))
				.map(pts -> findValueAtDate(portfolioElements, cutOffDate, pts.getSymbol().getId()))
				.flatMap(value -> StreamHelpers.unboxOptionals(value)).map(pe -> pe.value())
				.reduce(BigDecimal.ZERO, (acc, value) -> acc.add(value));
		return result;
	}

	private Optional<CalcPortfolioElement> findValueAtDate(List<CalcPortfolioElement> portfolioElements,
			LocalDate cutOffDate, Long symbolId) {
		return portfolioElements.stream().filter(pts -> pts.symbolId().equals(symbolId))
				.filter(pts -> pts.localDate().isBefore(cutOffDate))
				.max(Comparator.comparing(CalcPortfolioElement::localDate));
	}

	private Collection<CalcPortfolioElement> calcPortfolioQuotesForSymbol(PortfolioToSymbol portfolioToSymbol,
			List<DailyQuote> dailyQuotes, PortfolioSymbolWithDailyQuotes portfolioQuotes) {
		return dailyQuotes.stream()
				.filter(myDailyQuote -> portfolioToSymbol.getChangedAt().compareTo(myDailyQuote.getLocalDay()) <= 0
						&& Optional.ofNullable(portfolioToSymbol.getRemovedAt()).stream()
								.filter(myRemovedAt -> myDailyQuote.getLocalDay().compareTo(myRemovedAt) >= 0).findAny()
								.isEmpty())
				.map(myDailyQuote -> this.calculatePortfolioElement(myDailyQuote, portfolioToSymbol, portfolioQuotes))
				.filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
	}

	private Optional<CalcPortfolioElement> calculatePortfolioElement(DailyQuote dailyQuote,
			PortfolioToSymbol portfolioToSymbol, PortfolioSymbolWithDailyQuotes portfolioQuotes) {
		return this.currencyService.getCurrencyQuote(portfolioToSymbol, dailyQuote).map(currencyQuote -> {
			DailyQuote myPortfolioQuote = this.upsertPortfolioQuote(currencyQuote, dailyQuote, portfolioToSymbol,
					portfolioQuotes);
			return new CalcPortfolioElement(portfolioToSymbol.getSymbol().getId(), myPortfolioQuote.getLocalDay(),
					myPortfolioQuote.getClose(), portfolioToSymbol.getSymbol().getName(),
					portfolioToSymbol.getWeight());
		});
	}

	private DailyQuote upsertPortfolioQuote(Currency currencyQuote, DailyQuote dailyQuote,
			PortfolioToSymbol portfolioToSymbol, PortfolioSymbolWithDailyQuotes portfolioQuotes) {
		DailyQuote portfolioQuote = portfolioQuotes.dailyQuotes.stream()
				.filter(myDailyQuote -> myDailyQuote.getLocalDay().isEqual(dailyQuote.getLocalDay())).findFirst()
				.orElse(new DailyQuote());
		portfolioQuote.setClose(this.calcValue(Currency::getClose, currencyQuote, DailyQuote::getClose, dailyQuote,
				portfolioToSymbol, portfolioQuote.getClose()));
		portfolioQuote.setCurrencyKey(portfolioToSymbol.getSymbol().getCurrencyKey());
		portfolioQuote.setHigh(this.calcValue(Currency::getHigh, currencyQuote, DailyQuote::getHigh, dailyQuote,
				portfolioToSymbol, portfolioQuote.getHigh()));
		portfolioQuote.setLocalDay(dailyQuote.getLocalDay());
		portfolioQuote.setLow(this.calcValue(Currency::getLow, currencyQuote, DailyQuote::getLow, dailyQuote,
				portfolioToSymbol, portfolioQuote.getLow()));
		portfolioQuote.setOpen(this.calcValue(Currency::getOpen, currencyQuote, DailyQuote::getOpen, dailyQuote,
				portfolioToSymbol, portfolioQuote.getOpen()));
		portfolioQuote.setSymbol(portfolioQuotes.symbol);
		portfolioQuote.setSymbolKey(portfolioQuotes.symbol.getSymbol());
		portfolioQuote.setVolume(1L);
		if (Optional.ofNullable(portfolioQuote.getId()).isEmpty()) {
			portfolioQuote = this.dailyQuoteRepository.save(portfolioQuote);
			portfolioToSymbol.getSymbol().getDailyQuotes().add(portfolioQuote);
			portfolioQuotes.dailyQuotes.add(portfolioQuote);
		}
		return portfolioQuote;
	}

	private BigDecimal calcValue(Function<? super Currency, BigDecimal> currExtractor, Currency currencyQuote,
			Function<? super DailyQuote, BigDecimal> quoteExtractor, DailyQuote dailyQuote,
			PortfolioToSymbol portfolioToSymbol, final BigDecimal portfolioClose) {
		BigDecimal calcValue = calcValue(currExtractor, currencyQuote, quoteExtractor, dailyQuote,
				portfolioToSymbol.getPortfolio());
		return Optional.ofNullable(portfolioClose).orElse(BigDecimal.ZERO).add(calcValue)
				.multiply(BigDecimal.valueOf(portfolioToSymbol.getWeight()));
	}

	private BigDecimal calcValue(Function<? super Currency, BigDecimal> currExtractor, Currency currencyQuote,
			Function<? super DailyQuote, BigDecimal> quoteExtractor, DailyQuote dailyQuote, final Portfolio portfolio) {
		final BigDecimal currValue = currExtractor.apply(currencyQuote);
		final BigDecimal quoteValue = quoteExtractor.apply(dailyQuote);
		BigDecimal calcValue = BigDecimal.ZERO;
		if (dailyQuote.getCurrencyKey().equals(currencyQuote.getFromCurrKey())
				&& portfolio.getCurrencyKey().equals(currencyQuote.getToCurrKey())) {
			calcValue = quoteValue.multiply(currValue);
		} else if (dailyQuote.getCurrencyKey().equals(currencyQuote.getToCurrKey())
				&& portfolio.getCurrencyKey().equals(currencyQuote.getFromCurrKey())) {
			calcValue = quoteValue.divide(currValue, 10, RoundingMode.HALF_UP);
		}
		return calcValue;
	}
}
