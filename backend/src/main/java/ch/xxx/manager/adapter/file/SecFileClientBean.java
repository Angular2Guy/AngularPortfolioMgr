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
package ch.xxx.manager.adapter.file;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.xxx.manager.domain.file.FileClient;
import ch.xxx.manager.domain.model.entity.dto.FinancialElementImportDto;
import ch.xxx.manager.domain.model.entity.dto.SymbolFinancialsDto;
import ch.xxx.manager.domain.utils.StreamHelpers;
import ch.xxx.manager.usecase.service.AppInfoService;
import ch.xxx.manager.usecase.service.FinancialDataService;

@Component
public class SecFileClientBean implements FileClient {
	private static final Logger LOGGER = LoggerFactory.getLogger(SecFileClientBean.class);
	private volatile boolean importDone = true; 
	private final AppInfoService appInfoService;
	private final ObjectMapper objectMapper;
	private final FinancialDataService financialDataImportService;
	@Value("${ssd.io:false}")
	private boolean ssdIo;
	String financialDataImportPath;

	public SecFileClientBean(AppInfoService appInfoService, ObjectMapper objectMapper,
			FinancialDataService financialDataImportService) {
		this.appInfoService = appInfoService;
		this.objectMapper = objectMapper;
		this.financialDataImportService = financialDataImportService;
	}

	@EventListener(ApplicationReadyEvent.class)
	public void doOnStartup() {
		this.financialDataImportPath = this.appInfoService.getFinancialDataImportPath();
	}

	public Boolean importZipFile(String filename) {
		this.importDone = false;
		Thread shutDownThread = createShutDownThread();
		Runtime.getRuntime().addShutdownHook(shutDownThread);
		ZipFile initialFile = null;
		try {
			initialFile = new ZipFile(this.financialDataImportPath + filename);
			Enumeration<? extends ZipEntry> entries = initialFile.entries();
			LocalDateTime startCleanup = LocalDateTime.now();
			LOGGER.info("Drop indexes.");
			this.financialDataImportService.dropFeIndexes();
			LOGGER.info("Clear start.");
			this.financialDataImportService.clearFinancialsData();
			LOGGER.info("Clear time: {}", ChronoUnit.MILLIS.between(startCleanup, LocalDateTime.now()));
			List<SymbolFinancialsDto> symbolFinancialsDtos = new ArrayList<>();
			boolean first = true;
			final int[] maxChildren = new int[1];
			maxChildren[0] = 0;
			while (entries.hasMoreElements()) {
				ZipEntry element = entries.nextElement();
				LocalDateTime start = LocalDateTime.now();
				if (!element.isDirectory() && element.getSize() > 10) {
					try(InputStream inputStream = initialFile.getInputStream(element)) {
						if (first) {
							LOGGER.info("Filename: {}, Filesize: {}", element.getName(), element.getSize());
							first = false;
						}
						String text = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
						SymbolFinancialsDto symbolFinancialsDto = this.objectMapper.readValue(text,
								SymbolFinancialsDto.class);
						StreamHelpers.toStream(symbolFinancialsDto.getData()).forEach(myFinancialsDataDto -> {
							myFinancialsDataDto.setBalanceSheet(myFinancialsDataDto.getBalanceSheet().stream()
									.map(this::fixConcept)
									.collect(Collectors.toSet()));
							myFinancialsDataDto.setCashFlow(myFinancialsDataDto.getCashFlow().stream()
									.map(this::fixConcept)
									.collect(Collectors.toSet()));
							myFinancialsDataDto.setIncome(myFinancialsDataDto.getIncome().stream()
									.map(this::fixConcept)
									.collect(Collectors.toSet()));
							int bsChildern = myFinancialsDataDto.getBalanceSheet() == null ? 0 : myFinancialsDataDto.getBalanceSheet().size();
							int cfChildern = myFinancialsDataDto.getCashFlow() == null ? 0 : myFinancialsDataDto.getCashFlow().size();
							int icChildern = myFinancialsDataDto.getIncome() == null ? 0 : myFinancialsDataDto.getIncome().size();
							maxChildren[0] = bsChildern + cfChildern + icChildern > maxChildren[0] ? bsChildern + cfChildern + icChildern : maxChildren[0];
						});
						symbolFinancialsDtos.add(symbolFinancialsDto);
//						LOGGER.info(symbolFinancialsDto.toString());
//						LOGGER.info(text != null ? text.substring(0, 100) : "");
					} catch (Exception e) {
						LOGGER.info("Exception with file: {}", element.getName(), e);
					}
				}
				if ((this.ssdIo ? symbolFinancialsDtos.size() >= 100 : symbolFinancialsDtos.size() >= 35) || !entries.hasMoreElements()) {
					this.financialDataImportService.storeFinancialsData(symbolFinancialsDtos);
					symbolFinancialsDtos.clear();
					LOGGER.info("Persist time: {}, MaxChildren: {}", ChronoUnit.MILLIS.between(start, LocalDateTime.now()), maxChildren[0]);
					first = true;
				}
			}
			LOGGER.info("Recreate indexes.");
			this.financialDataImportService.createFeIndexes();
			LOGGER.info("Indexes ready.");
			this.financialDataImportService.updateFeConcepts();
			LOGGER.info("FeConcepts updated.");
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			this.closeFile(initialFile);
		}
		Runtime.getRuntime().removeShutdownHook(shutDownThread);
		this.importDone = true;
		return true;
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

	private void closeFile(ZipFile zipFile) {
		if (zipFile != null) {
			try {
				zipFile.close();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
}
