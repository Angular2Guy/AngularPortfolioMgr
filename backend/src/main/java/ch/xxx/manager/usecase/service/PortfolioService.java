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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.xxx.manager.domain.exception.ResourceForbiddenException;
import ch.xxx.manager.domain.exception.ResourceNotFoundException;
import ch.xxx.manager.domain.model.dto.PortfolioDto;
import ch.xxx.manager.domain.model.entity.AppUserRepository;
import ch.xxx.manager.domain.model.entity.DailyQuote;
import ch.xxx.manager.domain.model.entity.DailyQuoteRepository;
import ch.xxx.manager.domain.model.entity.Portfolio;
import ch.xxx.manager.domain.model.entity.PortfolioElement;
import ch.xxx.manager.domain.model.entity.PortfolioElementRepository;
import ch.xxx.manager.domain.model.entity.PortfolioRepository;
import ch.xxx.manager.domain.model.entity.PortfolioToSymbol;
import ch.xxx.manager.domain.model.entity.PortfolioToSymbolRepository;
import ch.xxx.manager.domain.model.entity.Symbol;
import ch.xxx.manager.domain.model.entity.Symbol.QuoteSource;
import ch.xxx.manager.domain.model.entity.SymbolRepository;
import ch.xxx.manager.domain.model.entity.dto.CalcPortfolioElement;
import ch.xxx.manager.domain.model.entity.dto.PortfolioBarsWrapper;
import ch.xxx.manager.domain.model.entity.dto.PortfolioWithElements;
import ch.xxx.manager.domain.utils.DataHelper.CurrencyKey;
import ch.xxx.manager.domain.utils.StreamHelpers;

@Service
@Transactional
public class PortfolioService {
	private static final Logger LOGGER = LoggerFactory.getLogger(PortfolioService.class);
	private final PortfolioRepository portfolioRepository;
	private final PortfolioElementRepository portfolioElementRepository;
	private final PortfolioToSymbolRepository portfolioToSymbolRepository;
	private final SymbolRepository symbolRepository;
	private final AppUserRepository appUserRepository;
	private final PortfolioCalculationService portfolioCalculationService;
	private final PortfolioToIndexService portfolioToIndexService;
	private final DailyQuoteRepository dailyQuoteRepository;

	public PortfolioService(PortfolioRepository portfolioRepository, AppUserRepository appUserRepository,
			PortfolioElementRepository portfolioElementRepository, DailyQuoteRepository dailyQuoteRepository,
			PortfolioToSymbolRepository portfolioToSymbolRepository, SymbolRepository symbolRepository,
			PortfolioCalculationService portfolioCalculationService, PortfolioToIndexService portfolioToIndexService) {
		this.portfolioRepository = portfolioRepository;
		this.portfolioToSymbolRepository = portfolioToSymbolRepository;
		this.symbolRepository = symbolRepository;
		this.portfolioCalculationService = portfolioCalculationService;
		this.appUserRepository = appUserRepository;
		this.portfolioToIndexService = portfolioToIndexService;
		this.portfolioElementRepository = portfolioElementRepository;
		this.dailyQuoteRepository = dailyQuoteRepository;
	}

	public List<Portfolio> getPortfoliosByUserId(Long userId) {
		return this.portfolioRepository.findByUserId(userId);
	}

	public Portfolio getPortfolioById(Long portfolioId) {
		return this.portfolioRepository.findById(portfolioId)
				.orElseThrow(() -> new ResourceNotFoundException("Portfolio not found: " + portfolioId));
	}

	public Long countPortfolioSymbolsByUserId(Long userId) {
		return this.portfolioRepository.countPortfolioSymbolsByUserId(userId);
	}

	public PortfolioBarsWrapper getPortfolioBarsByIdAndStart(Long portfolioId, LocalDate start,
			List<ComparisonIndex> compIndexes) {
		Portfolio portfolio = this.portfolioRepository.findById(portfolioId)
				.orElseThrow(() -> new ResourceNotFoundException("Portfolio not found: " + portfolioId));
		List<PortfolioCalculationService.ComparisonIndexQuotes> comparisonQuotes = compIndexes.stream()
				.map(ci -> new PortfolioCalculationService.ComparisonIndexQuotes(ci, this.portfolioToIndexService
						.calculateIndexComparison(portfolioId, ci, start.minus(1, ChronoUnit.MONTHS), LocalDate.now())))
				.toList();
		// LOGGER.info("" + comparisonQuotes.size());
		List<CalcPortfolioElement> portfolioBars = this.portfolioCalculationService.calculatePortfolioBars(portfolio,
				start, comparisonQuotes);
		return new PortfolioBarsWrapper(portfolio, start, portfolioBars);
	}

