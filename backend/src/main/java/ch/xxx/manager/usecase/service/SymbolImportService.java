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

import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.crypto.tink.DeterministicAead;
import com.google.crypto.tink.InsecureSecretKeyAccess;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.TinkJsonProtoKeysetFormat;
import com.google.crypto.tink.daead.DeterministicAeadConfig;

import ch.xxx.manager.domain.model.dto.HkSymbolImportDto;
import ch.xxx.manager.domain.model.entity.AppUserRepository;
import ch.xxx.manager.domain.model.entity.DailyQuoteRepository;
import ch.xxx.manager.domain.model.entity.Symbol;
import ch.xxx.manager.domain.model.entity.Symbol.QuoteSource;
import ch.xxx.manager.domain.model.entity.SymbolRepository;
import ch.xxx.manager.domain.utils.DataHelper.CurrencyKey;
import ch.xxx.manager.domain.utils.StreamHelpers;
import ch.xxx.manager.usecase.service.QuoteImportService.UserKeys;
import jakarta.annotation.PostConstruct;

@Service
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class SymbolImportService {
	private static final Logger LOGGER = LoggerFactory.getLogger(SymbolImportService.class);
	private static final Long PORTFOLIO_SYMBOL_LIMIT = 200L;

	private final NasdaqClient nasdaqClient;
	private final HkexClient hkexClient;
	private final SymbolRepository repository;
	private final DailyQuoteRepository dailyQuoteRepository;
	private final XetraClient xetraClient;
	private final AtomicReference<List<Symbol>> allSymbolEntities = new AtomicReference<>(new ArrayList<>());
	private final QuoteImportService quoteImportService;
	private final AppUserRepository appUserRepository;
	private DeterministicAead daead;
	@Value("${tink.json.key}")
	String tinkJsonKey;

	public SymbolImportService(NasdaqClient nasdaqClient, HkexClient hkexClient, SymbolRepository repository, DailyQuoteRepository dailyQuoteRepository,
			XetraClient xetraClient, QuoteImportService quoteImportService, AppUserRepository appUserRepository) {
		this.nasdaqClient = nasdaqClient;
		this.hkexClient = hkexClient;
		this.repository = repository;
		this.xetraClient = xetraClient;
		this.quoteImportService = quoteImportService;
		this.appUserRepository = appUserRepository;
		this.dailyQuoteRepository = dailyQuoteRepository;
	}

	@PostConstruct
	public void init() throws GeneralSecurityException {
		// LOGGER.info(this.tinkJsonKey);
		DeterministicAeadConfig.register();
		KeysetHandle handle = TinkJsonProtoKeysetFormat.parseKeyset(this.tinkJsonKey, InsecureSecretKeyAccess.get());
		this.daead = handle.getPrimitive(DeterministicAead.class);
	}

	public List<Symbol> refreshSymbolEntities() {
		List<Symbol> symbolEnities = this.repository.findAll();
		this.allSymbolEntities.set(symbolEnities);
		LOGGER.info("{} symbols updated.", symbolEnities.size());
		return Collections.unmodifiableList(new ArrayList<Symbol>(this.allSymbolEntities.get()));
	}

	@Async
	public Future<Long> updateSymbolQuotes(List<Symbol> symbolsToUpdate) {
		List<UserKeys> allUserKeys = this.appUserRepository.findAll().stream()
				.map(myAppUser -> new UserKeys(
						this.decrypt(myAppUser.getAlphavantageKey(), UUID.fromString(myAppUser.getUuid())),
						this.decrypt(myAppUser.getRapidApiKey(), UUID.fromString(myAppUser.getUuid()))))
				.filter(myUserKeys -> Optional.ofNullable(myUserKeys.alphavantageKey()).stream()
						.filter(myStr -> !myStr.isBlank()).findFirst().isPresent())
				.filter(myUserKeys -> Optional.ofNullable(myUserKeys.RapidApiKey()).stream()
						.filter(myStr -> !myStr.isBlank()).findFirst().isPresent())
				.toList();
		LOGGER.info("UserKeys size: {}", allUserKeys.size());
		final AtomicLong indexDaily = new AtomicLong(-1L);
		Long quoteCount = symbolsToUpdate.stream().flatMap(mySymbol -> {
			var myIndex = indexDaily.addAndGet(1L);
			long userKeyIndex = Math.floorDiv(myIndex, PORTFOLIO_SYMBOL_LIMIT);
			return Stream.of(this.quoteImportService.importUpdateDailyQuotes(mySymbol.getSymbol(),
					Duration.ofMillis(100), allUserKeys.get((Long.valueOf(userKeyIndex).intValue()))));
		}).reduce(0L, (acc, value) -> acc + value);
		LOGGER.info("Daily Quote import done for: {}", quoteCount);
		LOGGER.info("updateSymbolQuotes done.");
		return CompletableFuture.completedFuture(quoteCount);
	}

	public String decrypt(String ciphertext, UUID userUuid) {
		byte[] decrypted;
		try {
			decrypted = daead.decryptDeterministically(
					Base64.getDecoder().decode(ciphertext.getBytes(Charset.defaultCharset())),
					userUuid.toString().getBytes(Charset.defaultCharset()));
		} catch (GeneralSecurityException e) {
			LOGGER.debug(String.format("Decryption failed cipherText: '%s', userUuid: '%s', charSet: %s", ciphertext, userUuid, Charset.defaultCharset().toString()), e);
			decrypted = new byte[0];
		}
		var result = new String(decrypted, Charset.defaultCharset());		
		return result;
	}

	public String importUsSymbols() {
		Long symbolCount = this.importUsSymbols(this.nasdaqClient.importSymbols());
		return String.format("Nasdaq imported symbols: %d", symbolCount);
	}

	public Long importUsSymbols(List<String> nasdaq) {
		this.refreshSymbolEntities();
		LOGGER.info("importUsSymbols() called.");
		List<Symbol> result = this.repository
				.saveAll(nasdaq.stream().filter(this::filter).flatMap(symbolStr -> this.convert(symbolStr))
						.flatMap(entity -> this.replaceEntity(entity)).collect(Collectors.toList()));
		return Long.valueOf(result.size());
	}

	public String importHkSymbols() {
		Long symbolCount = this.hkexClient.importSymbols().stream().flatMap(hkex -> Stream.of((this.importHkSymbols(hkex)))).count();
		return String.format("Hkex imported symbols: %d", symbolCount);
	}

	public Long importHkSymbols(List<HkSymbolImportDto> hkex) {
		this.refreshSymbolEntities();
		LOGGER.info("importHkSymbols() called.");
		List<Symbol> result = this.repository
				.saveAll(hkex.stream().filter(this::filter).flatMap(myDto -> this.convert(myDto))
						.flatMap(entity -> this.replaceEntity(entity)).collect(Collectors.toList()));
		return Long.valueOf(result.size());
	}

	public String importDeSymbols() {
		Long symbolCount = this.xetraClient.importXetraSymbols().stream().flatMap(xetra -> Stream.of(this.importDeSymbols(xetra))).count();
		return String.format("Xetra imported symbols: %d", symbolCount);
	}

	public Long importDeSymbols(List<String> xetra) {
		this.refreshSymbolEntities();
		LOGGER.info("importDeSymbols() called.");
		List<Symbol> result = this.repository.saveAll(xetra.stream().filter(this::filter).filter(this::filterXetra)
				.flatMap(line -> this.convertXetra(line)).collect(Collectors.groupingBy(Symbol::getSymbol)).entrySet()
				.stream()
				.flatMap(group -> group.getValue().isEmpty() ? Stream.empty() : Stream.of(group.getValue().get(0)))
				.flatMap(entity -> this.replaceEntity(entity)).collect(Collectors.toList()));
		return Long.valueOf(result.size());
	}

	public List<String> importReferenceIndexes(List<String> symbolStrs) {
		LOGGER.info("importReferenceIndexes() called.");
		final List<String> localSymbolStrs = symbolStrs;
		final Set<String> symbolStrsToImport = new HashSet<>();
		List<Symbol> result = this.repository.saveAll(
				localSymbolStrs.stream().flatMap(indexSymbol -> upsertSymbolEntity(indexSymbol)).map(entity -> {
					symbolStrsToImport.add(entity.getSymbol());
					return entity;
				}).collect(Collectors.toList()));
		return result.stream().map(myEntity -> myEntity.getSymbol()).collect(Collectors.toList());
	}

	public List<Symbol> findSymbolsToUpdate() {
		List<String> symbolsToFilter = List.of(ComparisonIndex.SP500.getSymbol(),
				ComparisonIndex.EUROSTOXX50.getSymbol(), ComparisonIndex.MSCI_CHINA.getSymbol());
		List<Symbol> symbolsToUpdate = this.appUserRepository.findAll().stream()
				.map(appUser -> this.appUserRepository.findAllUserSymbolsByAppUserId(appUser.getId()))
				.flatMap(Set::stream).filter(StreamHelpers.distinctByKey(Symbol::getSymbol))
				.filter(mySymbol -> Optional.of(mySymbol.getSymbol()).stream()
						.noneMatch(aSymbol -> aSymbol.contains(ServiceUtils.PORTFOLIO_MARKER)))
				.filter(mySymbol -> symbolsToFilter.stream()
						.noneMatch(mySymbolStr -> mySymbolStr.equalsIgnoreCase(mySymbol.getSymbol())))
				.toList();
		return symbolsToUpdate;
	}
	
	public boolean deleteSymbolsWithDailyQuotes(Iterable<Symbol> symbols) {
		List<Symbol> symbolsToDelete = this.repository.findByQuoteSource(QuoteSource.DATA);
		this.dailyQuoteRepository.deleteAll(symbolsToDelete.stream().flatMap(mySymbol -> mySymbol.getDailyQuotes().stream()).toList());
		this.repository.deleteAll(symbolsToDelete);		
		return true;
	}

	public List<Symbol> findSymbolsByQuoteSource(QuoteSource quoteSource) {
		return this.repository.findByQuoteSource(quoteSource);
	}
	
	private Stream<Symbol> upsertSymbolEntity(String indexSymbol) {
		ComparisonIndex compIndex = Stream.of(ComparisonIndex.values())
				.filter(index -> index.getSymbol().equalsIgnoreCase(indexSymbol)).findFirst()
				.orElseThrow(() -> new RuntimeException("Unknown indexSymbol: " + indexSymbol));
		Symbol symbolEntity = new Symbol(null, compIndex.getSymbol(), compIndex.getName(), compIndex.getCurrencyKey(),
				compIndex.getSource());
		return this.replaceEntity(symbolEntity);
	}

	private Stream<Symbol> replaceEntity(Symbol entity) {
		return this.updateEntity(this.allSymbolEntities.get().stream()
				.filter(mySymbol -> mySymbol.getSymbol().toLowerCase().equals(entity.getSymbol().toLowerCase()))
				.findFirst().orElse(entity), entity);
	}

	private Stream<Symbol> updateEntity(Symbol dbEntity, Symbol importEntity) {
		if (!dbEntity.equals(importEntity)) {
			dbEntity.setName(importEntity.getName());
			dbEntity.setSymbol(importEntity.getSymbol());
			dbEntity.setCurrencyKey(importEntity.getCurrencyKey());
			dbEntity.setQuoteSource(importEntity.getQuoteSource());
		}
		return Stream.of(dbEntity);
	}

	private Stream<Symbol> convert(HkSymbolImportDto dto) {
		String cutSymbol = dto.getSymbol().trim().length() > 4
				? dto.getSymbol().trim().substring(dto.getSymbol().length() - 4)
				: dto.getSymbol().trim();
		return Stream.of(
				new Symbol(null, String.format("%s.HK", cutSymbol), dto.getName(), CurrencyKey.HKD, QuoteSource.YAHOO));
	}

	private boolean filter(HkSymbolImportDto dto) {
		long symbol = Long.parseLong(dto.getSymbol());
		return symbol < 10000;
	}

	private boolean filter(String line) {
		if (line.isBlank() || line.contains("ACT Symbol|Security Name|Exchange|")
				|| line.contains("File Creation Time:") || line.contains("Symbol|Security Name|Market Category|")
				|| line.contains("Market:") || line.contains("Date Last Update:")
				|| line.contains("Product Status;Instrument Status;")) {
			return false;
		}
		return true;
	}

	private boolean filterXetra(String line) {
		return line.contains("DEUTSCHLAND") || line.contains("DAX");
	}

	private Stream<Symbol> convertXetra(String symbolLine) {
		String[] strParts = symbolLine.split(";");
		String symbol = String.format("%s.DEX",
				strParts[7].substring(0, strParts[7].length() < 15 ? strParts[7].length() : 15));
		Symbol entity = new Symbol(null, symbol,
				strParts[2].substring(0, strParts[2].length() < 100 ? strParts[2].length() : 100), CurrencyKey.EUR,
				QuoteSource.YAHOO);
		return Stream.of(entity);
	}

	private Stream<Symbol> convert(String symbolLine) {
		String[] strParts = symbolLine.split("\\|");
		Symbol entity = new Symbol(null,
				strParts[0].substring(0, strParts[0].length() < 15 ? strParts[0].length() : 15),
				strParts[1].substring(0, strParts[1].length() < 100 ? strParts[1].length() : 100), CurrencyKey.USD,
				QuoteSource.YAHOO);
		return Stream.of(entity);
	}
}
