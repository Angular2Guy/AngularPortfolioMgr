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
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.xxx.manager.domain.exception.ResourceNotFoundException;
import ch.xxx.manager.domain.model.entity.Currency;
import ch.xxx.manager.domain.model.entity.DailyQuote;
import ch.xxx.manager.domain.model.entity.DailyQuoteRepository;
import ch.xxx.manager.domain.model.entity.Portfolio;
import ch.xxx.manager.domain.model.entity.PortfolioToSymbol;
import ch.xxx.manager.domain.model.entity.Symbol;
import ch.xxx.manager.domain.model.entity.dto.CalcPortfolioElement;
import ch.xxx.manager.domain.model.entity.dto.DailyQuoteEntityDto;
import ch.xxx.manager.domain.model.entity.dto.PortfolioWithElements;
import ch.xxx.manager.domain.utils.StreamHelpers;

@Service
@Transactional
public class PortfolioCalculationService extends PortfolioCalculcationBase {
	private static final Logger LOG = LoggerFactory.getLogger(PortfolioCalculationService.class);
	private final PortfolioStatisticService portfolioStatisticService;

	private record PortfolioSymbolWithDailyQuotes(Symbol symbol, List<DailyQuote> dailyQuotes) {
	};

	private record PortfolioData(Map<String, List<DailyQuote>> dailyQuotesMap,
			PortfolioSymbolWithDailyQuotes portfolioQuotes, List<CalcPortfolioElement> portfolioElements,
			List<DailyQuote> dailyQuotesToRemove) {
	};

	private record PtsWithList(PortfolioToSymbol pts, Collection<PortfolioToSymbol> ptsList) {
	};

	public record ComparisonIndexQuotes(ComparisonIndex comparisonIndex,
			List<DailyQuoteEntityDto> dailyQuoteEntityDtos) {
	};