	public Portfolio addPortfolio(Portfolio entity, Long userId) {
		entity.setAppUser(this.appUserRepository.findById(userId).orElse(null));
		Portfolio portfolio = this.portfolioRepository.save(entity);
		portfolio.getPortfolioToSymbols().add(createPortfolioPtSAndSymbol(portfolio));
		return portfolio;
	}

	public PortfolioWithElements addSymbolToPortfolio(PortfolioDto dto, Long symbolId, Long weight,
			LocalDateTime changedAt) {
		Portfolio updatedPortfolio = this.portfolioToSymbolRepository
				.saveAndFlush(this.createPtsEntity(dto, symbolId, weight, changedAt.toLocalDate())).getPortfolio();
		Optional<PortfolioToSymbol> ptsOpt = updatedPortfolio.getPortfolioToSymbols().stream()
				.filter(myPts -> myPts.getSymbol().getId().equals(symbolId)).findFirst();
		PortfolioWithElements portfolioWithElements = this.portfolioCalculationService
				.calculatePortfolio(updatedPortfolio, ptsOpt);
		return updatePortfolioElements(portfolioWithElements);
	}

	private Portfolio removeDailyQuotes(Portfolio portfolio, List<DailyQuote> dailyQuotesToRemove) {
		portfolio.getPortfolioToSymbols()
				.forEach(pts -> pts.getSymbol().getDailyQuotes().removeAll(dailyQuotesToRemove));
		return portfolio;
	}

	private List<DailyQuote> filterDailyQuotes(List<DailyQuote> dailyQuotes, List<DailyQuote> dailyQuotesToRemove) {
		return dailyQuotes.stream()
				.filter(myDailyQuote -> dailyQuotesToRemove.stream()
						.noneMatch(myRemoveQuote -> myDailyQuote.getLocalDay().isEqual(myRemoveQuote.getLocalDay())))
				.collect(Collectors.toList());
	}

	private PortfolioWithElements updatePortfolioElements(final PortfolioWithElements portfolioWithElements) {
		Portfolio portfolio = this.removeDailyQuotes(portfolioWithElements.portfolio(),
				portfolioWithElements.dailyQuotesToRemove());
		List<DailyQuote> filteredDailyQuotes = this.filterDailyQuotes(portfolioWithElements.portfolioDailyQuotes(),
				portfolioWithElements.dailyQuotesToRemove());
		List<PortfolioElement> portfolioElements = StreamHelpers
				.toStream(this.portfolioElementRepository.saveAll(portfolioWithElements.portfolioElements()))
				.collect(Collectors.toList());
//		filteredDailyQuotes.stream().filter(dq -> dq.getLocalDay().isAfter(LocalDate.of(2022, 9, 1)))
//				.peek(dq -> LOGGER.info("Porfolio: {} {}", dq.getId(), dq.getLocalDay().toString())).count();
		filteredDailyQuotes = this.dailyQuoteRepository
				.saveAll(filteredDailyQuotes.stream().collect(Collectors.toList()));
//		portfolioWithElements.dailyQuotesToRemove().stream()
//				.filter(dq -> dq.getLocalDay().isAfter(LocalDate.of(2022, 9, 1)))
//				.peek(dq -> LOGGER.info("Porfolio remove: {} {}", dq.getId(), dq.getLocalDay().toString())).count();
		this.dailyQuoteRepository
				.deleteAll(portfolioWithElements.dailyQuotesToRemove().stream().collect(Collectors.toList()));
		return new PortfolioWithElements(portfolio, portfolioElements, List.of(), List.of());
	}

	public PortfolioWithElements updatePortfolioSymbolWeight(PortfolioDto dto, Long symbolId, Long weight,
			LocalDateTime changedAt) {
		return this.portfolioToSymbolRepository.findByPortfolioIdAndSymbolId(dto.getId(), symbolId).stream()
				.flatMap(myEntity -> Stream.of(this.updateWeightPtsEntity(myEntity, weight, changedAt.toLocalDate())))
				.map(newEntity -> this.portfolioToSymbolRepository.saveAndFlush(newEntity))
				.map(newEntity -> this.updatePortfolioElements(this.portfolioCalculationService
						.calculatePortfolio(newEntity.getPortfolio(), Optional.empty())))
				.findFirst().orElseThrow(() -> new ResourceNotFoundException(
						String.format("Failed to remove symbol: %d from portfolio: %d", symbolId, dto.getId())));
	}

