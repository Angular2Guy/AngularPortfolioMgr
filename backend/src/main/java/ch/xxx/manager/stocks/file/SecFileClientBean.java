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
package ch.xxx.manager.stocks.file;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.zip.ZipFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import ch.xxx.manager.stocks.entity.dto.FinancialElementImportDto;
import ch.xxx.manager.stocks.entity.dto.FinancialsDataDto;
import ch.xxx.manager.stocks.entity.dto.SymbolFinancialsDto;
import ch.xxx.manager.common.utils.StreamHelpers;
import ch.xxx.manager.common.service.AppInfoService;
import ch.xxx.manager.findata.service.FinancialDataService;
import tools.jackson.databind.json.JsonMapper;

@Component("Sec")
public class SecFileClientBean implements FileClient {
	private static final Logger LOGGER = LoggerFactory.getLogger(SecFileClientBean.class);
	private volatile boolean importDone = true; 
	private final AppInfoService appInfoService;
	private final JsonMapper objectMapper;
	private final FinancialDataService financialDataImportService;
	@Value("${ssd.io:false}")
	private boolean ssdIo;
	String financialDataImportPath;

	public SecFileClientBean(AppInfoService appInfoService, JsonMapper objectMapper,
			FinancialDataService financialDataImportService) {
		this.appInfoService = appInfoService;
		this.objectMapper = objectMapper;
		this.financialDataImportService = financialDataImportService;
	}

	@EventListener(ApplicationReadyEvent.class)
	public void doOnStartup() {
		this.financialDataImportPath = this.appInfoService.getFinancialDataImportPath();
	}

    @Override
	public Boolean importZipFile(String filename) {
		if(!this.importDone) {
			return false;
		}
		this.importDone = false;
		Thread shutDownThread = createShutDownThread();
		Runtime.getRuntime().addShutdownHook(shutDownThread);
		try(ZipFile initialFile = new ZipFile(this.financialDataImportPath + filename)) {					
			LocalDateTime startCleanup = LocalDateTime.now();
			LOGGER.info("Drop indexes.");
			this.financialDataImportService.dropFeIndexes();
			LOGGER.info("Clear start.");
			this.financialDataImportService.clearFinancialsData();
			LOGGER.info("Clear time: {}", ChronoUnit.MILLIS.between(startCleanup, LocalDateTime.now()));
			List<SymbolFinancialsDto> symbolFinancialsDtos = new ArrayList<>();
			final var first = new AtomicBoolean(true);
			final var maxChildren = new AtomicInteger(0);
			final var start = new AtomicReference<LocalDateTime>(LocalDateTime.now());			
			StreamHelpers.convert(initialFile.entries()).forEach(zipEntry -> {
				start.set(LocalDateTime.now());
				if (!zipEntry.isDirectory() && zipEntry.getSize() > 10) {
					try(InputStream inputStream = new BufferedInputStream(initialFile.getInputStream(zipEntry))) {
						if (first.get()) {
							LOGGER.info("Filename: {}, Filesize: {}", zipEntry.getName(), zipEntry.getSize());
							first.set(false);
						}
						String text = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
						SymbolFinancialsDto symbolFinancialsDto = this.objectMapper.readValue(text,
								SymbolFinancialsDto.class);						
						maxChildren.set(StreamHelpers.toStream(symbolFinancialsDto.getData())
						.map(myFinancialsDataDto -> {
							cleanDto(myFinancialsDataDto);
							int bsChildern = Optional.ofNullable(myFinancialsDataDto.getBalanceSheet()).stream().map(Set::size).findFirst().orElse(0);
							int cfChildern = Optional.ofNullable(myFinancialsDataDto.getCashFlow()).stream().map(Set::size).findFirst().orElse(0); 
							int icChildern = Optional.ofNullable(myFinancialsDataDto.getIncome()).stream().map(Set::size).findFirst().orElse(0);
							return bsChildern + cfChildern + icChildern;
						}).max(Integer::compareTo).orElse(0));						
						symbolFinancialsDtos.add(symbolFinancialsDto);
//						LOGGER.info(symbolFinancialsDto.toString());
//						LOGGER.info(text != null ? text.substring(0, 100) : "");
					} catch (Exception e) {
						LOGGER.info("Exception with file: {}", zipEntry.getName(), e);
					}
				}
				if ((this.ssdIo ? symbolFinancialsDtos.size() >= 100 : symbolFinancialsDtos.size() >= 35)) {
					storeEntries(symbolFinancialsDtos, first, maxChildren, start);
				}
			});
			storeEntries(symbolFinancialsDtos, first, maxChildren, start);
			LOGGER.info("Import time: {} seconds.", ChronoUnit.SECONDS.between(start.get(), LocalDateTime.now()));
			var startCreateIndexes = LocalDateTime.now();
			LOGGER.info("Recreate indexes.");
			this.financialDataImportService.createFeIndexes();
			LOGGER.info("Indexes ready: {} seconds.",
					ChronoUnit.SECONDS.between(startCreateIndexes, LocalDateTime.now()));
			this.financialDataImportService.updateFeConcepts();
			LOGGER.info("FeConcepts updated: {} seconds.",
					ChronoUnit.SECONDS.between(start.get(), LocalDateTime.now()));
		} catch (IOException e) {
			throw new RuntimeException(e);
		} 
		Runtime.getRuntime().removeShutdownHook(shutDownThread);
		this.importDone = true;
		return true;
	}

	private void cleanDto(FinancialsDataDto myFinancialsDataDto) {
		myFinancialsDataDto.setBalanceSheet(myFinancialsDataDto.getBalanceSheet().stream()
				.map(this::fixConcept)
				.collect(Collectors.toSet()));
		myFinancialsDataDto.setCashFlow(myFinancialsDataDto.getCashFlow().stream()
				.map(this::fixConcept)
				.collect(Collectors.toSet()));
		myFinancialsDataDto.setIncome(myFinancialsDataDto.getIncome().stream()
				.map(this::fixConcept)
				.collect(Collectors.toSet()));
	}

	private void storeEntries(List<SymbolFinancialsDto> symbolFinancialsDtos, final AtomicBoolean first,
			final AtomicInteger maxChildren, AtomicReference<LocalDateTime> start) {
		LOGGER.info("Compute time: {}, MaxChildren: {}", ChronoUnit.MILLIS.between(start.get(), LocalDateTime.now()), maxChildren.get());
		this.financialDataImportService.storeFinancialsData(symbolFinancialsDtos);
		symbolFinancialsDtos.clear();
		LOGGER.info("Persist time: {}, MaxChildren: {}", ChronoUnit.MILLIS.between(start.get(), LocalDateTime.now()), maxChildren.get());
		first.set(true);
	}

	private Thread createShutDownThread() {
		return new Thread(() -> {
			while(!this.importDone) {
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					LOGGER.warn("ShutdownHook Thread interrupted.", e);
				}
			}
			LOGGER.info("ShutdownHook Thread is Done.");
		});
	}

	private FinancialElementImportDto fixConcept(FinancialElementImportDto myFinancialElementDto) {
		myFinancialElementDto.setConcept(
				myFinancialElementDto.getConcept() != null && myFinancialElementDto.getConcept().contains(":")
						? myFinancialElementDto.getConcept().trim()
								.substring(myFinancialElementDto.getConcept().indexOf(':') + 1)
						: myFinancialElementDto.getConcept());
		return myFinancialElementDto;
	}
}