	public PortfolioCalculationService(DailyQuoteRepository dailyQuoteRepository, CurrencyService currencyService,
			PortfolioStatisticService portfolioStatisticService) {
		super(dailyQuoteRepository, currencyService);
		this.portfolioStatisticService = portfolioStatisticService;
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
								dailyQuote.getLocalDay(), dailyQuote.getAdjClose(),
								Symbol.QuoteSource.PORTFOLIO.equals(portfolioPts.getSymbol().getQuoteSource())
										? portfolioPts.getSymbol().getName()
										: portfolioPts.getSymbol().getSymbol(),
								portfolioPts.getWeight())))
				.collect(Collectors.toList());

		List<CalcPortfolioElement> compQuotesPE = comparisonQuotes.stream()
				.flatMap(compQuotes -> compQuotes.dailyQuoteEntityDtos.stream())
				.map(dQuote -> new CalcPortfolioElement(dQuote.entity().getSymbol().getId(),
						dQuote.entity().getLocalDay(), dQuote.entity().getAdjClose(),
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

	public PortfolioWithElements calculatePortfolio(Portfolio portfolio, Optional<PortfolioToSymbol> ptsOpt) {
		Optional.ofNullable(portfolio).orElseThrow(() -> new ResourceNotFoundException("Portfolio not found."));
		LOG.info("Portfolio calculation called for: {}", portfolio.getId());
		Portfolio myPortfolio = this.addDailyQuotes(portfolio);
		PortfolioData myPortfolioData = this.calculatePortfolioData(myPortfolio.getPortfolioToSymbols(), ptsOpt);
		LocalDate cutOffDate = LocalDate.now().minus(Period.ofMonths(1));
		myPortfolio.setMonth1(this.portfolioValueAtDate(myPortfolio.getPortfolioToSymbols(),
				myPortfolioData.portfolioElements(), cutOffDate));
		cutOffDate = LocalDate.now().minus(Period.ofMonths(6));
		myPortfolio.setMonth6(this.portfolioValueAtDate(myPortfolio.getPortfolioToSymbols(),
				myPortfolioData.portfolioElements(), cutOffDate));
		cutOffDate = LocalDate.now().minus(Period.ofYears(1));
		myPortfolio.setYear1(this.portfolioValueAtDate(myPortfolio.getPortfolioToSymbols(),
				myPortfolioData.portfolioElements(), cutOffDate));
		cutOffDate = LocalDate.now().minus(Period.ofYears(2));
		myPortfolio.setYear2(this.portfolioValueAtDate(myPortfolio.getPortfolioToSymbols(),
				myPortfolioData.portfolioElements(), cutOffDate));
		cutOffDate = LocalDate.now().minus(Period.ofYears(5));
		myPortfolio.setYear5(this.portfolioValueAtDate(myPortfolio.getPortfolioToSymbols(),
				myPortfolioData.portfolioElements(), cutOffDate));
		cutOffDate = LocalDate.now().minus(Period.ofYears(10));
		myPortfolio.setYear10(this.portfolioValueAtDate(myPortfolio.getPortfolioToSymbols(),
				myPortfolioData.portfolioElements(), cutOffDate));
		PortfolioWithElements temp = this.portfolioStatisticService.calculatePortfolioWithElements(myPortfolio,
				myPortfolioData.portfolioQuotes.dailyQuotes);
		PortfolioWithElements result = new PortfolioWithElements(temp.portfolio(), temp.portfolioElements(),
				myPortfolioData.dailyQuotesToRemove, myPortfolioData.portfolioQuotes.dailyQuotes);
		return result;
	}

	private PortfolioData calculatePortfolioData(Set<PortfolioToSymbol> portfolioToSymbols,
			Optional<PortfolioToSymbol> ptsOpt) {
		Map<String, List<DailyQuote>> dailyQuotesMap = this.createDailyQuotesKeyMap(portfolioToSymbols);
		final List<LocalDate> commonQuoteDates = this.filteredCommonQuoteDates(dailyQuotesMap);
//		commonQuoteDates.stream().filter(myDate -> LocalDate.of(2022, 9, 1).isBefore(myDate))
//				.peek(myDate -> LOG.info("CommonDates: {}", myDate.toString())).count();
		final String portfolioSymbolKey = portfolioToSymbols.stream()
				.filter(pts -> pts.getSymbol().getSymbol().contains(ServiceUtils.PORTFOLIO_MARKER))
				.map(pts -> pts.getSymbol().getSymbol()).findFirst()
				.orElseThrow(() -> new ResourceNotFoundException("Portfolio Symbol not found."));
		List<DailyQuote> toDelete = dailyQuotesMap.getOrDefault(portfolioSymbolKey, List.of()).stream()
				.filter(myDailyQuote -> commonQuoteDates.stream()
						.noneMatch(myLocalDate -> myLocalDate.isEqual(myDailyQuote.getLocalDay())))
				.toList();
//		toDelete.stream().filter(myQuote -> LocalDate.of(2022, 9, 1).isBefore(myQuote.getLocalDay()))
//				.peek(myQuote -> LOG.info("DeleteDates: {}", myQuote.getLocalDay().toString())).count();
		List<DailyQuote> myQuotes = dailyQuotesMap.getOrDefault(portfolioSymbolKey, new LinkedList<>());
		myQuotes.removeAll(toDelete);
		dailyQuotesMap.put(portfolioSymbolKey, myQuotes);
		PortfolioSymbolWithDailyQuotes portfolioQuotes = portfolioToSymbols.stream()
				.filter(pts -> pts.getSymbol().getSymbol().contains(ServiceUtils.PORTFOLIO_MARKER))
				.peek(pts -> LOG.info(pts.getSymbol().getSymbol() + " " + pts.getSymbol().getId()))
				.map(pts -> new PortfolioSymbolWithDailyQuotes(pts.getSymbol(),
						Optional.ofNullable(dailyQuotesMap.get(pts.getSymbol().getSymbol())).stream()
								.flatMap(Collection::stream)
								.filter(myDailyQuote -> commonQuoteDates.stream()
										.anyMatch(myLocalDate -> myLocalDate.isEqual(myDailyQuote.getLocalDay())))
								.map(myDailyQuote -> this.resetPortfolioQuote(myDailyQuote))
								.collect(Collectors.toList())))
				.findFirst().orElseThrow(() -> new ResourceNotFoundException("Portfolio Symbol not found."));
		List<CalcPortfolioElement> portfolioElements = portfolioToSymbols.stream()
				.filter(pts -> !pts.getSymbol().getSymbol().contains(ServiceUtils.PORTFOLIO_MARKER))
				.filter(StreamHelpers.distinctByKey(pts -> pts.getSymbol().getSymbol()))
				.map(pts -> this.calcPortfolioQuotesForSymbol(new PtsWithList(pts, portfolioToSymbols),
						dailyQuotesMap.getOrDefault(pts.getSymbol().getSymbol(), new LinkedList<>()), portfolioQuotes,
						commonQuoteDates))
				.flatMap(Collection::stream).sorted(Comparator.comparing(CalcPortfolioElement::localDate))
				.collect(Collectors.toList());
		return new PortfolioData(dailyQuotesMap, portfolioQuotes, portfolioElements, toDelete);
	}

	private Optional<PortfolioToSymbol> findAtDayPts(String symbolStr, LocalDate atDay,
			Collection<PortfolioToSymbol> portfolioToSymbols) {
//		Map<String, List<PortfolioToSymbol>> ptsMap = portfolioToSymbols.stream().filter(pts -> pts.getSymbol().getSymbol().contains(ServiceUtils.PORTFOLIO_MARKER))
//				.collect(Collectors.groupingBy(pts -> pts.getSymbol().getSymbol()));
		LocalDate[] first = new LocalDate[1];
		Optional<PortfolioToSymbol> ptsWeight = portfolioToSymbols.stream()
				.filter(pts -> pts.getSymbol().getSymbol().equalsIgnoreCase(symbolStr))
				.sorted((pts1, pts2) -> pts1.getChangedAt().compareTo(pts2.getChangedAt()))
				.peek(pts -> first[0] = first[0] == null ? pts.getChangedAt() : first[0])
				.filter(pts -> pts.getChangedAt().compareTo(atDay) <= 0).filter(pts -> atDay.compareTo(first[0]) >= 0)
				.filter(pts -> Optional.ofNullable(pts.getRemovedAt()).stream()
						.noneMatch(myRemovedAt -> myRemovedAt.compareTo(LocalDate.now()) <= 0))
				.max((pts1,pts2) -> pts1.getChangedAt().compareTo(pts2.getChangedAt()));
		return ptsWeight;
	}

	private DailyQuote resetPortfolioQuote(DailyQuote myDailyQuote) {
		myDailyQuote.setClose(BigDecimal.ZERO);
		myDailyQuote.setAdjClose(BigDecimal.ZERO);
		myDailyQuote.setHigh(BigDecimal.ZERO);
		myDailyQuote.setLow(BigDecimal.ZERO);
		myDailyQuote.setOpen(BigDecimal.ZERO);
		return myDailyQuote;
	}

	private BigDecimal portfolioValueAtDate(Set<PortfolioToSymbol> portfolioToSymbols,
			List<CalcPortfolioElement> portfolioElements, LocalDate cutOffDate) {
		BigDecimal result = portfolioToSymbols.stream()
				.filter(pts -> !pts.getSymbol().getSymbol().contains(ServiceUtils.PORTFOLIO_MARKER))
//				.peek(pts -> LOG.info(pts.getSymbol().getSymbol()))
				.map(pts -> findValueAtDate(portfolioElements, cutOffDate, pts.getSymbol().getId()))
				.flatMap(StreamHelpers::unboxOptional)
//				.peek(pe -> LOG.info("value: {}, weight: {}", pe.value(), pe.weight()))
				.map(pe -> pe.value().multiply(BigDecimal.valueOf(pe.weight()), MathContext.DECIMAL128))
				.reduce(BigDecimal.ZERO, (acc, value) -> acc.add(value));
		return result;
	}

	private Optional<CalcPortfolioElement> findValueAtDate(List<CalcPortfolioElement> portfolioElements,
			LocalDate cutOffDate, Long symbolId) {
		return portfolioElements.stream().filter(pts -> pts.symbolId().equals(symbolId))
				.filter(pts -> pts.localDate().isBefore(cutOffDate))
				.max(Comparator.comparing(CalcPortfolioElement::localDate));
	}

	private Collection<CalcPortfolioElement> calcPortfolioQuotesForSymbol(PtsWithList ptsWithList,
			List<DailyQuote> dailyQuotes, PortfolioSymbolWithDailyQuotes portfolioQuotes,
			List<LocalDate> commonQuoteDates) {
		return dailyQuotes.stream().filter(myDailyQuote -> this
				.findAtDayPts(
						ptsWithList.pts().getSymbol().getSymbol(), myDailyQuote.getLocalDay(), ptsWithList.ptsList())
				.stream()
				.anyMatch(myPts -> myPts.getChangedAt().compareTo(myDailyQuote.getLocalDay()) <= 0
						&& Optional.ofNullable(myPts.getRemovedAt()).stream()
								.filter(myRemovedAt -> myDailyQuote.getLocalDay().compareTo(myRemovedAt) >= 0).findAny()
								.isEmpty()))
				.filter(myDailyQuote -> commonQuoteDates.stream()
						.anyMatch(myCommonDate -> myCommonDate.isEqual(myDailyQuote.getLocalDay())))
				.map(myDailyQuote -> this.calculatePortfolioElement(myDailyQuote, ptsWithList, portfolioQuotes))
				.filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
	}

	private Optional<CalcPortfolioElement> calculatePortfolioElement(DailyQuote dailyQuote, PtsWithList ptsWithList,
			PortfolioSymbolWithDailyQuotes portfolioQuotes) {
		return this
				.findAtDayPts(ptsWithList.pts().getSymbol().getSymbol(), dailyQuote.getLocalDay(),
						ptsWithList.ptsList())
				.stream().map(pts -> this.currencyService.getCurrencyQuote(pts, dailyQuote).map(currencyQuote -> {
					if ((pts.getRemovedAt() != null && pts.getRemovedAt().compareTo(LocalDate.now()) <= 0)
							|| pts.getWeight().equals(0L)) {
						LOG.error("Symbol: {}, Date: {}, Weight: {}", pts.getSymbol().getSymbol(),
								dailyQuote.getLocalDay().toString(), pts.getWeight());
					}
					DailyQuote myPortfolioQuote = this.upsertPortfolioQuote(currencyQuote, dailyQuote, pts,
							portfolioQuotes);
					BigDecimal peClose = this.calcValue(Currency::getClose, currencyQuote, DailyQuote::getAdjClose,
							dailyQuote, pts.getPortfolio());
					return new CalcPortfolioElement(pts.getSymbol().getId(), myPortfolioQuote.getLocalDay(), peClose,
							pts.getSymbol().getName(), pts.getWeight());
				})).findFirst().filter(Optional::isPresent).map(Optional::get);
	}

	private DailyQuote upsertPortfolioQuote(Currency currencyQuote, DailyQuote dailyQuote,
			PortfolioToSymbol portfolioToSymbol, PortfolioSymbolWithDailyQuotes portfolioQuotes) {
		DailyQuote portfolioQuote = portfolioQuotes.dailyQuotes.stream()
				.filter(myDailyQuote -> myDailyQuote.getLocalDay().isEqual(dailyQuote.getLocalDay())).findFirst()
				.orElse(new DailyQuote());
		portfolioQuote.setClose(this.calcValue(Currency::getClose, currencyQuote, DailyQuote::getClose, dailyQuote,
				portfolioToSymbol, portfolioQuote.getClose()));
		portfolioQuote.setAdjClose(this.calcValue(Currency::getClose, currencyQuote, DailyQuote::getClose, dailyQuote,
				portfolioToSymbol, portfolioQuote.getAdjClose()));
		portfolioQuote.setCurrencyKey(portfolioQuotes.symbol().getCurrencyKey());
		portfolioQuote.setHigh(this.calcValue(Currency::getHigh, currencyQuote, DailyQuote::getHigh, dailyQuote,
				portfolioToSymbol, portfolioQuote.getHigh()));
		portfolioQuote.setLocalDay(dailyQuote.getLocalDay());
		portfolioQuote.setLow(this.calcValue(Currency::getLow, currencyQuote, DailyQuote::getLow, dailyQuote,
				portfolioToSymbol, portfolioQuote.getLow()));
		portfolioQuote.setOpen(this.calcValue(Currency::getOpen, currencyQuote, DailyQuote::getOpen, dailyQuote,
				portfolioToSymbol, portfolioQuote.getOpen()));
		portfolioQuote.setSymbol(portfolioQuotes.symbol());
		portfolioQuote.setSymbolKey(portfolioQuotes.symbol().getSymbol());
		portfolioQuote.setVolume(1L);
		portfolioToSymbol.getSymbol().getDailyQuotes().add(portfolioQuote);
		portfolioQuotes.dailyQuotes.add(portfolioQuote);
		return portfolioQuote;
	}

	private BigDecimal calcValue(Function<? super Currency, BigDecimal> currExtractor, Currency currencyQuote,
			Function<? super DailyQuote, BigDecimal> quoteExtractor, DailyQuote dailyQuote,
			PortfolioToSymbol portfolioToSymbol, final BigDecimal portfolioClose) {
		BigDecimal calcValue = this.calcValue(currExtractor, currencyQuote, quoteExtractor, dailyQuote,
				portfolioToSymbol.getPortfolio());
		return Optional.ofNullable(portfolioClose).orElse(BigDecimal.ZERO).add(calcValue)
				.multiply(BigDecimal.valueOf(portfolioToSymbol.getWeight()));
	}
}