	public PortfolioWithElements removeSymbolFromPortfolio(Long portfolioId, Long symbolId, LocalDateTime removedAt) {
		return this.portfolioToSymbolRepository.findByPortfolioIdAndSymbolId(portfolioId, symbolId).stream()
				.flatMap(entity -> Stream.of(this.portfolioToSymbolRepository
						.saveAndFlush(this.markAsRemovedPtsEntity(entity, removedAt.toLocalDate()))))
				.map(newEntity -> this.removePortfolioElement(newEntity))
				.map(newEntity -> this.portfolioCalculationService.calculatePortfolio(newEntity.getPortfolio(),
						Optional.empty()))
				.peek(portfolioWithElements -> this.dailyQuoteRepository
						.saveAll(portfolioWithElements.portfolioDailyQuotes()))
				.peek(portfolioWithElements -> this.removeDailyQuotes(portfolioWithElements.portfolio(),
						portfolioWithElements.dailyQuotesToRemove()))
				.peek(portfolioWithElements -> this.dailyQuoteRepository
						.deleteAll(portfolioWithElements.dailyQuotesToRemove()))
				.findFirst().orElseThrow(() -> new ResourceNotFoundException(
						String.format("Failed to remove symbol: %d from portfolio: %d", symbolId, portfolioId)));
	}

	private PortfolioToSymbol removePortfolioElement(PortfolioToSymbol portfolioToSymbol) {
		List<PortfolioElement> toRemove = portfolioToSymbol.getPortfolio().getPortfolioElements().stream()
				.filter(myPortfolioElement -> myPortfolioElement.getSymbol()
						.equalsIgnoreCase(portfolioToSymbol.getSymbol().getSymbol()))
				.toList();
		portfolioToSymbol.getPortfolio().getPortfolioElements().removeAll(toRemove);
		toRemove.forEach(myPortfolioElement -> {
			myPortfolioElement.setPortfolio(null);
			this.portfolioElementRepository.delete(myPortfolioElement);
		});
		return portfolioToSymbol;
	}

	private PortfolioToSymbol markAsRemovedPtsEntity(PortfolioToSymbol entity, LocalDate removedAt) {
		PortfolioToSymbol portfolioToSymbol = new PortfolioToSymbol(null, entity.getPortfolio(), entity.getSymbol(), 0L,
				removedAt, removedAt);
		return portfolioToSymbol;
	}

	private PortfolioToSymbol updateWeightPtsEntity(PortfolioToSymbol entity, Long weight, LocalDate changedAt) {
		PortfolioToSymbol portfolioToSymbol = new PortfolioToSymbol(null, entity.getPortfolio(), entity.getSymbol(),
				weight, changedAt, null);
		return portfolioToSymbol;
	}

	private PortfolioToSymbol createPtsEntity(PortfolioDto dto, Long symbolId, Long weight, LocalDate changedAt) {
		Portfolio myPortfolio = this.portfolioRepository.findById(dto.getId())
				.orElseThrow(() -> new RuntimeException(String.format("Portfolio with id %d not found.", dto.getId())));
		if (250 >= this.countPortfolioSymbolsByUserId(myPortfolio.getAppUser().getId())) {
			final PortfolioToSymbol entity = new PortfolioToSymbol();
			myPortfolio.getPortfolioToSymbols().add(entity);
			entity.setPortfolio(myPortfolio);
			entity.setSymbol(this.symbolRepository.findById(symbolId).map(myPts -> {
				myPts.getPortfolioToSymbols().add(entity);
				return myPts;
			}).orElseThrow(() -> new RuntimeException(String.format("Symbol with id %d not found", symbolId))));
			entity.setWeight(weight);
			entity.setChangedAt(changedAt);
			return entity;
		} else {
			throw new ResourceForbiddenException("Too many Symbols in user Portfolios.");
		}
	}

	private PortfolioToSymbol createPortfolioPtSAndSymbol(Portfolio portfolioEntity) {
		Symbol symbolEntity = new Symbol(null, ServiceUtils.generateRandomPortfolioSymbol(), portfolioEntity.getName(),
				CurrencyKey.EUR, QuoteSource.PORTFOLIO);
		return this.portfolioToSymbolRepository
				.saveAndFlush(this.createPtSEntity(portfolioEntity, this.symbolRepository.save(symbolEntity)));
	}

	private PortfolioToSymbol createPtSEntity(Portfolio portfolioEntity, Symbol symbol) {
		PortfolioToSymbol ptSEntity = new PortfolioToSymbol();
		ptSEntity.setChangedAt(portfolioEntity.getCreatedAt());
		ptSEntity.setPortfolio(portfolioEntity);
		ptSEntity.setWeight(1L);
		ptSEntity.setSymbol(symbol);
		symbol.getPortfolioToSymbols().add(ptSEntity);
		return ptSEntity;
	}
}
